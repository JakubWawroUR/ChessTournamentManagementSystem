package src;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;

public class Main extends Application {
    private static Stage stg;
    @Override
    public void start(Stage primaryStage) throws IOException {
        stg = primaryStage;
        primaryStage.setResizable(false);
        Image icon = new Image(getClass().getResourceAsStream("/src/assets/icons/icons8-chess-100.png"));
        primaryStage.getIcons().add(icon);
        Parent root = FXMLLoader.load(getClass().getResource("auth/LoginController.fxml"));
        Scene scene = new Scene(root, 600, 400);
        scene.getStylesheets().add(getClass().getResource("/src/assets/styles/style.css").toExternalForm());
        primaryStage.setTitle("System do zarządzania Turniejami Szachowymi");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}