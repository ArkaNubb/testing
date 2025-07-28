package Main;



import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import util.SocketWrapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main extends Application {
    public static SocketWrapper socketWrapper;
    private static MemberPackage memberPackage;

    public static void setMemberPackage(MemberPackage mp) {
        memberPackage = mp;
    }

    public static MemberPackage getMemberPackage() {
        return memberPackage;
    }

    public Main(){

    }
    public Main(String serverAddress, int serverPort) {
//        Scanner scanner = null;
        try {
//            System.out.print("Enter name of the client: ");
//            scanner = new Scanner(System.in);
//            String clientName = scanner.nextLine();

            socketWrapper = new SocketWrapper(serverAddress, serverPort);
//            System.out.println("connected");
//            socketWrapper.write(clientName);
            new ReadThreadClient(socketWrapper);
//            new WriteThreadClient(socketWrapper);
        } catch (Exception e) {
            System.out.println(e);
        } finally {
//            scanner.close();
        }
    }

    public static SocketWrapper getSocketWrapper() {
        return socketWrapper;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
//         The SceneManager will handle all navigation between different screens
        String serverAddress = "localhost";
        int serverPort = 44444;
        new Main(serverAddress, serverPort);

        SceneManager.setStage(primaryStage);

        // Load the initial login view
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