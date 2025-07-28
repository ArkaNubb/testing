package service;

import Main.MemberPackage;
import book.Book;
import user.Member;
import util.SocketWrapper;

import java.io.IOException;
import java.util.List;

public class WriteThreadServer implements Runnable{
    private Thread thr;
    private SocketWrapper socketWrapper;
    Member member;
    List<Book>allbooks;

    public WriteThreadServer(SocketWrapper socketWrapper, Member member, List<Book>allbooks) {
        this.allbooks = allbooks;
        this.socketWrapper = socketWrapper;
        this.member = member;
        this.thr = new Thread(this);
        thr.start();
    }

    @Override
    public void run() {

        try {
//            while (true){
                if(member != null && allbooks != null) {
                    System.out.println("WriteServer");
                    MemberPackage memberPackage = new MemberPackage(member, allbooks);
                    socketWrapper.write(memberPackage);System.out.println("kire write koros");
                }
//            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
