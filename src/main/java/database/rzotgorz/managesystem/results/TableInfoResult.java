package database.rzotgorz.managesystem.results;

import database.rzotgorz.metaSystem.TableInfo;

public class TableInfoResult extends ResultItem {
    public TableInfo info;
    public String tableName;

    public TableInfoResult(TableInfo info, String tableName) {
        this.info = info;
        this.tableName = tableName;
    }

}
