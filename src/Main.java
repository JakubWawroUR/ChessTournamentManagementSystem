package src;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene; // Dodaj import dla Scene
import javafx.stage.Stage;
import src.dao.UserDAO;
import src.model.User;

import java.io.IOException;
import java.sql.SQLException; // Już dodałeś ten import, super!
import java.util.List;

public class Main extends Application {
    private static Stage stg; // Static stage reference
    @Override
    public void start(Stage primaryStage) throws IOException {
        stg = primaryStage; // Assign primaryStage to static stg

        primaryStage.setResizable(false);

        Parent root = FXMLLoader.load(getClass().getResource("auth/LoginController.fxml"));
        Scene scene = new Scene(root, 600, 400); // <-- TUTAJ DEKLARUJESZ ZMIENNĄ SCENE

        // Pamiętaj, aby ścieżka do pliku CSS była poprawna.
        // `/src/assets/styles/style.css` może oznaczać, że `src` jest w classpath.
        // Jeśli plik jest w `src/main/resources/assets/styles/style.css`, to ścieżka to `/assets/styles/style.css`
        scene.getStylesheets().add(getClass().getResource("/src/assets/styles/style.css").toExternalForm()); // <-- Używasz zadeklarowanej scene

        primaryStage.setTitle("Szachy - JavaFX");
        primaryStage.setScene(scene); // <-- Używasz zadeklarowanej scene
        primaryStage.show();
    }
    public static void main(String[] args) {
        UserDAO userDAO = new UserDAO();
        try { // Blok try-catch jest poprawnie dodany!
            List<User> users = userDAO.getAllUsers(); // Linia 26 (lub jej odpowiednik po zmianach)
            for (User user : users) {
                System.out.println("Użytkownik: " + user.getFirstName() + " " + user.getLastName() + " " + user.getPassword() + " " + user.getRole());
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas pobierania użytkowników z bazy danych: " + e.getMessage());
            e.printStackTrace();
        }
        launch(args);
    }
}