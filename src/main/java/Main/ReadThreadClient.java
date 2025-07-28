package Main;

import common.LibrarianPackage;
import common.MemberPackage;
import common.SocketWrapper;

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
                    Main.setMemberPackage((MemberPackage) obj);
                    // NOTE: You would do a similar update for the MemberController here
                }

                if (obj instanceof LibrarianPackage) {
                    System.out.println("LibrarianPackage received from server.");
                    Main.setLibrarianPackage((LibrarianPackage) obj);

                    // --- MODIFICATION: Trigger the UI update ---
                    LibrarianController controller = Main.getLibrarianController();
                    if (controller != null) {
                        controller.loadData();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("ReadThreadClient Error: " + e);
            // Proper error handling/logging should be here
        }
    }
}