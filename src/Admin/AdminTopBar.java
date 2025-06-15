package src.Admin;

import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import src.auth.Logout;
public class AdminTopBar extends Logout {

    @FXML
    private Text headerText;
    private Stage primaryStage;
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    public void setTitle(String title) {
        if (headerText != null) {
            headerText.setText(title);
        }
    }
}