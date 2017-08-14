package pipette.painter;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;

/**
 * Created by ordagan on 9.7.2016.
 */
public class testing extends Application {
    Polygon polygon;

    public void start(Stage primaryStage) throws Exception {
        StackPane pane = new StackPane();
        polygon = new Polygon();



        primaryStage.setScene(new Scene(pane, 800, 600));
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }

}
