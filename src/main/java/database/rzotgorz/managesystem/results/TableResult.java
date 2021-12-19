package database.rzotgorz.managesystem.results;

import database.rzotgorz.utils.PrettyTable;

import java.util.List;

public class TableResult extends ResultItem {

    private final List<String> headers;
    private final Iterable<List<String>> data;
    private final PrettyTable prettyTable;

    public TableResult(List<String> header, Iterable<List<String>> data) {
        prettyTable = new PrettyTable(header);
        data.forEach(prettyTable::addRow);
        this.headers = header;
        this.data = data;
    }

    public Iterable<List<String>> getData() {
        return data;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public int getHeaderIndex(String header) {
        return headers.indexOf(header);
    }

    @Override
    public String toString() {
        return prettyTable.toString();
    }
}
