package Main;

import book.Book;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import service.server;
import user.Author;
import user.Member;
import user.UserRole;

import java.io.IOException;
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

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("All fields are required.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            if (role == UserRole.MEMBER) {
                String userId = server.memberService.generateMemberUserId();
                Member member = new Member(email, userId, password, name, new ArrayList<>());
                server.memberService.addMember(member);
                messageLabel.setText("Member account created! Your User ID is: " + userId);
                messageLabel.setStyle("-fx-text-fill: green;");
            } else if (role == UserRole.AUTHOR) {
                String userId = server.authorService.genetateAuthorId();
                Author author = new Author(email, userId, password, name, new ArrayList<>());
                server.authorService.addAuthor(author);
                messageLabel.setText("Author account created! Your User ID is: " + userId);
                messageLabel.setStyle("-fx-text-fill: green;");
            }
        } catch (IOException e) {
            messageLabel.setText("Error saving user data: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    @FXML
    void handleBackToLogin(ActionEvent event) throws IOException {
        SceneManager.switchScene("/Main/login-view.fxml");
    }
}