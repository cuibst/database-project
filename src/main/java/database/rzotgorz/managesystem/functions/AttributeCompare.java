package database.rzotgorz.managesystem.functions;

import java.util.List;

public class AttributeCompare implements Function {

    private final int index1;
    private final int index2;
    private final String operator;

    public AttributeCompare(int index1, int index2, String operator) {
        this.index1 = index1;
        this.index2 = index2;
        this.operator = operator;
    }

    @Override
    public boolean consume(List<Object> record) {
        Object left = record.get(index1);
        Object right = record.get(index2);
        switch (operator) {
            case "=":
                return left.equals(right);
            case "<>":
                return !left.equals(right);
            default:
                break;
        }
        if(!(left instanceof Comparable) || !(right instanceof Comparable))
            return false;
        int result = ((Comparable)left).compareTo(right);
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
