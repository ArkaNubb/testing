package user;

public abstract class user {
    protected String email;
    protected String userId;
    protected String password;
    protected String name;

    public user(String email, String userId, String password, String name) {
        this.email = email;
        this.userId = userId;
        this.password = password;
        this.name = name;

    }
// I am Pritom
    abstract public void printInfo();
}
