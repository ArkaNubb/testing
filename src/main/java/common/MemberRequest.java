package common;

import java.io.Serializable;

public class MemberRequest implements Serializable {
    String bookID;
    String userID;
    String rating;
    int choice;
    public MemberRequest(String bookID, String userID, int choice) {
        this.bookID = bookID;
        this.userID = userID;
        this.choice = choice;
    }

    public int getChoice() {
        return choice;
    }

    public MemberRequest(String bookID, String userID, String rating) {
        this.bookID = bookID;
        this.userID = userID;
        this.rating = rating;
        choice = 2;
    }

    public double getRating() {
        return Double.parseDouble(rating);
    }

    public String getUserID() {
        return userID;
    }

    public String getBookID() {
        return bookID;
    }
}
