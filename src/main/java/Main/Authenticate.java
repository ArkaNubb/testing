package Main;

import java.io.Serializable;

public class Authenticate implements Serializable {
    private String userId;
    private String passWord;

    public Authenticate(String userId, String passWord) {
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
