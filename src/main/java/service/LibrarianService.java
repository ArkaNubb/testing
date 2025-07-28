package service;

import common.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import service.server;

public class LibrarianService {
    public static Librarian librarian;

    public static Librarian getLibrarian() {
        return librarian;
    }

    public LibrarianService() {
    }

    public static Map<Member, List<String>> pendingIssuingBook  = new HashMap<>();;
    public static Map<Member, List<Pair<String, Double>>> pendingReturnedBook  = new HashMap<>();;
    public static Map<Author, List<Book>> pendingPublishRequests  = new HashMap<>();;

    public static Map<Member, List<String>> getPendingIssuingBook() {
        return pendingIssuingBook;
    }

    public static Map<Member, List<Pair<String, Double>>> getPendingReturnedBook() {
        return pendingReturnedBook;
    }

    public static Map<Author, List<Book>> getPendingPublishRequests() {
        return pendingPublishRequests;
    }

    public LibrarianService(Librarian librarian) throws IOException {
        this.librarian = librarian;
        BufferedReader readPendingBooks =  new BufferedReader(new FileReader("src\\main\\java\\service\\librarianPendingBooks.txt"));
        BufferedReader readPendingReturnBooks =  new BufferedReader(new FileReader("src\\main\\java\\service\\librarianPendingReturnBooks.txt"));

        // reading pending Books
        List<String>pendingbookList = new ArrayList<>();
        String str;
        while((str = readPendingBooks.readLine()) != null){
            str = str.trim();
            if(str.isEmpty()) continue;
            pendingbookList.add(str);
        }
        for (var x: pendingbookList){
            String[] values = x.split("\\|");
            Member member = MemberService.isMemberFound(values[0]);
            List<String> bookIds = new ArrayList<>();
            String[] vv = values[1].split(",");
            for(var v: vv){
                bookIds.add(v);
            }
            pendingIssuingBook.put(member, bookIds);
        }

        // reading pending return books
        List<String>pendingbookreturnList = new ArrayList<>();
        while((str = readPendingReturnBooks.readLine()) != null){
            str = str.trim();
            if(str.isEmpty()) continue;
            pendingbookreturnList.add(str);
        }
        for (var x: pendingbookreturnList){
            String[] values = x.split("\\|");
            Member member = MemberService.isMemberFound(values[0]);
            List<Pair<String, Double>> bookPairs = new ArrayList<>();
            String[] vv = values[1].split(",");
            for(var v: vv){
                if(v.equals("dummybook")) continue;
                if(v.contains(":")){
                    String[] bookRating = v.split(":");
                    String bookId = bookRating[0];
                    double rating = Double.parseDouble(bookRating[1]);
                    bookPairs.add(new Pair<>(bookId, rating));
                }
            }
            pendingReturnedBook.put(member, bookPairs);
        }

        // reading author pending books
        BufferedReader readPendingPublishRequests = new BufferedReader(new FileReader("src\\main\\java\\service\\authorPendingRequest.txt"));

        // Reading pending publish requests
        List<String> pendingPublishList = new ArrayList<>();
        while((str = readPendingPublishRequests.readLine()) != null){
            str = str.trim();
            if(str.isEmpty()) continue;
            pendingPublishList.add(str);
        }

        for(var x: pendingPublishList){
            String[] values = x.split("\\|");

            Author author = AuthorService.isAuthorFound(values[0]);
            String[] bookData=values[1].split(";");
            List<Book> books=new ArrayList<>();
            for(String bookInfo : bookData){
                if(bookInfo.equals("dummybook")) continue;
                String[] bookParts = bookInfo.split(":");
                String[] genreArray = bookParts[3].split(",");
                List<String> genres = Arrays.asList(genreArray);
                int totalCopies = Integer.parseInt(bookParts[4]);
                Book book=new Book(bookParts[0], bookParts[0], bookParts[0], author.getName(), genres, new ArrayList<>(), totalCopies, totalCopies);
                books.add(book);
            }
            pendingPublishRequests.put(author, books);
        }
        readPendingPublishRequests.close();

    }

