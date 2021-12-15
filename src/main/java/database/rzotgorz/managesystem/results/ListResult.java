package database.rzotgorz.managesystem.results;

import java.util.Set;

public class ListResult extends ResultItem {

    private final String header;

    private final Set<String> results;

    public ListResult(Set<String> results, String header) {
        this.header = header;
        this.results = results;
    }

    @Override
    public String toString() {
        int mx = header.length() + 4;
        for (String name : results) {
            mx = Math.max(mx, name.length() + 4);
        }
        final int tabWidth = mx;
        StringBuilder builder = new StringBuilder();
        builder.append('+');
        builder.append("-".repeat(tabWidth - 2));
        builder.append("+\n");
        builder.append("| ");
        builder.append(header);
        builder.append(" ".repeat(tabWidth - header.length() - 3));
        builder.append("|\n");
        builder.append('+');
        builder.append("-".repeat(tabWidth - 2));
        builder.append("+\n");
        results.forEach(name -> {
            builder.append("| ");
            builder.append(name);
            builder.append(" ".repeat(Math.max(0, tabWidth - name.length() - 3)));
            builder.append("|\n");
        });
        builder.append('+');
        builder.append("-".repeat(tabWidth - 2));
        builder.append("+\n");
        return builder.toString();
    }
}
