package database.rzotgorz.managesystem.clauses;

public class ValueOperatorClause extends OperatorClause {
    private final Object value;

    public ValueOperatorClause(String tableName, String columnName, String operator, Object value) {
        super(tableName, columnName, operator);
        this.value = value;
    }
}
