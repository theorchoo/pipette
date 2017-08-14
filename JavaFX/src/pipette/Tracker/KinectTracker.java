package pipette.Tracker;

import com.sun.glass.ui.Pixels;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.*;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jdk.nashorn.internal.runtime.ECMAException;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;
import org.openkinect.freenect.*;
import sun.awt.image.ImageDecoder;
import sun.awt.image.InputStreamImageSource;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;


public class KinectTracker extends Application {
    Scene monitorScene;
    static BufferedImage image;
    boolean track = false;
    Context ctx = null;
    Device dev = null;
    ImageView imageView_origin;
    ImageView imageView_transformed;
    BufferedImage outRgb;
    Mat startM;
    Mat endM;
    Mat trans = new Mat(3,3, CvType.CV_32F);
    Button calButton;
    int clicks = 4;
    ArrayList<Point> src_pnt = new ArrayList<>();
    private double curX = -1;
    private double curY = -1;

    public double getX() {
        return curX;
    }

    public double getY() {
        return curY;
    }

    public boolean pipetteInView() {
        return curX >= 0 && curY >= 0;
    }

    public void calibrate() {
        System.out.println("start calibration.");
        clicks = 0;
        src_pnt.clear();
        calButton.setDisable(true);
        monitorScene.getRoot().setStyle("-fx-background-color: FF0000;");
    }

    public void endCalibrate() {
        System.out.println("end calibration.");
        startM = Converters.vector_Point2f_to_Mat(src_pnt);
        trans = Imgproc.getPerspectiveTransform(startM,endM);
        calButton.setDisable(false);
        monitorScene.getRoot().setStyle("-fx-background-color: FFFFFF;");
    }

    public void start(Stage primaryStage) throws Exception {
        BorderPane borderPane = new BorderPane();
        imageView_origin = new ImageView();
        imageView_transformed = new ImageView();
        HBox buttonBar = new HBox();

        calButton = new Button("Calibrate");
        calButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                calibrate();
            }
        });

        Button startTrack = new Button("Start Tracking!");
        startTrack.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                track = !track;
                if (startTrack.getText().equals("Start Tracking!")) {
                    startTrack.setText("Stop Tracking");
                } else {
                    startTrack.setText("Start Tracking!");
                }
            }
        });


        imageView_origin.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (clicks < 3) {
                    src_pnt.add(new Point(event.getX(), event.getY()));
                    clicks++;
                } else if (clicks == 3) {
                    src_pnt.add(new Point(event.getX(), event.getY()));
                    clicks++;
                    endCalibrate();
                }
            }
        });

        buttonBar.getChildren().addAll(calButton, startTrack);
        buttonBar.setPrefWidth(500);
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.setSpacing(20);

        borderPane.setPadding(new Insets(20,20,20,20));
        borderPane.setLeft(imageView_origin);
        borderPane.setRight(imageView_transformed);
        borderPane.setBottom(buttonBar);

        monitorScene = new Scene(borderPane,1100,600);
        primaryStage.setScene(monitorScene);


        // default transform matrix
        ArrayList<Point> dst_pnt = new ArrayList<>();
        Point p4 = new Point(0.0, 0.0);
        dst_pnt.add(p4);
        Point p5 = new Point(640.0, 0.0);
        dst_pnt.add(p5);
        Point p6 = new Point(0.0, 488.0);
        dst_pnt.add(p6);
        Point p7 = new Point(640.0, 488.0);
        dst_pnt.add(p7);
        //endM = Converters.vector_Point2f_to_Mat(dst_pnt);
        endM = new Mat(4,1,CvType.CV_32FC2);
        endM.put(0,0,0.0,0.0,640.0,0.0,0.0,488.0,640.0,488.0);
        startM = new Mat(4,1,CvType.CV_32FC2);
        startM.put(0,0,0.0,0.0,640.0,0.0,0.0,488.0,640.0,488.0);
        System.out.println(endM);
        System.out.println(startM);
        trans = Imgproc.getPerspectiveTransform(startM,endM);
        System.out.println(trans);
        // INITIALIZE DEVICE
        ctx = Freenect.createContext();
        if (ctx.numDevices() > 0) {
            dev = ctx.openDevice(0);
        } else {
            System.err.println("No kinects detected.  Exiting.");
            System.exit(0);
        }

        dev.setVideoFormat(VideoFormat.IR_8BIT);
        VideoHandler videoHandler = new VideoHandler() {
            @Override
            public void onFrameReceived(FrameMode frameMode, ByteBuffer byteBuffer, int i) {
                processRgb(frameMode,byteBuffer,i);
            }
        };
        dev.startVideo(videoHandler);

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if (ctx != null)
                    if (dev != null) {
                        dev.close();
                    }
                ctx.shutdown();
            }
        });
        // SHUT DOWN
        primaryStage.show();
    }

    protected void processRgb( FrameMode mode, ByteBuffer frame, int timestamp ) {
//        if( mode.getVideoFormat() != VideoFormat.IR_8BIT ) {
//            System.out.println("Bad rgb format!");
//        }

//        System.out.println("Got rgb! "+timestamp);
        byte[] b =  new byte[frame.remaining()];
        frame.get(b);
//        System.out.println(b.length);

        Mat mat = new Mat(mode.getHeight(),mode.getWidth(), CvType.CV_8UC1);
        mat.put(0,0,b);
        frame.clear();

        outRgb = new BufferedImage(mode.getWidth(),mode.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
        outRgb.setData(Raster.createRaster(outRgb.getSampleModel(), new DataBufferByte(b,b.length),new
                java.awt.Point()));

        WritableImage img = new WritableImage(mode.getWidth(),mode.getHeight());
        SwingFXUtils.toFXImage(outRgb, img);

        Mat mat2 = mat.clone();
        Imgproc.warpPerspective(mat2,mat2,trans,new Size(640,488));

        if (track) {
            Core.MinMaxLocResult minMaxLocResult = Core.minMaxLoc(mat2);
            if (minMaxLocResult.maxVal < 10) {
                // not led.
                curX = curY = -1;
            } else {
                curX = minMaxLocResult.maxLoc.x;
                curY = minMaxLocResult.maxLoc.y;
                Imgproc.circle(mat2,minMaxLocResult.maxLoc,5,new Scalar(255,255,255));
            }
        }

        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".png",mat2,matOfByte);
        Image outRgb2 = new Image(new ByteArrayInputStream(matOfByte.toArray()));
//        System.out.println(outRgb2);

        imageView_origin.setImage(img);
        imageView_transformed.setImage(outRgb2);
    }

    public static void main(String[] args) throws InterruptedException {
        launch(args);
    }
}
