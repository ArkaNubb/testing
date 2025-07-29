package Main;

import common.*;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import service.AuthorService;
import service.server;

import java.io.IOException;

public class LoginController {

    @FXML private ChoiceBox<UserRole> roleChoiceBox;
    @FXML private TextField userIdField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        roleChoiceBox.setItems(FXCollections.observableArrayList(UserRole.values()));
        roleChoiceBox.setValue(UserRole.MEMBER); // Default selection
    }

    @FXML
    void handleLogin(ActionEvent event) {
        String userId = userIdField.getText();
        String password = passwordField.getText();
        UserRole role = roleChoiceBox.getValue();

        if (userId.isEmpty() || password.isEmpty()) {
            errorLabel.setText("User ID and Password cannot be empty.");
            return;
        }

        try {
            boolean loginSuccess = false;
            String nextScene = "";
            SocketWrapper socketWrapper;
            switch (role) {
                case MEMBER:
//                    Member member = MemberService.isMemberFound(userId);
                    socketWrapper = Main.getSocketWrapper();
                    if (socketWrapper == null) {
                        errorLabel.setText("Connection to server not established.");
                        return;
                    }

                    Authenticate authenticate = new Authenticate(userId, password);
                    Main.setMemberPackage(null); // Clear previous login data

                    // Write to the server directly. No new thread needed for this.
                    socketWrapper.write(authenticate);
                    for (int i = 0; i < 100; i++) {
                        if (Main.getMemberPackage() != null) {
                            break;
                        }
                        Thread.sleep(50); // Pause briefly to allow the reader thread to work
                    }

                    MemberPackage memberPackage = Main.getMemberPackage();
                    Member member = null;
                    if(memberPackage!= null) member = memberPackage.getMember();
                    if (member != null && member.getPassword().equals(password)) {
                        SceneManager.setCurrentUser(member);
                        loginSuccess = true;
                        nextScene = "/Main/member-view.fxml";
                    }
                    break;
                case AUTHOR:
//                    Author author = AuthorService.isAuthorFound(userId);
                    socketWrapper = Main.getSocketWrapper();
                    if (socketWrapper == null) {
                        errorLabel.setText("Connection to server not established.");
                        return;
                    }
                    AuthorAuthenticate authorAuthenticate = new AuthorAuthenticate(userId, password);
                    Main.setAuthorPackage(null);

                    socketWrapper.write(authorAuthenticate);
                    for (int i = 0; i < 100; i++) {
                        if (Main.getAuthorPackage() != null) {
                            System.out.println("AuthorPackage received");
                            break;
                        }
                        Thread.sleep(50); // Pause briefly to allow the reader thread to work
                    }

                    AuthorPackage authorPackage = Main.getAuthorPackage();
                    Author author = null;
                    if(authorPackage != null) {
                        author = authorPackage.getAuthor();
                    }

                    if (author != null && author.getPassword().equals(password)) {
                        SceneManager.setCurrentUser(author);
                        loginSuccess = true;
                        nextScene = "/Main/author-view.fxml";
                    }
                    break;
                case LIBRARIAN:
                    socketWrapper = Main.getSocketWrapper();
                    if (socketWrapper == null) {
                        errorLabel.setText("Connection to server not established.");
                        return;
                    }

                    LibrarianAuthenticate librarianAuthenticate = new LibrarianAuthenticate(userId, password);
                    Main.setLibrarianPackage(null);

                    // Write to the server directly. No new thread needed for this.
                    socketWrapper.write(librarianAuthenticate);
                    for (int i = 0; i < 100; i++) {
                        if (Main.getLibrarianPackage() != null) {
                            System.out.println("LibrarianPackage received");
                            break;
                        }
                        Thread.sleep(50); // Pause briefly to allow the reader thread to work
                    }

                    LibrarianPackage librarianPackage = Main.getLibrarianPackage();
                    Librarian librarian = null;
                    if(librarianPackage != null) {
                        librarian = librarianPackage.getLibrarian();
                    }

                    // Add proper null checking here
                    if (librarian != null) {
                        System.out.println("Librarian found: " + librarian.getName());
                        if (librarian.getUserId().equals(userId) && librarian.getPassword().equals(password)) {
                            SceneManager.setCurrentUser(librarian);
                            loginSuccess = true;
                            nextScene = "/Main/librarian-view.fxml";

                            // ADD THIS: Ensure the UI will be refreshed when the scene loads
                            // The LibrarianController.initialize() will handle setting up the controller reference
                        } else {
                            System.out.println("Invalid credentials for librarian");
                        }
                    } else {
                        System.out.println("Librarian is null - authentication failed");
                        errorLabel.setText("Authentication failed. Please check your credentials.");
                    }
                    break;
            }

            if (loginSuccess) {
                SceneManager.switchScene(nextScene);
            } else {
                errorLabel.setText("Invalid credentials for selected role.");
            }

        } catch (IOException e) {
            errorLabel.setText("Error loading the application view.");
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void handleSignupLink(ActionEvent event) throws IOException {
        SceneManager.switchScene("/Main/signup-view.fxml");
    }
}