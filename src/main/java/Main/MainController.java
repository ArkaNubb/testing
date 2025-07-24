package Main;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {
    @FXML
    private Stage stage;
    private Scene scene;
    private Parent root;
    @FXML private TextField textField;
    @FXML private PasswordField passwordField;
    @FXML private BorderPane borderPane;
    private Text text;
    public void MemberLogin(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("memberLoginPage.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
//        borderPane.setAccessibleText("kire");
        text = new Text("2305168");
    }

}
