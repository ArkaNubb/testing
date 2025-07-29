package service;

import common.MemberRequest;
import common.*;
import java.io.IOException;
import java.util.ArrayList;

public class ReadThreadServer implements Runnable {
    private Thread thr;
    private SocketWrapper socketWrapper;

    public ReadThreadServer(SocketWrapper socketWrapper) {
        this.socketWrapper = socketWrapper;
        this.thr = new Thread(this);
        thr.start();
    }

    private void cleanupDisconnectedClient() {
        server.clientMap.entrySet().removeIf(entry -> entry.getValue() == socketWrapper);
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

                if (obj instanceof AuthorAuthenticate) {
                    AuthorAuthenticate authorAuthenticate = (AuthorAuthenticate)obj;
                    Author author = AuthorService.getAuthor(authorAuthenticate.getUserId());
                    if (author != null && author.getPassword().equals(authorAuthenticate.getPassWord())) {
                        AuthorPackage authorPackage = new AuthorPackage(author);
                        if (!server.clientMap.containsKey(author.getUserId())) {
                            server.clientMap.put(author.getUserId(), socketWrapper);
                        }
                        socketWrapper.write(authorPackage);
                        System.out.println("Author authenticated successfully: " + author.getName());
                    } else {
                        System.out.println("Author authentication failed for userId: " + authorAuthenticate.getUserId());
                    }
                }

                if (obj instanceof MemberRequest) {
                    MemberRequest memberRequest = (MemberRequest) obj;
                    if(memberRequest.getChoice() == 1) {
                        server.librarianService.requestBorrowedBook(MemberService.isMemberFound(memberRequest.getUserID()), memberRequest.getBookID());
                    } else if(memberRequest.getChoice() == 2) {
                        server.librarianService.returnBorrowedBook(MemberService.isMemberFound(memberRequest.getUserID()), memberRequest.getBookID(), memberRequest.getRating());
                    } else if(memberRequest.getChoice() == 3) {
                        server.librarianService.approveBook(memberRequest.getUserID(), memberRequest.getBookID());
                    } else if(memberRequest.getChoice() == 4){
                        server.librarianService.acceptBook(memberRequest.getUserID(), memberRequest.getBookID());
                    }
                }

                if (obj instanceof AuthorRequest) {
                    AuthorRequest authorRequest = (AuthorRequest) obj;
                    try {
                        System.out.println("Processing author request of type: " + authorRequest.getRequestType());

                        if (authorRequest.getRequestType() == AuthorRequest.PUBLISH_BOOK) {
                            Author currentAuthor = AuthorService.getAuthor(authorRequest.getAuthorId());
                            if (currentAuthor != null) {
                                // CORRECTED: Generate the Book ID on the server
                                String newBookId = server.authorService.generateBookId();
                                System.out.println("Generated new Book ID: " + newBookId + " for title: " + authorRequest.getBookTitle());

                                Book newBook = new Book(
                                        authorRequest.getBookTitle(),
                                        newBookId, // Use the new server-generated ID
                                        authorRequest.getPublishedDate(),
                                        currentAuthor.getName(),
                                        authorRequest.getGenres(),
                                        new ArrayList<>(),
                                        authorRequest.getTotalCopies(),
                                        authorRequest.getTotalCopies()
                                );
                                server.librarianService.requestPublishBook(currentAuthor, newBook);
                            }

                        } else if (authorRequest.getRequestType() == AuthorRequest.REMOVE_BOOK) {
                            Author currentAuthor = AuthorService.getAuthor(authorRequest.getAuthorId());
                            if (currentAuthor != null) {
                                System.out.println("Processing remove request for book ID: " + authorRequest.getBookId());
                                Book bookToRemove = BookService.findBook(authorRequest.getBookId());
                                if (bookToRemove != null) {
                                    AuthorService authorService = new AuthorService(currentAuthor);
                                    authorService.RemoveBook(bookToRemove);
                                    // This broadcast is specific to removing a book
                                    LibrarianService.broadcastAuthorActionResults(currentAuthor.getUserId());
                                }
                            }
                        } else if (authorRequest.getRequestType() == AuthorRequest.APPROVE_PUBLICATION) {
                            System.out.println("Processing approval for book ID: " + authorRequest.getBookId());
                            server.librarianService.approvePublishRequest(authorRequest.getAuthorId(), authorRequest.getBookId());
                        }

                    } catch (Exception e) {
                        System.out.println("Error processing author request: " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                if (obj instanceof LogoutRequest) {
                    LogoutRequest logoutRequest = (LogoutRequest) obj;
                    server.clientMap.remove(logoutRequest.getUserId());
                }
                if(obj instanceof Member){
                    String userId = server.memberService.generateMemberUserId();
                    Member member = (Member)obj;
                    member.setUserId(userId);
                    server.memberService.addMember(member);
                    socketWrapper.write(userId);
                }
                if(obj instanceof Author){
                    String userId = server.authorService.genetateAuthorId();
                    Author author = (Author)obj;
                    author.setUserId(userId);
                    server.authorService.addAuthor((Author)obj);
                    socketWrapper.write(userId);
                }
            }
        } catch (Exception e) {
            System.out.println("A client has disconnected.");
            cleanupDisconnectedClient();
        }
    }
}