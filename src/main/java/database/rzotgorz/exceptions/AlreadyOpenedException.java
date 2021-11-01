package database.rzotgorz.exceptions;

public class AlreadyOpenedException extends OpenFileException{
    public AlreadyOpenedException(String filename) {
        super(filename + " has already been opened");
    }
}
