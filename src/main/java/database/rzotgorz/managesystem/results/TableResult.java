package database.rzotgorz.managesystem.results;

import database.rzotgorz.utils.PrettyTable;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TableResult extends ResultItem {

    private final List<String> headers;
    private final Iterable<List<Object>> data;
    private final PrettyTable prettyTable;

    public TableResult(List<String> header, Iterable<List<Object>> data) {
        prettyTable = new PrettyTable(header);
        data.forEach(list -> {
            List<String> strings = new ArrayList<>();
            list.forEach(obj -> strings.add(obj.toString()));
            prettyTable.addRow(strings);
        });
        this.headers = header;
        this.data = data;
    }

    public Iterable<List<Object>> getData() {
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
