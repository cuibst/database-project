package database.rzotgorz.managesystem.results;

public class QueryResult extends ResultItem {
    private final String type;
    private final int count;

    public QueryResult(String type, int count) {
        this.type = type;
        this.count = count;
    }

    @Override
    public String toString() {
        return "Ok, " + count + " records " + type + ".";
    }
}
