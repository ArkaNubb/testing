package Main;

import common.*;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import service.AuthorService;
import service.server;

import java.io.IOException;

public class LoginController {

    @FXML private ChoiceBox<UserRole> roleChoiceBox;
    @FXML private TextField userIdField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    @FXML private HBox loadingBox;

    @FXML
    public void initialize() {
        setupRoleChoiceBox();
        setupInputValidation();
        setupButtonEffects();
        clearErrorMessage();
    }

    private void setupRoleChoiceBox() {
        roleChoiceBox.setItems(FXCollections.observableArrayList(UserRole.values()));
        roleChoiceBox.setValue(UserRole.MEMBER); // Default selection

        // Add custom string converter for better display
        roleChoiceBox.setConverter(new javafx.util.StringConverter<UserRole>() {
            @Override
            public String toString(UserRole role) {
                if (role == null) return "";
                switch (role) {
                    case MEMBER: return "📖 Library Member";
                    case AUTHOR: return "✍️ Author";
                    case LIBRARIAN: return "👨‍💼 Librarian";
                    default: return role.toString();
                }
            }

            @Override
            public UserRole fromString(String string) {
                // This won't be used in ChoiceBox, but required by interface
                return null;
            }
        });
    }

    private void setupInputValidation() {
        // Add focus listeners for real-time validation
        userIdField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // Lost focus
                if (userIdField.getText().trim().isEmpty()) {
                    setFieldError(userIdField, true);
                } else {
                    setFieldError(userIdField, false);
                }
            }
        });

        passwordField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // Lost focus
                if (passwordField.getText().trim().isEmpty()) {
                    setFieldError(passwordField, true);
                } else {
                    setFieldError(passwordField, false);
                }
            }
        });

        // Clear error when user starts typing
        userIdField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.trim().isEmpty()) {
                setFieldError(userIdField, false);
                clearErrorMessage();
            }
        });

        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.trim().isEmpty()) {
                setFieldError(passwordField, false);
                clearErrorMessage();
            }
        });
    }

    private void setupButtonEffects() {
        // Add hover effects
        loginButton.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), loginButton);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });

        loginButton.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), loginButton);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }

    private void setFieldError(Control field, boolean hasError) {
        if (hasError) {
            field.setStyle(field.getStyle().replaceAll("-fx-border-color: [^;]*;", "") +
                    "-fx-border-color: #e74c3c; -fx-border-width: 2px; -fx-effect: dropshadow(three-pass-box, rgba(231, 76, 60, 0.3), 5, 0, 0, 2);");
        } else {
            field.setStyle(field.getStyle().replaceAll("-fx-border-color: [^;]*;", "").replaceAll("-fx-effect: [^;]*;", "") +
                    "-fx-border-color: #3498db; -fx-effect: dropshadow(three-pass-box, rgba(52, 152, 219, 0.2), 5, 0, 0, 2);");
        }
    }

    private void showErrorMessage(String message) {
        errorLabel.setText(message);

        // Fade in animation
        FadeTransition ft = new FadeTransition(Duration.millis(300), errorLabel);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    private void clearErrorMessage() {
        errorLabel.setText("");
    }

    private void setLoadingState(boolean loading) {
        Platform.runLater(() -> {
            loginButton.setDisable(loading);
            loadingBox.setVisible(loading);

            if (loading) {
                loginButton.setText("🔄 Signing In...");
                loginButton.setStyle(loginButton.getStyle() + "-fx-opacity: 0.8; -fx-background-color: linear-gradient(45deg, #95a5a6, #7f8c8d);");
            } else {
                loginButton.setText("🔐 Sign In");
                loginButton.setStyle(loginButton.getStyle().replaceAll("-fx-opacity: [^;]*;", "").replaceAll("-fx-background-color: linear-gradient\\([^;]*\\);", "") +
                        "-fx-background-color: linear-gradient(45deg, #3498db, #2980b9);");
            }
        });
    }

    @FXML
    void handleLogin(ActionEvent event) {
        String userId = userIdField.getText().trim();
        String password = passwordField.getText().trim();
        UserRole role = roleChoiceBox.getValue();

        // Input validation
        boolean hasErrors = false;

        if (userId.isEmpty()) {
            setFieldError(userIdField, true);
            hasErrors = true;
        }

        if (password.isEmpty()) {
            setFieldError(passwordField, true);
            hasErrors = true;
        }

        if (hasErrors) {
            showErrorMessage("Please fill in all required fields.");
            return;
        }

        // Clear any previous errors
        setFieldError(userIdField, false);
        setFieldError(passwordField, false);
        clearErrorMessage();

        // Show loading state
        setLoadingState(true);

        // Create background task for login
        Task<Boolean> loginTask = new Task<Boolean>() {
            private String nextScene = "";
            private String errorMessage = "";

            @Override
            protected Boolean call() throws Exception {
                try {
                    SocketWrapper socketWrapper = Main.getSocketWrapper();
                    if (socketWrapper == null) {
                        errorMessage = "Unable to connect to server. Please try again later.";
                        return false;
                    }

                    switch (role) {
                        case MEMBER:
                            return authenticateMember(socketWrapper, userId, password);
                        case AUTHOR:
                            return authenticateAuthor(socketWrapper, userId, password);
                        case LIBRARIAN:
                            return authenticateLibrarian(socketWrapper, userId, password);
                        default:
                            errorMessage = "Invalid role selected.";
                            return false;
                    }
                } catch (Exception e) {
                    errorMessage = "Authentication failed. Please check your credentials.";
                    e.printStackTrace();
                    return false;
                }
            }

            private boolean authenticateMember(SocketWrapper socketWrapper, String userId, String password)
                    throws InterruptedException, IOException {
                Authenticate authenticate = new Authenticate(userId, password);
                Main.setMemberPackage(null);

                socketWrapper.write(authenticate);

                // Wait for response with timeout
                for (int i = 0; i < 100; i++) {
                    if (Main.getMemberPackage() != null) {
                        break;
                    }
                    Thread.sleep(50);
                }

                MemberPackage memberPackage = Main.getMemberPackage();
                Member member = null;
                if (memberPackage != null) member = memberPackage.getMember();

                if (member != null && member.getPassword().equals(password)) {
                    SceneManager.setCurrentUser(member);
                    nextScene = "/Main/member-view.fxml";
                    return true;
                } else {
                    errorMessage = "Invalid member credentials. Please check your User ID and password.";
                    return false;
                }
            }

            private boolean authenticateAuthor(SocketWrapper socketWrapper, String userId, String password)
                    throws InterruptedException, IOException {
                AuthorAuthenticate authorAuthenticate = new AuthorAuthenticate(userId, password);
                Main.setAuthorPackage(null);

                socketWrapper.write(authorAuthenticate);

                for (int i = 0; i < 100; i++) {
                    if (Main.getAuthorPackage() != null) {
                        break;
                    }
                    Thread.sleep(50);
                }

                AuthorPackage authorPackage = Main.getAuthorPackage();
                Author author = null;
                if (authorPackage != null) {
                    author = authorPackage.getAuthor();
                }

                if (author != null && author.getPassword().equals(password)) {
                    SceneManager.setCurrentUser(author);
                    nextScene = "/Main/author-view.fxml";
                    return true;
                } else {
                    errorMessage = "Invalid author credentials. Please check your User ID and password.";
                    return false;
                }
            }

            private boolean authenticateLibrarian(SocketWrapper socketWrapper, String userId, String password)
                    throws InterruptedException, IOException {
                LibrarianAuthenticate librarianAuthenticate = new LibrarianAuthenticate(userId, password);
                Main.setLibrarianPackage(null);

                socketWrapper.write(librarianAuthenticate);

                for (int i = 0; i < 100; i++) {
                    if (Main.getLibrarianPackage() != null) {
                        break;
                    }
                    Thread.sleep(50);
                }

                LibrarianPackage librarianPackage = Main.getLibrarianPackage();
                Librarian librarian = null;
                if (librarianPackage != null) {
                    librarian = librarianPackage.getLibrarian();
                }

                if (librarian != null && librarian.getUserId().equals(userId) &&
                        librarian.getPassword().equals(password)) {
                    SceneManager.setCurrentUser(librarian);
                    nextScene = "/Main/librarian-view.fxml";
                    return true;
                } else {
                    errorMessage = "Invalid librarian credentials. Please check your User ID and password.";
                    return false;
                }
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    setLoadingState(false);
                    if (getValue()) {
                        try {
                            // Add success animation before scene switch
                            FadeTransition ft = new FadeTransition(Duration.millis(200), loginButton);
                            ft.setFromValue(1.0);
                            ft.setToValue(0.0);
                            ft.setOnFinished(e -> {
                                try {
                                    SceneManager.switchScene(nextScene);
                                } catch (IOException ex) {
                                    showErrorMessage("Error loading the application. Please try again.");
                                    ex.printStackTrace();
                                }
                            });
                            ft.play();
                        } catch (Exception e) {
                            showErrorMessage("Error loading the application. Please try again.");
                            e.printStackTrace();
                        }
                    } else {
                        showErrorMessage(errorMessage);
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    setLoadingState(false);
                    showErrorMessage("Authentication failed. Please try again.");
                });
            }
        };

        // Run login task in background thread
        Thread loginThread = new Thread(loginTask);
        loginThread.setDaemon(true);
        loginThread.start();
    }

    @FXML
    void handleSignupLink(ActionEvent event) {
        try {
            // Add transition effect
            FadeTransition ft = new FadeTransition(Duration.millis(200), loginButton.getParent());
            ft.setFromValue(1.0);
            ft.setToValue(0.0);
            ft.setOnFinished(e -> {
                try {
                    SceneManager.switchScene("/Main/signup-view.fxml");
                } catch (IOException ex) {
                    showErrorMessage("Error loading signup page.");
                    ex.printStackTrace();
                }
            });
            ft.play();
        } catch (Exception e) {
            showErrorMessage("Error loading signup page.");
            e.printStackTrace();
        }
    }
}