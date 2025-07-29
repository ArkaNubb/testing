package Main;

import common.SocketWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import service.server;
import common.Author;
import common.Member;
import common.UserRole;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class SignupController {

    @FXML private ChoiceBox<UserRole> roleChoiceBox;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    @FXML
    public void initialize() {
        // Exclude Librarian from signup options as there is only one.
        roleChoiceBox.setItems(FXCollections.observableArrayList(UserRole.MEMBER, UserRole.AUTHOR));
        roleChoiceBox.setValue(UserRole.MEMBER);
    }

    @FXML
    void handleSignup(ActionEvent event) {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        UserRole role = roleChoiceBox.getValue();
        SocketWrapper socketWrapper;
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("All fields are required.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            if (role == UserRole.MEMBER) {
                Member member = new Member(email, "-1", password, name, new ArrayList<>());
//                server.memberService.addMember(member);
                socketWrapper = Main.getSocketWrapper();
                socketWrapper.write(member);

                for (int i = 0; i < 100; i++) {
                    if (Main.recieveUserID != null) {
                        break;
                    }
                    Thread.sleep(50); // Pause briefly to allow the reader thread to work
                }

                messageLabel.setText("Member account created! Your User ID is: " + Main.recieveUserID);
                messageLabel.setStyle("-fx-text-fill: green;");
            } else if (role == UserRole.AUTHOR) {
                Author author = new Author(email, "-1", password, name, new ArrayList<>());
//                server.authorService.addAuthor(author);
                socketWrapper = Main.getSocketWrapper();
                socketWrapper.write(author);

                for (int i = 0; i < 100; i++) {
                    if (Main.recieveUserID != null) {
                        break;
                    }
                    Thread.sleep(50); // Pause briefly to allow the reader thread to work
                }

                messageLabel.setText("Author account created! Your User ID is: " + Main.recieveUserID);
                messageLabel.setStyle("-fx-text-fill: green;");
            }
        } catch (IOException e) {
            messageLabel.setText("Error saving user data: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void handleBackToLogin(ActionEvent event) throws IOException {
        SceneManager.switchScene("/Main/login-view.fxml");
    }
}