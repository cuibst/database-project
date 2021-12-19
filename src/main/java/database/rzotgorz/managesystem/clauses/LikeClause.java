package database.rzotgorz.managesystem.clauses;

public class LikeClause extends WhereClause {
    private final String pattern;

    public LikeClause(String tableName, String columnName, String pattern) {
        super(tableName, columnName);
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }
}
