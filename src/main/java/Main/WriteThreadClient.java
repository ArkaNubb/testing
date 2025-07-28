package Main;

import util.SocketWrapper;

import java.io.IOException;
import java.util.Scanner;

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
//            if(authenticate != null) {
                socketWrapper.write(authenticate);
//            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