    public void requestBorrowedBook(Member member, String bookId) throws IOException {
        System.out.println("🔄 Processing borrow request for member: " + member.getName() + ", bookId: " + bookId);

        if(pendingIssuingBook.containsKey(member)){
            pendingIssuingBook.get(member).add(bookId);
            String filePath = "src\\main\\java\\service\\librarianPendingBooks.txt";
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            boolean found = false;
            for (int i = 0; i < lines.size(); i++){
                if(lines.get(i).contains(member.getUserId()+"|")){
                    found = true;
                    String[] parts = lines.get(i).split("\\|");
                    String[] books = parts[1].split(",");

                    List<String> bookList = new ArrayList<>(Arrays.asList(books));
                    String updatedBooks = String.join(",", bookList);
                    if (!bookId.isEmpty()) {
                        updatedBooks += (updatedBooks.isEmpty() ? "" : ",") + bookId;
                    }
                    parts[1] = updatedBooks;
                    lines.set(i, String.join("|", parts));
                    break;
                }
            }
            Files.write(Paths.get(filePath), lines);
        }
        else{
            List<String> list = new ArrayList<>();
            list.add(bookId);
            pendingIssuingBook.put(member, list);
            BufferedWriter pendingInformation = new BufferedWriter(new FileWriter("src\\main\\java\\service\\librarianPendingBooks.txt", true));
            pendingInformation.write( member.getUserId() + "|" + "dummybook," + bookId + "\n");
            pendingInformation.close();
        }

        System.out.println("✅ Borrow request processed, triggering broadcast...");
        broadcastLibrarianUpdate();
    }

    public void returnBorrowedBook(Member member, String bookId, double rating) throws IOException {
        System.out.println("🔄 Processing return request for member: " + member.getName() + ", bookId: " + bookId);

        if(pendingReturnedBook.containsKey(member)){
            pendingReturnedBook.get(member).add(new Pair<>(bookId, rating));
            String filePath = "src\\main\\java\\service\\librarianPendingReturnBooks.txt";
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            boolean found = false;
            for (int i = 0; i < lines.size(); i++){
                if(lines.get(i).contains(member.getUserId()+"|")){
                    found = true;
                    String[] parts = lines.get(i).split("\\|");
                    String[] books = parts[1].split(",");

                    List<String> bookList = new ArrayList<>(Arrays.asList(books));
                    String updatedBooks = String.join(",", bookList);
                    if (!bookId.isEmpty()) {
                        updatedBooks += (updatedBooks.isEmpty() ? "" : ",") + bookId+":"+rating;
                    }
                    parts[1] = updatedBooks;
                    lines.set(i, String.join("|", parts));
                    break;
                }
            }
            Files.write(Paths.get(filePath), lines);
        }
        else{
            List<Pair<String, Double>> list = new ArrayList<>();
            list.add(new Pair<>(bookId, rating));
            pendingReturnedBook.put(member, list);
            BufferedWriter pendingInformation = new BufferedWriter(new FileWriter("src\\main\\java\\service\\librarianPendingReturnBooks.txt", true));
            pendingInformation.write(member.getUserId() + "|" + "dummybook," + bookId +":"+rating+ "\n");
            pendingInformation.close();
        }

        System.out.println("✅ Return request processed, triggering broadcast...");
        broadcastLibrarianUpdate();
    }

    public void requestPublishBook(Author author, Book book) throws IOException {
        // old author hoile just ager info
        if(pendingPublishRequests.containsKey(author)){
            pendingPublishRequests.get(author).add(book);
            updatePendingPublishFile(author,book,false);
        }
        // new author
        else{
            List<Book> books=new ArrayList<>();
            books.add(book);
            pendingPublishRequests.put(author,books);
            updatePendingPublishFile(author,book,true);
        }
        broadcastLibrarianUpdate();
    }

