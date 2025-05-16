package src;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import src.dao.UserDAO;
import src.model.User;

import java.io.IOException;
import java.util.List;

public class Main extends Application {
    private static Stage stg;
    @Override
    public void start(Stage primaryStage) throws IOException {
        stg = primaryStage;
        primaryStage.setResizable(false);
        Parent root = FXMLLoader.load(getClass().getResource("auth/main.fxml"));
        primaryStage.setTitle("Szachy - JavaFX");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();
    }
    public void changeScene(String fxml) throws IOException {
        Parent pane = FXMLLoader.load(getClass().getResource(fxml));
        stg.getScene().setRoot(pane);
    }

    public static void main(String[] args) {
        UserDAO userDAO = new UserDAO();
        List<User> users = userDAO.getAllUsers();

        for (User user : users) {
            System.out.println(user.getFirstname() + " " + user.getLastname() + " " + user.getPassword() + " " + user.getRole());
        }
        launch(args);
    }
}