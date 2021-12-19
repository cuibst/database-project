package database.rzotgorz.managesystem.clauses;

import java.util.List;
import java.util.Set;

public class WhereInClause extends WhereClause {
    private final Set<Object> values;

    public WhereInClause(String tableName, String columnName, List<Object> values) {
        super(tableName, columnName);
        this.values = Set.copyOf(values);
    }

    public Set<Object> getValues() {
        return values;
    }
}
