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
                path.setContent("M204,162.833c16.295-1.59-11.667-50,3.333-67.5  s60-34.167,94.167,3.333S394,92.833,413.167,87c19.167-5.833,60-31.667,95.833,4.167s19.167,55.833,20,90  s58.333,63.333,56.667,89.167C584,296.167,614.833,339.5,564,338.667c-50.833-0.833-159.167-10.833-137.5,20  s34.167,107.5,10.833,105c-23.333-2.5-162.5,30-163.333-54.167s-24.167-65.833-32.5-60.833s-99.167,62.5-79.167-20.833  S129,237,129,237S33.167,179.5,204,162.833z");
                path.setScaleX((shape.getLayoutBounds().getWidth()/path.getLayoutBounds().getWidth())/4);
                path.setScaleY((shape.getLayoutBounds().getHeight()/path.getLayoutBounds().getHeight())/4);
                path.setFill(color);
                double xtrans = svgGroup.getLayoutBounds().getMinX();
                double ytrans = svgGroup.getLayoutBounds().getMinY();

                path.setLayoutX(x-xtrans-130);
                path.setLayoutY(y-ytrans-200);
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

    public static String toRGBCode( Color color )
    {
        return String.format( "#%02X%02X%02X",
                (int)( color.getRed() * 255 ),
                (int)( color.getGreen() * 255 ),
                (int)( color.getBlue() * 255 ) );
    }

}
