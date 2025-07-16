package book;

import java.util.ArrayList;
import java.util.List;

public class Book {
    private String name;
    private String publishedDate;
    private String AuthorName;
    private List<String>genre;
    private List<Double> ratings;

    public Book(String name, String publishedDate, String authorName, List<String> genre, List<Double>ratings) {
        this.name = name;
        this.publishedDate = publishedDate;
        AuthorName = authorName;
        this.genre = genre;
        this.ratings = ratings;
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
}
