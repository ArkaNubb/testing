package Main;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import user.user;

import java.io.IOException;

public class SceneManager {

    private static Stage stage;
    private static user currentUser; // Store the logged-in user globally

    public static void setStage(Stage stage) {
        SceneManager.stage = stage;
    }

    public static void setCurrentUser(user user) {
        currentUser = user;
    }

    public static user getCurrentUser() {
        return currentUser;
    }

    public static void switchScene(String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlFile));
        Parent parent = loader.load();
        stage.getScene().setRoot(parent);
    }
}