    private void updatePendingPublishFile(Author author, Book book, boolean isNewAuthor) throws IOException {
        if(isNewAuthor){
            BufferedWriter pendingInformation = new BufferedWriter(new FileWriter("src\\main\\java\\service\\authorPendingRequest.txt", true));
            String bookData = book.getName() + ":" + book.getBookId()+":"+book.getPublishedDate() + ":" +String.join(",", book.getGenre())+":"+book.getTotal_copies();
            pendingInformation.write(author.getUserId()+"|"+"dummybook;"+bookData+"\n");
            pendingInformation.close();
        }
        else{
            String filePath="src\\main\\java\\service\\authorPendingRequest.txt";
            List<String> lines=Files.readAllLines(Paths.get(filePath));
            for (int i=0;i<lines.size();i++){
                if(lines.get(i).contains(author.getUserId() + "|")){
                    String[] parts=lines.get(i).split("\\|");
                    String bookData=book.getName() + ":" + book.getBookId() + ":"+ book.getPublishedDate()+":"+String.join(",", book.getGenre()) + ":" + book.getTotal_copies();
                    parts[1]=parts[1] + ";" + bookData;
                    lines.set(i, String.join("|", parts));
                    break;
                }
            }
            Files.write(Paths.get(filePath), lines);
        }
    }

    public void showPendingBooks(){
        for(Member member: pendingIssuingBook.keySet()){
            System.out.print("Name: ");
            System.out.print(member.getName());
            System.out.print(", Id: ");
            System.out.println(member.getUserId());
            System.out.println("Pending Books: ");
            for(var bookId: pendingIssuingBook.get(member)){
                if(bookId.equals("dummybook")) continue;
                Book book = BookService.findBook(bookId);
                if(book != null){
                    System.out.print("Name: ");
                    System.out.print(book.getName());
                    System.out.print(", Id: ");
                    System.out.println(book.getBookId());
                }
            }
        }
    }

    public void approveBook(String memberUserId, String bookId) throws IOException {
        Book book = BookService.findBook(bookId);
        if(book.isAvailable() > 0){
            Member member = MemberService.isMemberFound(memberUserId);
            member.addBorrowedBook(BookService.findBook(bookId));

            pendingIssuingBook.get(member).remove(bookId);
            // changing member information

            String filePath = "src\\main\\java\\service\\memberInformation.txt";
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            boolean found = false;
            for (int i = 0; i < lines.size(); i++){
                if(lines.get(i).contains("|" + member.getUserId()+"|")){
                    found = true;
                    String[] parts = lines.get(i).split("\\|");
                    String[] books = parts[4].split(",");

                    List<String> bookList = new ArrayList<>(Arrays.asList(books));
                    String updatedBooks = String.join(",", bookList);
                    if (!bookId.isEmpty()) {
                        updatedBooks += (updatedBooks.isEmpty() ? "" : ",") + bookId;
                    }
                    parts[4] = updatedBooks;
                    lines.set(i, String.join("|", parts));
                    break;
                }
            }
            Files.write(Paths.get(filePath), lines);

            //changing book avaibility information
            filePath = "src\\main\\java\\service\\bookInformation.txt";
            lines = Files.readAllLines(Paths.get(filePath));
            found = false;
            for (int i = 0; i < lines.size(); i++){
                if(lines.get(i).contains("|" +bookId+"|")){
                    found = true;
                    String[] parts = lines.get(i).split("\\|");
                    int availabe_copy = Integer.parseInt(parts[7]);
                    parts[7] = String.valueOf(availabe_copy - 1);
                    lines.set(i, String.join("|", parts));
                    break;
                }
            }
            Files.write(Paths.get(filePath), lines);
            book.setAvailable_copies(book.isAvailable() - 1);

            //changing pending books information
            filePath = "src\\main\\java\\service\\librarianPendingBooks.txt";
            lines = Files.readAllLines(Paths.get(filePath));
            found = false;
            for (int i = 0; i < lines.size(); i++){
                if(lines.get(i).contains(member.getUserId()+"|")){
                    found = true;
                    String[] parts = lines.get(i).split("\\|");
                    String[] books = parts[1].split(",");

                    List<String> bookList = new ArrayList<>(Arrays.asList(books));
                    bookList.remove(bookId);
                    String updatedBooks = String.join(",", bookList);
                    parts[1] = updatedBooks;
                    if(bookList.size() == 1) lines.remove(i);
                    else lines.set(i, String.join("|", parts));
                    break;
                }
            }
            Files.write(Paths.get(filePath), lines);

            broadcastLibrarianUpdate();
            broadcastMemberUpdate(memberUserId); // Update the specific member's UI
            broadcastToAllMembers();
        }
        else System.out.println("BOOK IS NOT AVAILABLE");
    }

