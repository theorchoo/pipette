package pipette.painter;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.util.Duration;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML //  fx:id="myButton"
    public Group root; // Value injected by FXMLLoader

    @FXML
    private Group svgGroup;

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert root != null : "fx:id=\"root\" was not injected: check your FXML file 'simple.fxml'.";
        Main.controller = this;
    }

    void fill(double x, double y, Color color) {
        for (Node node : svgGroup.getChildren()) {
            if (node.contains(x,y)) {
                if (!Shape.class.isInstance(node)) {
                    return;
                }
                Shape shape = (Shape) node;
                Color previousColor = (Color) shape.getFill();

                shape.setFill(color);

                SVGPath path = new SVGPath();
                path.setContent("M335.412,281.412c0,0,21.176-32.941,54.118-60\n" +
                        "\tc38.676-31.77,83.529-29.412,109.412-23.529c25.882,5.882,77.647,16.471,107.059,5.882c29.412-10.588,87.059-3.529,125.882,42.353\n" +
                        "\tc38.824,45.882,44.706,69.412,44.706,92.941c0,23.529-22.353,69.412-18.824,77.647s28.235,48.235,31.765,83.529\n" +
                        "\tS787.176,587.294,747.176,612s-174.118,80-236.471,65.882c-62.353-14.118-118.824-58.823-110.588-95.294\n" +
                        "\tS366,536.706,361.294,519.059c-4.706-17.647,11.765-50.588-16.471-83.529C316.588,402.588,303.647,328.471,335.412,281.412z");
                path.setScaleX((shape.getLayoutBounds().getWidth()/path.getLayoutBounds().getWidth())/10);
                path.setScaleY((shape.getLayoutBounds().getHeight()/path.getLayoutBounds().getHeight())/10);
                path.setFill(color);
                double xtrans = svgGroup.getLayoutBounds().getMinX();
                double ytrans = svgGroup.getLayoutBounds().getMinY();

                path.setLayoutX(x-xtrans-530);
                path.setLayoutY(y-ytrans-410);
                svgGroup.getChildren().remove(shape);
                Pane pane = new Pane(path);
                pane.setClip(shape);
                pane.setStyle("-fx-background-color:" + toRGBCode(previousColor) + " ;");
                svgGroup.getChildren().add(pane);


                Timeline timeline = new Timeline();
                timeline.setCycleCount(1);
                timeline.setAutoReverse(false);

                KeyValue keyValueX = new KeyValue(path.scaleXProperty(), (shape.getLayoutBounds().getWidth
                        ()/path.getLayoutBounds().getWidth())*3);
                KeyValue keyValueY = new KeyValue(path.scaleYProperty(), (shape.getLayoutBounds().getHeight
                        ()/path.getLayoutBounds().getHeight())*3);
                KeyValue keyRotate = new KeyValue(path.rotateProperty(), 20);

                Duration duration = new javafx.util.Duration(2000);

                EventHandler<ActionEvent> onFinished = new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent t) {
                        svgGroup.getChildren().remove(pane);
                        pane.setClip(null);
                        svgGroup.getChildren().add(shape);
                    }
                };

                KeyFrame keyFrame = new KeyFrame(duration, onFinished, keyRotate, keyValueX, keyValueY);
                timeline.getKeyFrames().add(keyFrame);
                timeline.play();

                return;
            }
        }

    }

    public void reset() {
        for (Node node : svgGroup.getChildren()) {
            if (!Shape.class.isInstance(node)) {
                continue;
            }
            Shape shape = (Shape) node;
            Color previousColor = (Color) shape.getFill();
            if (previousColor != Color.BLACK) {
                shape.setFill(Color.WHITE);
            }
        }
    }

    public static String toRGBCode( Color color )
    {
        return String.format( "#%02X%02X%02X",
                (int)( color.getRed() * 255 ),
                (int)( color.getGreen() * 255 ),
                (int)( color.getBlue() * 255 ) );
    }

}
