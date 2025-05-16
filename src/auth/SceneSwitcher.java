package src.auth;


import javafx.event.ActionEvent;
import java.io.IOException;

public interface SceneSwitcher {
    void switchScene(ActionEvent event) throws IOException;
}
