package common;

import java.io.Serializable;

public class LibrarianAuthenticate implements Serializable {
    private String userId;
    private String passWord;

    public LibrarianAuthenticate(String userId, String passWord) {
        this.userId = userId;
        this.passWord = passWord;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassWord() {
        return passWord;
    }
}
