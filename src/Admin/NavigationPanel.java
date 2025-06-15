package src.Admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class NavigationPanel {

    @FXML
    private Text usersText;

    @FXML
    private Text tournamentsText;

    @FXML
    public void switchScene(MouseEvent event) throws IOException {
        System.out.println("dziala");
        Object source = event.getSource();
        String fxmlFile = null;

        if (source == usersText) {
            fxmlFile = "AdminUserController.fxml";
        } else if (source == tournamentsText) {
            fxmlFile = "AdminTournamentController.fxml";
        } else {
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

}
