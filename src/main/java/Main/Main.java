package Main;

import common.AuthorPackage;
import common.LibrarianPackage;
import common.MemberPackage;
import common.SocketWrapper;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    public static SocketWrapper socketWrapper;
    private static MemberPackage memberPackage;
    public static LibrarianPackage librarianPackage;
    public static AuthorPackage authorPackage;

    public static String recieveUserID;
    public static AuthorPackage getAuthorPackage() {
        return authorPackage;
    }

    public static void setAuthorPackage(AuthorPackage authorPackage) {
        Main.authorPackage = authorPackage;
    }

    private static LibrarianController librarianController;
    private static MemberController memberController;
    private static AuthorController authorController;

    public static AuthorController getAuthorController() {
        return authorController;
    }

    public static void setAuthorController(AuthorController controller) {
        Main.authorController = controller;
    }

    public static void setMemberPackage(MemberPackage memberPackage) {
        Main.memberPackage = memberPackage;
    }

    public static MemberPackage getMemberPackage() {
        return memberPackage;
    }

    public static LibrarianPackage getLibrarianPackage() {
        return librarianPackage;
    }

    public static void setLibrarianPackage(LibrarianPackage librarianPackage) {
        Main.librarianPackage = librarianPackage;
    }

    public static LibrarianController getLibrarianController() {
        return librarianController;
    }

    public static void setLibrarianController(LibrarianController controller) {
        Main.librarianController = controller;
    }

    public static MemberController getMemberController() {
        return memberController;
    }

    public static void setMemberController(MemberController controller) {
        Main.memberController = controller;
    }

    public Main(){
        // Default constructor
    }

    public Main(String serverAddress, int serverPort) {
        try {
            socketWrapper = new SocketWrapper(serverAddress, serverPort);
            new ReadThreadClient(socketWrapper);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static SocketWrapper getSocketWrapper() {
        return socketWrapper;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        String serverAddress = "localhost";
        int serverPort = 44444;
        new Main(serverAddress, serverPort);

        SceneManager.setStage(primaryStage);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Main/login-view.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Library Management System");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) throws IOException {
        launch(args);
    }
}