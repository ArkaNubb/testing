package Main;

import common.MemberPackage;
import util.SocketWrapper;

public class ReadThreadClient implements Runnable {
    private Thread thr;
    private SocketWrapper socketWrapper;

    public ReadThreadClient(SocketWrapper socketWrapper) {
        this.socketWrapper = socketWrapper;
        this.thr = new Thread(this);
        thr.start();
    }

    public void run() {
        try {
            while(true){
//                System.out.println("readClient");
                MemberPackage obj = (MemberPackage) socketWrapper.read();
                if (obj instanceof MemberPackage) {
                    Main.setMemberPackage(obj);
                }

            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
