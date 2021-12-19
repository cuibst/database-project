package database.rzotgorz.managesystem.functions;

import java.util.List;

public class NullFunction implements Function{

    private final int index;
    private final boolean isNull;

    public NullFunction(int index, boolean isNull) {
        this.index = index;
        this.isNull = isNull;
    }

    @Override
    public boolean consume(List<Object> record) {
        return (record.get(index) == null) == isNull;
    }
}
