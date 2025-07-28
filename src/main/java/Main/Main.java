package Main;



import common.MemberPackage;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import util.SocketWrapper;

import java.io.IOException;

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