package database.rzotgorz.managesystem.clauses;

public class NullClause extends WhereClause{
    private boolean isNull;

    public NullClause(String tableName, String columnName, boolean isNull) {
        super(tableName, columnName);
        this.isNull = isNull;
    }

    public boolean isNull() {
        return isNull;
    }
}
