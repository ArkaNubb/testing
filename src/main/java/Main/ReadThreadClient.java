package Main;

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

                    // Debug the package contents
                    int allBooksCount = 0;
                    int borrowedBooksCount = 0;

                    if (pkgg.getAllBooks() != null) {
                        allBooksCount = pkgg.getAllBooks().size();
                    }

                    if (pkgg.getMember() != null && pkgg.getMember().getBorrowedBooks() != null) {
                        borrowedBooksCount = pkgg.getMember().getBorrowedBooks().size();
                    }

                    System.out.println("Package contains - All Books: " + allBooksCount +
                            ", Borrowed Books: " + borrowedBooksCount);

                    Main.setMemberPackage(pkgg);

                    // Trigger UI update on JavaFX Application Thread
                    MemberController controllerr = Main.getMemberController();
                    if (controllerr != null) {
                        System.out.println("Triggering real-time Member UI update...");
                        Platform.runLater(() -> {
                            controllerr.loadData();
                        });
                    } else {
                        System.out.println("INFO: MemberController is null - no real-time update needed (probably initial login)");
                    }
                }
                if (obj instanceof LibrarianPackage) {
                    System.out.println("=== LibrarianPackage received from server ===");
                    LibrarianPackage pkg = (LibrarianPackage) obj;

                    // Debug the package contents
                    int issueCount = 0;
                    int returnCount = 0;
                    int publishCount = 0;

                    if (pkg.pendingIssuingBook != null) {
                        for (var entry : pkg.pendingIssuingBook.entrySet()) {
                            issueCount += entry.getValue().size();
                        }
                    }

                    if (pkg.pendingReturnedBook != null) {
                        for (var entry : pkg.pendingReturnedBook.entrySet()) {
                            returnCount += entry.getValue().size();
                        }
                    }

                    if (pkg.pendingPublishRequests != null) {
                        for (var entry : pkg.pendingPublishRequests.entrySet()) {
                            publishCount += entry.getValue().size();
                        }
                    }

                    System.out.println("Package contains - Issues: " + issueCount +
                            ", Returns: " + returnCount +
                            ", Publishes: " + publishCount);

                    Main.setLibrarianPackage(pkg);

                    // Trigger UI update on JavaFX Application Thread
                    LibrarianController controller = Main.getLibrarianController();
                    if (controller != null) {
                        System.out.println("Triggering real-time UI update...");
                        Platform.runLater(() -> {
                            controller.loadData();
                        });
                    } else {
                        System.out.println("WARNING: LibrarianController is null - cannot update UI!");
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