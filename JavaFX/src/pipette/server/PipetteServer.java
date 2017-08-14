package pipette.server;

import com.sun.net.httpserver.HttpServer;
import pipette.painter.Main;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by ordagan on 30.8.2016.
 */
public class PipetteServer {

    HttpServer server;
    Main mainApplication;

    public PipetteServer(Main mainApplication) {
        this.mainApplication = mainApplication;
    }

    public void startServer(int port) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port),0);
        System.out.println("Server starting at port: " + port);
        server.createContext("/", new PipetteHandler(this));
        server.setExecutor(null);
        server.start();
    }

    public void stop(){
        server.stop(3);
        System.out.println("Stopped server");
    }

    public void check(int r, int g, int b) {
        System.out.println("check");
        mainApplication.pipetteAction(r,g,b);
    }
}
