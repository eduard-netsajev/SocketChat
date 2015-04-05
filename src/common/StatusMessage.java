package common;

public class StatusMessage extends Message {

    public StatusMessage(String author, String text) {
        super(author, text);
    }

    @Override
    public String toString() {
        return getAuthor() + " " + getText();
    }
}