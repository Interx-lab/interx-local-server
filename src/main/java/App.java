import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import interx.protos.Protocol.EmptyMessage;
import interx.protos.Protocol.Message;
import interx.protos.ServerGrpc;


public class App {
    private static final Logger logger = Logger.getLogger(App.class.getName());

    private final ManagedChannel channel;
    private final ServerGrpc.ServerBlockingStub blockingStub;

    public App(String host, int port) {
      this(ManagedChannelBuilder.forAddress(host, port)
          // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
          // needing certificates.
          .usePlaintext(true)
          .build());
    }

    App(ManagedChannel channel) {
      this.channel = channel;
      blockingStub = ServerGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
      channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void send(String json) {
      logger.info("Will try to send " + json + " ...");
      Message request = Message.newBuilder().setJson(json).build();
      EmptyMessage response;
      try {
        response = blockingStub.send(request);
      } catch (StatusRuntimeException e) {
        logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        return;
      }
      logger.info("Sent.");
    }

    public static void main(String[] args) throws Exception {
      App client = new App("localhost", 50051);
      try {
        client.send("{company:\"AppleKingdom\", factory:\"Universal\"}, machine:\"125\", temperature:\"125\"");
        client.send("{company:\"AppleKingdom\", factory:\"Tree\"}, machine:\"117\", temperature:\"50\"");
        client.send("{company:\"SamsungNet\", factory:\"Electronics\"}, machine:\"99\", temperature:\"15\", speed:\"45 km/h\"");
      } finally {
        client.shutdown();
      }
    }

}
