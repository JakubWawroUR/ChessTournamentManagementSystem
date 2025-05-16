package src.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import src.dao.UserDAO;
import java.io.IOException;

public class LoginController implements SceneSwitcher{
    UserDAO userDAO = new UserDAO();
    @FXML
    private TextField login;
    @FXML
    private ToggleButton registerButton;
    @FXML
    private PasswordField password;

    @FXML
    public void switchScene(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("RegisterController.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) login.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    public void userLogin(ActionEvent event) throws IOException {
        checkLogin();
    }

    private void checkLogin() throws IOException {
        if (userDAO.checkUserExists(login.getText(), password.getText())) {
            System.out.println("Działa");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("afterLogin.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) login.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } else {
            System.out.println("Nie działa");
        }
    }
}
