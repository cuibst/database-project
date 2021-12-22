package database.rzotgorz.managesystem.results;

public class DescriptionResult extends ResultItem {
    private final TableResult columnInfo;
    private final TableResult keysInfo;

    public DescriptionResult(TableResult columnInfo, TableResult keysInfo) {
        this.columnInfo = columnInfo;
        this.keysInfo = keysInfo;
    }

    @Override
    public String toString() {
        return columnInfo.toString() + keysInfo.toString() + "\n";
    }
}
