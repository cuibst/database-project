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
    private boolean init = false;
    private int size = -1;

    public TableResult(List<String> header, Iterable<List<Object>> data) {
        prettyTable = new PrettyTable(header);
        this.headers = header;
        this.data = data;
        if(data instanceof List)
            size = ((List) data).size();
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

    private void initTable() {
        data.forEach(list -> {
            List<String> strings = new ArrayList<>();
            list.forEach(obj -> strings.add((obj == null) ? "NULL" : obj.toString()));
            prettyTable.addRow(strings);
        });
    }

    public int getSize() {
        if(size != -1)
            return size;
        size = 0;
        for (List<Object> datum : data) {
            size++;
        }
        return size;
    }

    @Override
    public String toString() {
        if(!init) {
            init = true;
            initTable();
        }
        return prettyTable.toString();
    }
}
