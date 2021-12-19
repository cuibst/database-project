package database.rzotgorz.managesystem.clauses;

public class ColumnOperatorClause extends OperatorClause{
    private String targetTable;
    private String targetColumn;

    public ColumnOperatorClause(String tableName, String columnName, String operator, String targetTable, String targetColumn) {
        super(tableName, columnName, operator);
        this.targetTable = targetTable;
        this.targetColumn = targetColumn;
    }

    @Override
    public String getTargetTable() {
        return targetTable;
    }

    @Override
    public String getTargetColumn() {
        return targetColumn;
    }

    @Override
    public void setTargetTable(String targetTable) {
        this.targetTable = targetTable;
    }

    @Override
    public void setTargetColumn(String targetColumn) {
        this.targetColumn = targetColumn;
    }
}