    public void showPendingReturnBook(){
        for(Member member: pendingReturnedBook.keySet()){
            System.out.print("Name: ");
            System.out.print(member.getName());
            System.out.print(", Id: ");
            System.out.println(member.getUserId());
            System.out.println("Pending Returned Books: ");
            for(var bookpair: pendingReturnedBook.get(member)){
                if(bookpair.first.equals("dummybook")) continue;
                Book book = BookService.findBook(bookpair.first);
                if(book!=null){
                    System.out.println("Name :");
                    System.out.println(book.getName());
                    System.out.println(", Id :");
                    System.out.println(book.getBookId());
                    System.out.println(", Rating :");
                    System.out.println(bookpair.second);
                }
            }
            System.out.println();
        }
    }

    public void acceptBook(String memberUserId, String bookId) throws IOException{
        Member member = MemberService.isMemberFound(memberUserId);
        member.removeBook(BookService.findBook(bookId));

        // find the rating
        double rating=0.0;
        Pair<String,Double> bookToRemove=null;
        for(Pair<String, Double>bookPair:pendingReturnedBook.get(member)){
            if(bookPair.first.equals(bookId)){
                rating=bookPair.second;
                bookToRemove=bookPair;
                break;
            }
        }
        if(bookToRemove!=null) pendingReturnedBook.get(member).remove(bookToRemove);

        //changing member information
        String filePath = "src\\main\\java\\service\\memberInformation.txt";
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        boolean found = false;
        for (int i = 0; i < lines.size(); i++){
            if(lines.get(i).contains("|" + member.getUserId()+"|")){
                found = true;
                String[] parts = lines.get(i).split("\\|");
                String[] books = parts[4].split(",");

                List<String> bookList = new ArrayList<>(Arrays.asList(books));
                bookList.remove(bookId);
                String updatedBooks = String.join(",", bookList);
                parts[4] = updatedBooks;
                lines.set(i, String.join("|", parts));
                break;
            }
        }
        Files.write(Paths.get(filePath), lines);
        Book book = BookService.findBook(bookId);
        book.setAvailable_copies(book.isAvailable() + 1);

        // changing book avaibility
        filePath = "src\\main\\java\\service\\bookInformation.txt";
        lines = Files.readAllLines(Paths.get(filePath));
        found = false;
        for (int i = 0; i < lines.size(); i++){
            if(lines.get(i).contains("|" +bookId+"|")){
                found = true;
                String[] parts = lines.get(i).split("\\|");
                int availabe_copy = Integer.parseInt(parts[7]);
                parts[7] = String.valueOf(availabe_copy + 1);
                if(rating>0.0){
                    String currentRatings=parts[5];
                    if(currentRatings.isEmpty() || currentRatings.equals("dummyrating")){
                        parts[5] = String.valueOf(rating);
                    }
                    else{
                        parts[5]=currentRatings + "," + rating;
                    }
                }
                lines.set(i, String.join("|", parts));
                break;
            }
        }
        Files.write(Paths.get(filePath), lines);

        //changing return book information
        filePath = "src\\main\\java\\service\\librarianPendingReturnBooks.txt";
        lines = Files.readAllLines(Paths.get(filePath));
        found = false;
        for (int i = 0; i < lines.size(); i++){
            if(lines.get(i).contains(member.getUserId()+"|")){
                found = true;
                String[] parts = lines.get(i).split("\\|");
                String[] books = parts[1].split(",");

                List<String> bookList = new ArrayList<>();
                for(String bookEntry : books){
                    if(!bookEntry.equals("dummybook") &&
                            !bookEntry.startsWith(bookId + ":") &&
                            !bookEntry.equals(bookId)){
                        bookList.add(bookEntry);
                    }
                }

                if(bookList.isEmpty()){
                    lines.remove(i);
                }
                else{
                    String updatedBooks = String.join(",", bookList);
                    parts[1] = updatedBooks;
                    lines.set(i, String.join("|", parts));
                }
                break;
            }
        }
        Files.write(Paths.get(filePath), lines);

        broadcastLibrarianUpdate();
        broadcastMemberUpdate(memberUserId); // Update the specific member's UI
        broadcastToAllMembers();
    }

