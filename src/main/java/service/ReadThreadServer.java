package service;

import common.MemberRequest;
import common.*;

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
                    if(!server.clientMap.containsKey(member.getUserId()))server.clientMap.put( member.getUserId(), socketWrapper);
                    socketWrapper.write(memberPackage);
                }

                if (obj instanceof LibrarianAuthenticate) {
                    // send MemberPackage
                    Librarian librarian = LibrarianService.getLibrarian();
//                    System.out.println(member);
                    LibrarianPackage librarianPackage = new LibrarianPackage(librarian, LibrarianService.getPendingIssuingBook(), LibrarianService.getPendingReturnedBook(), LibrarianService.getPendingPublishRequests(), BookService.getAllBooks());
//                    socketWrapper.write(librarianPackage);
                    if(!server.clientMap.containsKey(librarian.getUserId()))server.clientMap.put(librarian.getUserId(), socketWrapper);
                    new WriteThreadServer(socketWrapper, librarianPackage);
                }

                if(obj instanceof MemberRequest){
                    MemberRequest memberRequest = (MemberRequest)(obj);
                    Member currentMember = MemberService.isMemberFound(memberRequest.getUserID());
                    server.librarianService.requestBorrowedBook(currentMember, memberRequest.getBookID());

                    Librarian librarian = LibrarianService.getLibrarian();
                    LibrarianPackage librarianPackage = new LibrarianPackage(librarian, LibrarianService.getPendingIssuingBook(), LibrarianService.getPendingReturnedBook(), LibrarianService.getPendingPublishRequests(), BookService.getAllBooks());
                    if(!server.clientMap.containsKey(librarian.getUserId()))new WriteThreadServer(server.clientMap.get(librarian.getUserId()), librarianPackage);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
