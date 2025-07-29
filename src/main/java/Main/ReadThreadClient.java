package Main;

import common.AuthorPackage;
import common.LibrarianPackage;
import common.MemberPackage;
import common.SocketWrapper;
import javafx.application.Platform;

import java.io.IOException;

public class ReadThreadClient implements Runnable {
    private final Thread thr;
    private final SocketWrapper socketWrapper;

    public ReadThreadClient(SocketWrapper socketWrapper) {
        this.socketWrapper = socketWrapper;
        this.thr = new Thread(this);
        thr.start();
    }

    public void run() {
        try {
            while (true) {
                Object obj = socketWrapper.read();

                if (obj instanceof MemberPackage) {
                    System.out.println("=== MemberPackage received from server ===");
                    MemberPackage pkgg = (MemberPackage) obj;

                    Main.setMemberPackage(pkgg);

                    // Trigger UI update on JavaFX Application Thread
                    MemberController controllerr = Main.getMemberController();
                    if (controllerr != null) {
                        System.out.println("Triggering real-time Member UI update...");
                        Platform.runLater(() -> {
                            controllerr.loadData();
                        });
                    } else {
                        System.out.println("MemberController is null");
                    }
                }

                if (obj instanceof AuthorPackage) {
                    System.out.println("AuthorPackage received from server");
                    AuthorPackage pkgg = (AuthorPackage) obj;

                    Main.setAuthorPackage(pkgg);

                    // Trigger UI update on JavaFX Application Thread
                    AuthorController authorController = Main.getAuthorController();
                    if (authorController != null) {
                        System.out.println("Author UI updating...");
                        Platform.runLater(() -> {
                            authorController.loadData();
                        });
                    } else {
                        System.out.println("AuthorController is null");
                    }
                }
                if(obj instanceof String){
                    Main.recieveUserID = (String)obj;
                }

                if (obj instanceof LibrarianPackage) {
                    System.out.println("LibrarianPackage received from server");
                    LibrarianPackage pkg = (LibrarianPackage) obj;
                    Main.setLibrarianPackage(pkg);

                    // Trigger UI update on JavaFX Application Thread
                    LibrarianController controller = Main.getLibrarianController();
                    if (controller != null) {
                        System.out.println("Triggering real-time UI update...");
                        Platform.runLater(() -> {
                            controller.loadData();
                        });
                    } else {
                        System.out.println("LibrarianController is null - cannot update UI");
                    }
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}