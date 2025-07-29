package common;

import java.io.Serializable;

public abstract class user implements Serializable {
    protected String email;
    protected String userId;
    protected String password;
    protected String name;

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public user(String email, String userId, String password, String name) {
        this.email = email;
        this.userId = userId;
        this.password = password;
        this.name = name;

    }
    @Override
    public String toString() {
        return email + "|" + userId + "|" + name;
    }
    public String getUserId(){
        return userId;
    }
    public String getPassword(){
        return password;
    }
    public String getEmail(){
        return email;
    }
    public String getName(){
        return name;
    }
}
