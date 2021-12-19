package database.rzotgorz.managesystem;

import java.util.List;
import java.util.function.Consumer;

public class Selector {

    public enum SelectorType {
        ALL, FIELD, AGGREGATION, COUNTER
    }

    private final SelectorType type;
    private String tableName;
    private String columnName;

    public SelectorType getType() {
        return type;
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    private interface Aggregator {
        Object calc(List<Object> data);
    }

    public enum AggregatorType {
        MIN, MAX, SUM, AVG, COUNT;

        public int value() {
            switch (this) {
                case MIN:
                    return 0;
                case MAX:
                    return 1;
                case SUM:
                    return 2;
                case AVG:
                    return 3;
                case COUNT:
                    return 4;
                default:
                    return 5;
            }
        }
    }

    private final AggregatorType aggregatorType;

    private static final Aggregator[] aggregators = new Aggregator[5];

    public String target() {
        return tableName + "." + columnName;
    }

    public Selector(SelectorType type, String tableName, String columnName, AggregatorType aggregatorType) {
        this.type = type;
        this.tableName = tableName;
        this.columnName = columnName;
        this.aggregatorType = aggregatorType;
    }

    @Override
    public String toString() {
        switch (type) {
            case FIELD:
                return target();
            case COUNTER:
                return "COUNT(*)";
            case AGGREGATION:
                return String.format("%s(%s)", aggregatorType.toString(), target());
            default:
                return "";
        }
    }

    public Object select(List<Object> data) {
        switch (type) {
            case FIELD:
                return data.get(0);
            case COUNTER:
                return data.size();
            case AGGREGATION:
                return aggregators[aggregatorType.value()].calc(data);
            default:
                return null;
        }
    }

    static {
        aggregators[0] = data -> {
            float result = Float.MAX_VALUE;
            for(Object obj : data) {
                if(obj == null || (obj.getClass() != Integer.class && obj.getClass() != Float.class))
                    continue;
                if(obj.getClass() == Integer.class)
                    result = Math.min(result, (Integer) obj);
                else
                    result = Math.min(result, (Float) obj);
            }
            return result;
        };

        aggregators[1] = data -> {
            float result = Float.MIN_VALUE;
            for(Object obj : data) {
                if(obj == null || (obj.getClass() != Integer.class && obj.getClass() != Float.class))
                    continue;
                if(obj.getClass() == Integer.class)
                    result = Math.max(result, (Integer) obj);
                else
                    result = Math.max(result, (Float) obj);
            }
            return result;
        };

        aggregators[2] = data -> {
            float result = 0f;
            for(Object obj : data) {
                if(obj == null || (obj.getClass() != Integer.class && obj.getClass() != Float.class))
                    continue;
                if(obj.getClass() == Integer.class)
                    result += (Integer)obj;
                else
                    result = (Float)obj;
            }
            return result;
        };

        aggregators[3] = data -> {
            float result = 0f;
            int cnt = 0;
            for(Object obj : data) {
                if(obj == null || (obj.getClass() != Integer.class && obj.getClass() != Float.class))
                    continue;
                if(obj.getClass() == Integer.class)
                    result += (Integer)obj;
                else
                    result = (Float)obj;
                cnt ++;
            }
            return result / cnt;
        };

        aggregators[4] = data -> {
            int cnt = 0;
            for(Object obj : data) {
                if(obj == null)
                    continue;
                cnt ++;
            }
            return cnt;
        };

    }

}
