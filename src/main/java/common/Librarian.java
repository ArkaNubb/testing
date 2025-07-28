package common;

import java.io.*;


public class Librarian extends user implements Serializable{

    public Librarian(String email, String userId, String password, String name) throws IOException {
        super(email, userId, password, name);
    }
}
