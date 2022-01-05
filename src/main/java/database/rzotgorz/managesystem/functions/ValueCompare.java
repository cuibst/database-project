package database.rzotgorz.managesystem.functions;

import java.util.List;

public class ValueCompare implements Function{
    private final Object value;
    private final int index;
    private final String operator;

    public ValueCompare(Object value, int index, String operator) {
        this.value = value;
        this.index = index;
        this.operator = operator;
    }

    @Override
    public boolean consume(List<Object> record) {
        Object target = record.get(index);
        if(target == null)
            return false;
        switch (operator) {
            case "=":
                return target.equals(value);
            case "<>":
                return !target.equals(value);
            default:
                break;
        }
        if(!(target instanceof Comparable))
            return false;
        int result = ((Comparable)target).compareTo(value);
        switch (operator) {
            case "<" :
                return result < 0;
            case "<=" :
                return result <= 0;
            case ">" :
                return result > 0;
            case ">=" :
                return result >= 0;
            default :
                return false;
        }
    }
}