    public void showPendingPublishRequests(){
        if(pendingPublishRequests.isEmpty()){
            System.out.println("No pending publish requests.");
            return;
        }
        for(Author author:pendingPublishRequests.keySet()){
            System.out.print("Author Name: " +author.getName());
            System.out.print(", Author ID: " +author.getUserId());
            System.out.println();
            System.out.println("Pending Books for Publishing:");

            for(Book book:pendingPublishRequests.get(author)){
                if(book.getName().equals("dummybook")) continue;
                System.out.println(" Book Name: " +book.getName());
                System.out.println(" Book ID: " +book.getBookId());
                System.out.println(" Published Date: " +book.getPublishedDate());
                System.out.println(" Genres: " +String.join(", ", book.getGenre()));
                System.out.println(" Total Copies: " + book.getTotal_copies());
            }
            System.out.println();
        }
    }

    public void approvePublishRequest(String authorUserId, String bookId) throws IOException {
        Author author=null;
        Book bookToPublish=null;

        // Find the author and book
        for(Author a : pendingPublishRequests.keySet()){
            if(a.getUserId().equals(authorUserId)){
                author = a;
                for(Book book : pendingPublishRequests.get(a)){
                    if(book.getBookId().equals(bookId)){
                        bookToPublish = book;
                        break;
                    }
                }
                break;
            }
        }
        // baachal handling....
        if(author!=null && bookToPublish!=null){
            BookService.addBookToSystem(bookToPublish);
            author.addPublishedBooks(bookToPublish);

            // Remove from pending requests
            pendingPublishRequests.get(author).remove(bookToPublish);
            if(pendingPublishRequests.get(author).isEmpty()){
                pendingPublishRequests.remove(author);
            }
            // handeled book infooo
            BufferedWriter bookInformation = new BufferedWriter(new FileWriter("src\\main\\java\\service\\bookInformation.txt", true));
            String bookEntry = "\n" + bookToPublish.getName()+"|"+bookToPublish.getBookId() + "|" +
                    bookToPublish.getPublishedDate()+"|"+bookToPublish.getAuthorName() + "|" +
                    String.join(",",bookToPublish.getGenre())+"|0.0|"+
                    bookToPublish.getTotal_copies()+"|"+bookToPublish.isAvailable();
            bookInformation.write(bookEntry);
            bookInformation.close();

            updatePendingPublishRequestsFile();
            System.out.println("Book publishing request approved");
            System.out.println("Book '" + bookToPublish.getName() +" 'is now available in the library.");

            // handled author information file

            String filePath = "src\\main\\java\\service\\authorInformation.txt";
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            boolean found = false;
            for (int i = 0; i < lines.size(); i++){
                if(lines.get(i).contains("|" + author.getUserId()+"|")){
                    found = true;
                    String[] parts = lines.get(i).split("\\|");
                    String[] books = parts[4].split(",");

                    List<String> bookList = new ArrayList<>(Arrays.asList(books));
                    String updatedBooks = String.join(",", bookList);
                    if (!bookId.isEmpty()) {
                        updatedBooks += (updatedBooks.isEmpty() ? "" : ",") + bookId;
                    }
                    parts[4] = updatedBooks;
                    lines.set(i, String.join("|", parts));
                    break;
                }
            }
            Files.write(Paths.get(filePath), lines);
        }
        else{
            System.out.println("Invalid author ID or book ID.");
        }

        broadcastLibrarianUpdate();

    }

