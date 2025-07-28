package common;

import java.io.Serializable;

public class MemberRequest implements Serializable {
    String bookID;
    String userID;

    public MemberRequest(String bookID, String userID) {
        this.bookID = bookID;
        this.userID = userID;
    }

    public String getUserID() {
        return userID;
    }

    public String getBookID() {
        return bookID;
    }
}
