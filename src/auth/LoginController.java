package src.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import src.dao.UserDAO;
import java.io.IOException;

public class LoginController {
    UserDAO userDAO = new UserDAO();
    @FXML
    private TextField login;
    @FXML
    private PasswordField password;
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
