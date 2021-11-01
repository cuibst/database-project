package database.rzotgorz.exceptions;

public class FailToOpenException extends OpenFileException{
    public FailToOpenException(String filename) {
        super("Failed to open file " + filename + ".");
    }
}
