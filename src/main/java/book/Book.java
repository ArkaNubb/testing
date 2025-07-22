package book;

import java.util.ArrayList;
import java.util.List;

public class Book {
    private String name;
    private String bookId;
    private String publishedDate;
    private String AuthorName;
    private List<String>genre;
    private List<Double> ratings;
    private int total_copies;
    private int available_copies;

    public Book(String name, String bookId, String publishedDate, String authorName, List<String> genre, List<Double>ratings, int total_copies, int available_copies) {
        this.name = name;
        this.bookId = bookId;
        this.publishedDate = publishedDate;
        AuthorName = authorName;
        this.genre = genre;
        this.ratings = ratings;
        this.total_copies = total_copies;
        this.available_copies = available_copies;
    }

    public Book(String name, String publishedDate, String authorName, List<String> genre) {
        this.name = name;
        this.publishedDate = publishedDate;
        AuthorName = authorName;
        this.genre = genre;
    }

    public Book() {

    }

    public String getName(){
        return name;
    }

    public String getAuthorName(){
        return AuthorName;
    }
    public void setRating(double rating){
        ratings.add(rating);
    }
    public double getRating(){
        double sum = 0;
        for(var x: ratings){
            sum += x;
        }
        return sum / ratings.size();
    }

    @Override
    public String toString() {
        return "Book ID: " + bookId + "\n" +
                "-------------------------\n" +
                "Name           : " + name + "\n" +
                "Published Date : " + publishedDate + "\n" +
                "Author Name    : " + AuthorName + "\n" +
                "Genres         : " + genre + "\n" +
                "Ratings        : " + String.format("%.2f", getRating()) + "\n" +
                "Total Copies   : " + total_copies + "\n" +
                "Available      : " + available_copies + "\n" +
                "Borrowed       : " + (total_copies - available_copies) + "\n" +
                "-------------------------";
    }

    public String getBookId(){
        return bookId;
    }
    public int isAvailable(){
        return available_copies;
    }

    public void setAvailable_copies(int available_copies) {
        this.available_copies = available_copies;
    }
}
