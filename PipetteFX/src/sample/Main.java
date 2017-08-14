package sample;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.setTitle("Hello World!");
        Group root = new Group();
        primaryStage.setScene(new Scene(root, 500, 500, Color.BLACK));

        Circle circle = new Circle(250,250,100, Color.web("white"));
        Circle maskCircle = new Circle(250,250,100);
        root.getChildren().add(circle);

        ImageView imageView = new ImageView();
        Image img = new Image("trans.gif");
        imageView.setImage(img);
        imageView.setClip(maskCircle);

        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setHue(40);
        imageView.setEffect(colorAdjust);
        root.getChildren().add(imageView);
        primaryStage.show();
}


    public static void main(String[] args) {
        launch(args);
    }
}
