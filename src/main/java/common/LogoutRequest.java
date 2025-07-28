package common;

import java.io.Serializable;

public class LogoutRequest implements Serializable {
    private String userId;

    public LogoutRequest(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}