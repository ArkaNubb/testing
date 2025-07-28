package service;

import common.MemberRequest;
import common.*;

import java.io.IOException;

public class ReadThreadServer implements Runnable {
    private Thread thr;
    private SocketWrapper socketWrapper;

    public ReadThreadServer(SocketWrapper socketWrapper) {
        this.socketWrapper = socketWrapper;
        this.thr = new Thread(this);
        thr.start();
    }

    private void cleanupDisconnectedClient() {
        // Remove this socket from the client map
        server.clientMap.entrySet().removeIf(entry -> entry.getValue() == socketWrapper);
        System.out.println("🧹 Cleaned up disconnected client from map");
    }

    public void run() {
        try {
            while(true){
                Object obj = socketWrapper.read();

                if (obj instanceof Authenticate) {
                    Member member = MemberService.isMemberFound(((Authenticate) obj).getUserId());
                    if (member != null) {
                        MemberPackage memberPackage = new MemberPackage(member, BookService.getAllBooks());
                        if (!server.clientMap.containsKey(member.getUserId())) {
                            server.clientMap.put(member.getUserId(), socketWrapper);}

                        socketWrapper.write(memberPackage);}
                }

                if (obj instanceof LibrarianAuthenticate) {
                    Librarian librarian = LibrarianService.getLibrarian();
                    if (librarian != null) {
                        LibrarianPackage librarianPackage = new LibrarianPackage(
                                librarian,
                                LibrarianService.getPendingIssuingBook(),
                                LibrarianService.getPendingReturnedBook(),
                                LibrarianService.getPendingPublishRequests(),
                                BookService.getAllBooks()
                        );
                        String librarianUserId = librarian.getUserId();
                        server.clientMap.put(librarianUserId, socketWrapper);

                        socketWrapper.write(librarianPackage);
                    }
                }

                if (obj instanceof MemberRequest) {
                    MemberRequest memberRequest = (MemberRequest) obj;
                    Member currentMember = MemberService.isMemberFound(memberRequest.getUserID());

                    if (currentMember != null) {
                        try {
                            // Process the request - this will trigger broadcastLibrarianUpdate() internally
                            if(memberRequest.getChoice() == 1)server.librarianService.requestBorrowedBook(currentMember, memberRequest.getBookID());
                            if(memberRequest.getChoice() == 2)server.librarianService.returnBorrowedBook(currentMember, memberRequest.getBookID(), memberRequest.getRating());
                            if(memberRequest.getChoice() == 3){
//                                System.out.println("hellooooo");
                                server.librarianService.approveBook(memberRequest.getUserID(), memberRequest.getBookID());
                            }
                            if(memberRequest.getChoice() == 4){
                                server.librarianService.acceptBook(memberRequest.getUserID(), memberRequest.getBookID());

                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (obj instanceof LogoutRequest) {
                    LogoutRequest logoutRequest = (LogoutRequest) obj;
                    server.clientMap.remove(logoutRequest.getUserId());
                }
            }
        } catch (Exception e) {
            cleanupDisconnectedClient();
        }
    }
}