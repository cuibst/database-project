package database.rzotgorz.managesystem.clauses;

import java.util.List;

public class WhereInClause extends WhereClause {
    private final List<Object> values;

    public WhereInClause(String tableName, String columnName, List<Object> values) {
        super(tableName, columnName);
        this.values = values;
    }
}
