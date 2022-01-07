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
        int result;
        if(value.getClass() != target.getClass()) {
            Float value1, target1;
            target1 = getaFloat(target);
            value1 = getaFloat(value);
            result = target1.compareTo(value1);
        } else result = ((Comparable)target).compareTo(value);
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

    private Float getaFloat(Object value) {
        float value1;
        if (Integer.class.equals(value.getClass())) {
            value1 = ((Integer) value).floatValue();
        } else if(Float.class.equals(value.getClass())) {
            value1 = (Float) value;
        } else if(Long.class.equals(value.getClass())) {
            value1 = ((Long) value).floatValue();
        } else {
            throw new RuntimeException("Value type mismatch");
        }
        return value1;
    }
}
