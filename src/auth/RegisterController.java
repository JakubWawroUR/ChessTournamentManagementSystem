package src.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import src.dao.UserDAO;
import src.model.Role;
import src.model.User;

import java.io.IOException;
import java.sql.SQLException;

public class RegisterController implements SceneSwitcher {
    @FXML
    private ToggleButton RegisterButton;
    @FXML
    private TextField firstname;
    @FXML
    private TextField lastname;
    @FXML
    private TextField login;
    @FXML
    private PasswordField password1;
    @FXML
    private PasswordField password2;
    public void userRegister(ActionEvent event) throws IOException {
        handleRegister(event);
    }
    private void handleRegister(ActionEvent event) throws IOException {
        if (!password1.getText().equals(password2.getText())) {
            System.out.println("Hasła nie są identyczne!");
            return;
        }

        User newUser = new User(
                0,
                login.getText(),
                password1.getText(),
                firstname.getText(),
                lastname.getText(),
                Role.GRACZ
        );

        try {
            userDAO.addUser(newUser);
            System.out.println("Użytkownik zarejestrowany pomyślnie!");
            // Po rejestracji przełącz na scenę logowania
            switchScene(event);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            System.out.println("Błąd podczas rejestracji użytkownika");
        }
    }
    private UserDAO userDAO = new UserDAO();
    @Override
    public void switchScene(ActionEvent event) throws IOException {
        System.out.println("dziala");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

}
