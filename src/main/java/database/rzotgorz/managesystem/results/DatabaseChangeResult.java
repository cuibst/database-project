package database.rzotgorz.managesystem.results;

public class DatabaseChangeResult extends ResultItem {
    private final String newDatabaseName;

    public DatabaseChangeResult(String newDatabaseName) {
        this.newDatabaseName = newDatabaseName;
    }

    public String getNewDatabaseName() {
        return newDatabaseName;
    }

    @Override
    public String toString() {
        return "Database changed to " + newDatabaseName;
    }
}
