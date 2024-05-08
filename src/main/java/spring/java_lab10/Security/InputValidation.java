package spring.java_lab10.Security;

public class InputValidation {

    public static boolean isValidText(String text) {
        return text.matches("[a-zA-Z0-9_ґҐєЄіІїЇҐґА-Яа-я ]+") && text.length() <= 50;

    }
}
