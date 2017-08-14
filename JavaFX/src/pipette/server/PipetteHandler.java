package pipette.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import javafx.scene.paint.Color;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ordagan on 30.8.2016.
 */
public class PipetteHandler implements HttpHandler {
    public final static String ACK = "Acknowledge";
    Pattern pattern = Pattern.compile("Color_r_(\\d{1,5})g_(\\d{1,5})b_(\\d{1,5}).*");
    Pattern pattern2 = Pattern.compile("Button");

    PipetteServer server;

    public PipetteHandler(PipetteServer server) {
        this.server = server;
    }

    public void handle(HttpExchange httpExchange) throws IOException {

        InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), "utf-8");
        BufferedReader br = new BufferedReader(isr);
        String query = br.readLine();
        System.out.println(query);
        Matcher colorPatternMatcher = pattern.matcher(query);
        Matcher buttonMatcher = pattern2.matcher(query);

        if (colorPatternMatcher.matches()) {
            int red = Integer.valueOf(colorPatternMatcher.group(1));
            int green = Integer.valueOf(colorPatternMatcher.group(2));
            int blue = Integer.valueOf(colorPatternMatcher.group(3));

            if (red < 110) {red=0;}
            if (green < 110) {green=0;}
            if (blue < 40) {blue=0;}

            double redD, greenD, blueD;
            redD = red/2000.0;
            greenD = (green/600.0) - (red/6000.0);
            if (blue > 500) {
                blueD = 1;
            } else {
                blueD = (blue / Math.max(Math.max(red, green),1)) - (red/6000.0);
            }

            if (redD < 0) { redD = 0; }
            if (greenD < 0) { greenD = 0; }
            if (blueD < 0) { blueD = 0; }

            redD *= 255;
            greenD *= 255;
            blueD *= 255;

            redD = Math.min(redD,255);
            greenD = Math.min(greenD,255);
            blueD = Math.min(blueD,255);

            if (redD > (blueD + greenD) * 2) {
                redD = 230;
            }
            server.check((int)redD,(int)greenD,(int)blueD);
            //server.mainApplication.pipetteAction((int)redD,(int)greenD,(int)blueD);
        }
        else if (buttonMatcher.matches()) {
            server.mainApplication.pipetteButton();
        }
        httpExchange.sendResponseHeaders(200, ACK.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(ACK.getBytes());
        os.close();
    }
}
