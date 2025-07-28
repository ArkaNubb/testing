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
                    System.out.println("🔐 Processing Member Authentication...");
                    Member member = MemberService.isMemberFound(((Authenticate) obj).getUserId());
                    System.out.println("👤 Member found: " + (member != null ? member.getName() : "null"));

                    if (member != null) {
                        MemberPackage memberPackage = new MemberPackage(member, BookService.getAllBooks());

                        // Add member to client map if not already present
                        if (!server.clientMap.containsKey(member.getUserId())) {
                            server.clientMap.put(member.getUserId(), socketWrapper);
                            System.out.println("✅ Added member to clientMap: " + member.getUserId());
                        }

                        socketWrapper.write(memberPackage);
                        System.out.println("📦 MemberPackage sent to: " + member.getName());
                    }
                }

                if (obj instanceof LibrarianAuthenticate) {
                    System.out.println("🔐 Processing Librarian Authentication...");

                    Librarian librarian = LibrarianService.getLibrarian();
                    System.out.println("👨‍💼 Librarian found: " + (librarian != null ? librarian.getName() : "null"));

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

                    System.out.println("📚 Received MemberRequest from " +
                            (currentMember != null ? currentMember.getName() : "unknown") +
                            " for book " + memberRequest.getBookID());

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
                            System.out.println("✅ MemberRequest processed successfully");
                        } catch (IOException e) {
                            System.out.println("❌ Error processing MemberRequest: " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("❌ Member not found for userId: " + memberRequest.getUserID());
                    }
                }
                if (obj instanceof LogoutRequest) {
                    LogoutRequest logoutRequest = (LogoutRequest) obj;
                    server.clientMap.remove(logoutRequest.getUserId());
                    System.out.println("👋 User " + logoutRequest.getUserId() + " logged out and removed from clientMap");
                    System.out.println("📋 ClientMap now contains: " + server.clientMap.keySet());
                }
            }
        } catch (Exception e) {
            cleanupDisconnectedClient();
        }
    }
}