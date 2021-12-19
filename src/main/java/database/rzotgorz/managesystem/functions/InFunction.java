package database.rzotgorz.managesystem.functions;

import java.util.List;
import java.util.Set;

public class InFunction implements Function {

    private final Set<Object> values;
    private final int index;

    public InFunction(Set<Object> values, int index) {
        this.values = values;
        this.index = index;
    }

    @Override
    public boolean consume(List<Object> record) {
        Object target = record.get(index);
        return values.contains(target);
    }
}
