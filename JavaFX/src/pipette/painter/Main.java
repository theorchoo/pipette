package pipette.painter;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.stage.WindowEvent;

import org.opencv.core.Core;
import pipette.Tracker.KinectTracker;
import pipette.server.PipetteServer;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

public class Main extends Application {
    static Controller controller;
    PipetteServer server;
    KinectTracker kinectTracker;
    Scene menuScene;
    Stage stage;
    private ArrayList<Scene> paintingScenes = new ArrayList<>();
    private ArrayList<File> files = new ArrayList<>();
    Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();

    private void parsePaintFolder() throws Exception {
        File folder = new File("/Users/ordagan/Documents/pipette/JavaFX/src/pipette/painter/paints");
        File[] paintings = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().substring(pathname.getName().lastIndexOf('.')).equals(".fxml");
            }
        });
        for (File file : paintings) {
            Parent parent = FXMLLoader.load(getClass().getResource("paints/" + file.getName()));
            parent.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    fill(event.getX(), event.getY(), Color.valueOf("#ff0000"));
                }
            });
            Scene s = new Scene(parent,screenSize.getWidth(), screenSize.getHeight());
            s.setOnKeyPressed(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    if (event.getCode() == KeyCode.R) {
                        controller.reset();
                    }
                }
            });

            paintingScenes.add(s);
            files.add(file);
        }
    }

    private Scene getMenuScene() {
        BorderPane pane = new BorderPane();
        HBox hBox = new HBox();
        hBox.getStyleClass().add("topBar");
        Text headline = new Text("Choose Painting:");
        headline.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        headline.getStyleClass().add("topBarHeadline");
        hBox.getChildren().addAll(headline);
        pane.setTop(hBox);

        FlowPane flow = new FlowPane();
        flow.setVgap(4);
        flow.setHgap(4);
        flow.setStyle("-fx-background-color: FFFFFF;");

        for (int i = 0; i < files.size(); i++) {
            final int j = i;
            Button button = new Button(files.get(i).getName().substring(0,files.get(i).getName().lastIndexOf('.')));
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    switchToScene(j);
                }
            });
            flow.getChildren().add(button);
        }

        pane.setCenter(flow);
        return new Scene(pane,screenSize.getWidth(), screenSize.getHeight());
    }

    public void switchToScene(int index) {
         stage.setScene(paintingScenes.get(index));
         stage.getScene().getStylesheets().add("/Users/ordagan/Documents/pipette/JavaFX/src/pipette/painter/style.css");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        server = new PipetteServer(this);
        server.startServer(7090);

        Stage kinectStage = new Stage();
        kinectTracker = new KinectTracker();
        kinectTracker.start(kinectStage);

        stage = primaryStage;
        // stage.setMaximized(true);

        parsePaintFolder();
        menuScene = getMenuScene();

        stage.setTitle("Pipette");
        stage.setScene(menuScene);
        stage.getScene().getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                server.stop();
            }
        });

        stage.show();
    }

    public void pipetteAction(int r, int g, int b) {
        System.out.println("fill");
        System.out.println(r);
        System.out.println(g);
        System.out.println(b);
        Color color = Color.rgb(r,g,b);
        System.out.println(color.getRed() + " ; " + color.getGreen() + " ; " + color.getBlue());

        double x = kinectTracker.getX();
        double y = kinectTracker.getY();
        fill(x,y,color);
    }

    public void pipetteButton() {
        System.out.println("button pressed");
    }

    public void fill(double x, double y, Color rgbColor) {
        System.out.println(x + "//" + y);
        controller.fill(x,y, rgbColor);
    }

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
    }
}
