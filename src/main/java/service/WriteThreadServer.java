package service;

import common.*;

import java.io.IOException;
import java.util.List;

public class WriteThreadServer implements Runnable{
    private Thread thr;
    private SocketWrapper socketWrapper;
    Member member;
    List<Book>allbooks;
    LibrarianPackage librarianPackage;

    public WriteThreadServer(SocketWrapper socketWrapper, Member member, List<Book>allbooks) {
        this.allbooks = allbooks;
        this.socketWrapper = socketWrapper;
        this.member = member;
        this.thr = new Thread(this);
        thr.start();
    }
    public WriteThreadServer(SocketWrapper socketWrapper, LibrarianPackage librarianPackage){
        this.socketWrapper = socketWrapper;
        this.librarianPackage = librarianPackage;
        this.thr = new Thread(this);
        thr.start();
    }


    @Override
    public void run() {

        try {
//            while (true){
                if(librarianPackage != null) {
                    socketWrapper.write(librarianPackage);
                }
//            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
