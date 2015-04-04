package common;

import java.io.Serializable;

public class Message implements Serializable {

    private String author;
    private String text;

    public Message(String author, String text) {
        this.author = author;
        this.text = text;
    }

    public String getAuthor() {
        return author;
    }

    public String getText() {
        return text;
    }

    public String toString() {
        return author + ": " + text;
    }
}