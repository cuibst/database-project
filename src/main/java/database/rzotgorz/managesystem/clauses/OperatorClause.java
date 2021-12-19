package database.rzotgorz.managesystem.clauses;

public class OperatorClause extends WhereClause{
    private final String operator;

    public OperatorClause(String tableName, String columnName, String operator) {
        super(tableName, columnName);
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }
}
