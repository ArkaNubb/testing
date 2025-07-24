package Main;

import book.Book;
import user.Author;
import user.Librarian;
import user.Member;
import service.AuthorService;
import service.BookService;
import service.LibrarianService;
import service.MemberService;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;



public class Main extends Application{

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("main-view.fxml"));
        stage.setScene(new Scene(root)); // Explicitly set size
        stage.show();
    }

    public static void main(String[] args) throws IOException {

        System.out.println("hello");

        // reading from file;
        BufferedReader bookInformation = new BufferedReader(new FileReader("src\\main\\java\\Main\\bookInformation.txt"));
        BufferedReader authorInformation = new BufferedReader(new FileReader("src\\main\\java\\Main\\authorInformation.txt"));
        BufferedReader memberInformation = new BufferedReader(new FileReader("src\\main\\java\\Main\\memberInformation.txt"));
        BufferedReader librarianInformation = new BufferedReader(new FileReader("src\\main\\java\\Main\\librarianInformation.txt"));

        // reading booklist;
        List<String>bookList = new ArrayList<>();
        String str;
        while((str = bookInformation.readLine()) != null){
            bookList.add(str);
        }

        // reading authorlist;
        List<String>authorList = new ArrayList<>();
        while((str = authorInformation.readLine()) != null){
            authorList.add(str);
        }

        // reading memberlist;
        List<String>memberList = new ArrayList<>();
        while((str = memberInformation.readLine()) != null){
            memberList.add(str);
        }

        // reading libraring;
        String librarian = librarianInformation.readLine();

        bookInformation.close();
        authorInformation.close();
        memberInformation.close();
        librarianInformation.close();

        // loading
        List<Book>allBooks = new ArrayList<>();
        List<Member>allMembers = new ArrayList<>();
        List<Author>allAuthors = new ArrayList<>();

        // loading allbooks
        for(var bookInfo: bookList){
            String [] values = bookInfo.split("\\|");
            String [] genre__ = values[4].split(",");
            String [] ratings__ = values[5].split(",");
            List<String> genre = new ArrayList<>();
            List<Double> ratings = new ArrayList<>();
            for(var g: genre__){
                genre.add(g);
            }
            for(var r: ratings__){
                double d = Double.parseDouble(r.trim());
                ratings.add(d);
            }
            int total_copies = Integer.parseInt(values[6]);
            int available_copies = Integer.parseInt(values[7]);
            Book book = new Book(values[0], values[1], values[2], values[3], genre, ratings, total_copies, available_copies);
            allBooks.add(book);
        }
        BookService bookService = new BookService(allBooks);

        //loading authors
        for (var authorInfo: authorList){
            String [] values = authorInfo.split("\\|");
            String [] bookIds = values[4].split(",");
            List<Book>authorPublishedBooks = new ArrayList<>();
            for (var x: bookIds){
                authorPublishedBooks.add(bookService.findBook(x));
            }
            Author author = new Author(values[0], values[1], values[2], values[3], authorPublishedBooks);
            allAuthors.add(author);
        }
        AuthorService authorService = new AuthorService(allAuthors);

        //loading members
        for (var memberInfo: memberList){
            String [] valuess = memberInfo.split("\\|");
            String [] bookIds = valuess[4].split(",");

            List<Book>booklist = new ArrayList<>();
            for(var x: bookIds){
                if(x.equals("dummybook")) continue;
                booklist.add(bookService.findBook(x));
            }
            Member member = new Member(valuess[0], valuess[1], valuess[2], valuess[3], booklist);
            allMembers.add(member);
        }
        MemberService memberService = new MemberService(allMembers);

        //loading librarian;
        String [] values = librarian.split("\\|");
        Librarian current_librarian = new Librarian(values[0], values[1], values[2], values[3]);
        LibrarianService librarianService = new LibrarianService(current_librarian);

        // here all uis

//        launch(args);

        Scanner sc = new Scanner(System.in);
        int choice;
        choice = 0;
       while(choice != 5){
           System.out.println("1. LOGIN AS A MEMBER");
           System.out.println("2. LOGIN AS A AUTHOR");
           System.out.println("3. LOGIN AS A LIBRARIAN");
           System.out.println("4. Create Account: ");
           System.out.println("5. Enter 5 to exit library");
           System.out.print("Enter your Choice: ");
           choice = sc.nextInt();
           sc.nextLine();
           switch (choice){
               case 1:
               {
                   String userId;
                   String password;
                   System.out.print("USER ID: ");
                   userId = sc.nextLine();
                   Member member = MemberService.isMemberFound(userId);
                   if(member != null){
                       System.out.print("PASSWORD: ");
                       password = sc.nextLine();
                       if(member.getPassword().equals(password)){
                           System.out.println("found it!!!");

                           int member_choice = 0;
                           while(member_choice != 5){
                               System.out.println("Enter 1 for viewing all books");
                               System.out.println("Enter 2 for showing borrowed books");
                               System.out.println("Enter 3 to request borrow books");
                               System.out.println("Enter 4 to request returning books");
                               System.out.println("Enter 5 for logOut");
                               member_choice = sc.nextInt();
                               sc.nextLine();
                               switch (member_choice){
                                   case 1 : {
                                       BookService.showAllBooks();
                                       break;
                                   }
                                   case 2:{
                                       member.showAllBorrowedBooks();
                                       break;
                                   }
                                   case 3:{
                                       if(member.isMemberCanBorrow()){
                                           System.out.println("Enter bookId: ");
                                           String bookId = sc.nextLine();
                                           librarianService.requestBorrowedBook(member, bookId);
                                       }
                                       else System.out.println("You've reached your maximum borrow limit");
                                       break;
                                   }
                                   case 4:{
                                       System.out.println("Enter bookId: ");
                                       String bookId = sc.nextLine();
                                       System.out.println("Enter your rating: ");
                                       double rating = sc.nextDouble();
                                       librarianService.returnBorrowedBook(member, bookId, rating);
                                       break;
                                   }
                               }
                           }
                       }
                       else{
                           System.out.println("Wrong password!! Try again");
                       }
                   }
                   else{
                       System.out.println("UserId not found! Create new Account");
                   }
                   break;
               }
               case 2:{
                   String userId;
                   String password;
                   System.out.print("USER ID: ");
                   userId = sc.nextLine();

                   Author author = AuthorService.isAuthorFound(userId);
                   if(author != null){
                       System.out.print("PASSWORD: ");
                       password=sc.nextLine();
                       if(author.getPassword().equals(password)){
                           System.out.println("Logged in as Author: " + author.getName());

                           int author_choice=0;
                           while(author_choice!=4){
                               System.out.println("Enter 1 to view your published books");
                               System.out.println("Enter 2 to request publishing a new book");
                               System.out.println("Enter 3 to remove a book");
                               System.out.println("Enter 4 for logOut");
                               author_choice = sc.nextInt();
                               sc.nextLine();

                               switch (author_choice){
                                   case 1: {
                                       author.showPublishedBooks();
                                       break;
                                   }
                                   case 2: {
                                       System.out.print("Enter book name: ");
                                       String bookName = sc.nextLine();
                                       System.out.print("Enter published date (dd-mm-yyyy): ");
                                       String publishedDate = sc.nextLine();
                                       System.out.print("Enter number of genres: ");
                                       int numGenres = sc.nextInt();
                                       sc.nextLine();

                                       List<String> genres = new ArrayList<>();
                                       for(int i = 0;i<numGenres; i++){
                                           System.out.print("Enter genre "+(i+1) + ": ");
                                           String genre = sc.nextLine().toUpperCase();
                                           genres.add(genre);
                                       }

                                       System.out.print("Enter total number of copies: ");
                                       int totalCopies = sc.nextInt();
                                       sc.nextLine();

                                       try {
                                           List<Double> defaultRatings=new ArrayList<>();
                                           String bookId = authorService.generateBookId();
                                           Book book = new Book(bookName, bookId, publishedDate, author.getName(), genres, defaultRatings, totalCopies, totalCopies);
                                           librarianService.requestPublishBook(author, book);
                                           System.out.println("book publishing request sent to librarian :");
                                           System.out.println("Book ID: " + bookId);
                                           System.out.println("wait for librarian approval.");
                                       } catch (IOException e) {
                                           System.out.println("Error while requesting book publication: " + e.getMessage());
                                       }
                                       break;
                                   }
                                   case 3: {
//                                       System.out.print("Enter book name to remove: ");
//                                       authorService.RemoveBook(bookName);
                                       System.out.println("Enter bookId: ");
                                       String bookId = sc.nextLine();
                                       Book book = BookService.findBook(bookId);
                                       authorService.RemoveBook(book);
                                       System.out.println("Book removed from your published list.");
                                       break;
                                   }
                               }
                           }
                       }
                       else{
                           System.out.println("Wrong password!! Try again");
                       }
                   }
                   else{
                       System.out.println("Author ID not found! Create new Account");
                   }
                   break;
               }
               case 3:{
                   String userId;
                   String password;
                   System.out.print("USER ID: ");
                   userId = sc.nextLine();
                   if(current_librarian.getUserId().equals(userId)){
                       System.out.print("PASSWORD: ");
                       password = sc.nextLine();
                       if(current_librarian.getPassword().equals(password)){
                           System.out.println("logged in as librarian");

                           int librarian_choice = 0;
                           while(librarian_choice != 5){
                               System.out.println("Enter 1 for viewing all books");
                               System.out.println("Enter 2 for showing pending issuing books");
                               System.out.println("Enter 3 to showing pending returning books");
                               System.out.println("Enter 4 for showing pending book publishing requests");
                               System.out.println("Enter 5 for logOut");
                               librarian_choice = sc.nextInt();
                               sc.nextLine();
                               switch (librarian_choice){
                                   case 1 : {
                                       bookService.showAllBooks();
                                       break;
                                   }
                                   case 2:{
                                       librarianService.showPendingBooks();
                                       int addChoice = 0;
                                       while(addChoice != 2){
                                           System.out.println("Enter 1 for approve pending issuing books");
                                           System.out.println("Enter 2 to quit");
                                           addChoice = sc.nextInt();
                                           sc.nextLine();
                                           switch (addChoice){
                                               case 1:{
                                                   System.out.print("Enter member userId: ");
                                                   String memberUserId = sc.nextLine();
                                                   System.out.print("Enter bookId: ");
                                                   String bookId = sc.nextLine();
                                                   librarianService.approveBook(memberUserId, bookId);
                                                   break;
                                               }
                                           }

                                       }
                                       break;
                                   }
                                   case 3:{
                                       librarianService.showPendingReturnBook();
                                       int addChoice = 0;
                                       while(addChoice != 2){
                                           System.out.println("Enter 1 for accepting returned books");
                                           System.out.println("Enter 2 to quit");
                                           addChoice = sc.nextInt();
                                           sc.nextLine();
                                           switch (addChoice){
                                               case 1:{
                                                   System.out.print("Enter member userId: ");
                                                   String memberUserId = sc.nextLine();
                                                   System.out.print("Enter bookId: ");
                                                   String bookId = sc.nextLine();
                                                   librarianService.acceptBook(memberUserId, bookId);
                                                   break;
                                               }
                                           }

                                       }
                                       break;
                                   }
                                   case 4: {
                                       librarianService.showPendingPublishRequests();
                                       int addChoice = 0;
                                       while (addChoice != 2) {
                                           System.out.println("Enter 1 to approve book publishing request");
                                           System.out.println("Enter 2 to quit");
                                           addChoice = sc.nextInt();
                                           sc.nextLine();
                                           switch (addChoice) {
                                               case 1: {
                                                   System.out.print("Enter author userId: ");
                                                   String authorUserId = sc.nextLine();
                                                   System.out.print("Enter bookId to approve: ");
                                                   String bookId = sc.nextLine();
                                                   try {
                                                       librarianService.approvePublishRequest(authorUserId, bookId);
                                                   } catch (IOException e) {
                                                       System.out.println("Error approving publish request: " + e.getMessage());
                                                   }
                                                   break;
                                               }
                                           }
                                       }
                                       break;
                                   }
                                   case 5:{
                                       break;
                                   }
                               }
                           }
                       }
                       else{
                           System.out.println("Wrong password!! Try again");
                       }
                   }
                   else{
                       System.out.println("UserId not found! Create new Account");
                   }
                   break;
               }
               case 4:{
                   System.out.println("Enter Account type: 1. Member 2.Author");
                   int account_type = sc.nextInt();
                   sc.nextLine();
                   switch (account_type){
                       case 1:{
                           System.out.print("Enter your name: ");
                           String name = sc.nextLine();
                           System.out.print("Enter your email: ");
                           String email = sc.nextLine();
                           System.out.print("Enter your password: ");
                           String password = sc.nextLine();
                           String userId = memberService.generateMemberUserId();
                           System.out.println("Your User Id(please remember it): " + userId);
                           List<Book>emptyList = new ArrayList<>();
                           Member member = new Member(email, userId, password, name, emptyList);
                           memberService.addMember(member);
                           break;
                       }
                       case 2:{
                           System.out.print("Enter your name: ");
                           String name = sc.nextLine();
                           System.out.print("Enter your email: ");
                           String email = sc.nextLine();
                           System.out.print("Enter your password: ");
                           String password = sc.nextLine();

                           // Generate author ID

                           String userId = authorService.genetateAuthorId();
                           System.out.println("Your Author User Id(please remember it): " + userId);
                           List<Book> publishedBooks = new ArrayList<>();
                           Author newAuthor = new Author(email, userId, password, name, publishedBooks);
                           authorService.addAuthor(newAuthor);

                           // Save to file

                           break;
                       }
                   }
               }
           }
       }
    }
}

