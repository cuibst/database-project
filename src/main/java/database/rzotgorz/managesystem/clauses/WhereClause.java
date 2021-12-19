package database.rzotgorz.managesystem.clauses;

public class WhereClause {
    private String tableName;
    private String columnName;

    public WhereClause(String tableName, String columnName) {
        this.tableName = tableName;
        this.columnName = columnName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getTargetTable() {
        return null;
    }

    public String getTargetColumn() {
        return null;
    }

    public void setTargetTable(String targetTable) {}

    public void setTargetColumn(String targetColumn) {}
}
