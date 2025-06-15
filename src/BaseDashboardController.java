package src;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import java.net.URL;
import java.util.ResourceBundle;
import src.model.User;

public abstract class BaseDashboardController implements Initializable {

    @FXML
    protected Label welcomeLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        if (welcomeLabel != null) {
            welcomeLabel.setText("Witaj!");
        }
    }

    public abstract void setLoggedInUser(User user);

    protected void setWelcomeMessage(String message) {
        if (welcomeLabel != null) {
            welcomeLabel.setText(message);
        }
    }
}