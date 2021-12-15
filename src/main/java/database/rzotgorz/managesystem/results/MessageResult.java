package database.rzotgorz.managesystem.results;

public class MessageResult extends ResultItem {
    private final String message;
    private final boolean error;

    public MessageResult(String message) {
        this.message = message;
        error = false;
    }

    public MessageResult(String message, boolean error) {
        this.message = message;
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public boolean isError() {
        return error;
    }

    @Override
    public String toString() {
        return error ? "Error: " + message : message;
    }
}
