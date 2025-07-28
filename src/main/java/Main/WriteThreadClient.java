package Main;

import common.Authenticate;
import common.SocketWrapper;

public class WriteThreadClient implements Runnable {

    private Thread thr;
    private SocketWrapper socketWrapper;
    Authenticate authenticate;

    public WriteThreadClient(SocketWrapper socketWrapper, Authenticate authenticate) {
        this.socketWrapper = socketWrapper;
        this.authenticate = authenticate;
        this.thr = new Thread(this);
        thr.start();
    }

    public WriteThreadClient(SocketWrapper socketWrapper) {
        this.socketWrapper = socketWrapper;
        this.thr = new Thread(this);
        thr.start();
    }

    public void run() {
        System.out.println("writeClient");
        try {
                socketWrapper.write(authenticate);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
