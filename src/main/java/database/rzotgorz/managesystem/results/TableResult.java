package database.rzotgorz.managesystem.results;

import database.rzotgorz.utils.PrettyTable;

import java.util.List;

public class TableResult extends ResultItem {

    private final PrettyTable prettyTable;

    public TableResult(List<String> header, Iterable<List<String>> data) {
        prettyTable = new PrettyTable(header);
        data.forEach(prettyTable::addRow);
    }

    @Override
    public String toString() {
        return prettyTable.toString();
    }
}
