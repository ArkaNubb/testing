package service;

import Main.Authenticate;
import Main.MemberPackage;
import user.Member;
import util.SocketWrapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import Main.Message;

public class ReadThreadServer implements Runnable {
    private Thread thr;
    private SocketWrapper socketWrapper;
//    public HashMap<String, SocketWrapper> clientMap;

    public ReadThreadServer(SocketWrapper socketWrapper) {
//        this.clientMap = map;
        this.socketWrapper = socketWrapper;
        this.thr = new Thread(this);
        thr.start();
    }

    public void run() {
        try {
            while(true){
                Object obj = socketWrapper.read();
                if (obj instanceof Authenticate) {
                    // send MemberPackage
                    Member member = MemberService.isMemberFound(((Authenticate) obj).getUserId());
                    System.out.println(member);
                    MemberPackage memberPackage = new MemberPackage(member, BookService.getAllBooks());
                    socketWrapper.write(memberPackage);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