    private void updatePendingPublishRequestsFile() throws IOException {
        BufferedWriter writer=new BufferedWriter(new FileWriter("src\\main\\java\\service\\authorPendingRequest.txt"));

        for(Author author:pendingPublishRequests.keySet()){
            List<Book> books=pendingPublishRequests.get(author);
            if(!books.isEmpty()){
                StringBuilder bookData=new StringBuilder();
                for(int i=0;i<books.size();i++){
                    Book book=books.get(i);
                    if(i==0){
                        bookData.append("dummybook;");
                    }
                    bookData.append(book.getName()).append(":").append(book.getBookId()).append(":").append(book.getPublishedDate()).append(":").append(String.join(",", book.getGenre()))
                            .append(":").append(book.getTotal_copies());
                    if(i<books.size()-1){
                        bookData.append(";");
                    }
                }
                writer.write(author.getUserId() +"|"+bookData.toString()+"\n");
            }
        }
        writer.close();
    }

    // FIXED: Enhanced broadcast method with better debugging and error handling
    // In LibrarianService.java

    private void broadcastLibrarianUpdate() {
        try {
            System.out.println("🔄 Starting broadcastLibrarianUpdate...");
            Librarian currentLibrarian = LibrarianService.getLibrarian();
            if (currentLibrarian == null) {
                System.out.println("❌ ERROR: Librarian is null - cannot broadcast");
                return;
            }

            String librarianUserId = currentLibrarian.getUserId();
            if (server.clientMap.containsKey(librarianUserId)) {
                System.out.println("✅ Librarian found in clientMap - preparing broadcast...");

                LibrarianPackage librarianPackage = new LibrarianPackage(
                        currentLibrarian,
                        LibrarianService.getPendingIssuingBook(),
                        LibrarianService.getPendingReturnedBook(),
                        LibrarianService.getPendingPublishRequests(),
                        BookService.getAllBooks()
                );

                SocketWrapper librarianSocket = server.clientMap.get(librarianUserId);

                // --- FIX: Write directly to the socket instead of creating a new thread ---
                librarianSocket.write(librarianPackage);

                System.out.println("✅ Real-time update sent to librarian successfully!");

            } else {
                System.out.println("ℹ️  Librarian '" + librarianUserId + "' not found in clientMap");
            }
        } catch (Exception e) {
            System.out.println("❌ ERROR in broadcastLibrarianUpdate: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void broadcastMemberUpdate(String memberUserId) {
        try {
            System.out.println("🔄 Broadcasting member update to: " + memberUserId);

            if (server.clientMap.containsKey(memberUserId)) {
                Member updatedMember = MemberService.isMemberFound(memberUserId);
                if (updatedMember != null) {
                    MemberPackage memberPackage = new MemberPackage(updatedMember, BookService.getAllBooks());

                    SocketWrapper memberSocket = server.clientMap.get(memberUserId);
                    memberSocket.write(memberPackage);

                    System.out.println("✅ Real-time update sent to member: " + memberUserId);
                }
            } else {
                System.out.println("ℹ️  Member '" + memberUserId + "' not found in clientMap");
            }
        } catch (Exception e) {
            System.out.println("❌ ERROR in broadcastMemberUpdate: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Broadcast to all connected members (useful when book availability changes)
     */
    private void broadcastToAllMembers() {
        try {
            System.out.println("🔄 Broadcasting updates to all connected members...");

            for (String userId : server.clientMap.keySet()) {
                Member member = MemberService.isMemberFound(userId);
                if (member != null) {
                    // This is a member, not a librarian
                    broadcastMemberUpdate(userId);
                }
            }

            System.out.println("✅ Broadcast to all members completed");
        } catch (Exception e) {
            System.out.println("❌ ERROR in broadcastToAllMembers: " + e.getMessage());
            e.printStackTrace();
        }
    }
}