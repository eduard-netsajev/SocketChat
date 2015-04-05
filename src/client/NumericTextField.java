package client;

import javafx.scene.control.TextField;

public class NumericTextField extends TextField {

    public NumericTextField() {
        super();
    }

    public NumericTextField(String text) {
        super(text);
    }

    @Override public void replaceText(int start, int end, String text) {
        // Ignore if input would be invalid
        if (text.matches("[0-9]")) {
            super.replaceText(start, end, text);
        }
    }

    @Override public void replaceSelection(String text) {
        if (text.matches("[0-9]")) {
            super.replaceSelection(text);
        }
    }
}
