package user;

public abstract class user {
    protected String userId;
    protected String name;
    protected String email;
    protected String password;

    public user(String email, String userId, String name, String password) {
        this.email = email;
        this.userId = userId;
        this.name = name;
        this.password = password;
    }

    abstract public void printInfo();
}
