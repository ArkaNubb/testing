package common;

import java.io.Serializable;
import java.util.List;

public class AuthorPackage implements Serializable {
    Author author;

    public AuthorPackage(Author author) {
        this.author = author;
    }
    public Author getAuthor() {
        return author;
    }
}
