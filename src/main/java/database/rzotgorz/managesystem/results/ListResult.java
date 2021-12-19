package database.rzotgorz.managesystem.results;

import database.rzotgorz.utils.PrettyTable;

import java.util.Set;

public class ListResult extends ResultItem {

    private final PrettyTable table;

    public ListResult(Set<String> results, String header) {
        table = new PrettyTable(header);
        results.forEach(table::addRow);
    }

    @Override
    public String toString() {
        return table.toString();
    }

}
