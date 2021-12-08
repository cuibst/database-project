package database.rzotgorz.metaSystem;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class ColumnInfo {
    private String type;
    private String name;
    private int size;
    private Object defaultValue;

    public ColumnInfo(String type, String name, int size, Object defaultValue) {
        this.type = type;
        this.name = name;
        this.defaultValue = defaultValue;
        if (this.type == "INT") {
            this.size = 5;
        } else if (this.type == "DATE") {
            this.size = 9;
        } else if (this.type == "FLOAT") {
            this.size = 5;
        } else if (this.type == "VARCHAR") {
            this.size = size + 1;
        }
    }

    public int getSize() {
        return size;
    }

    public String description() {
        return String.format("name:{} , type:{} , size:{} , default", name, type, size - 1, defaultValue);
    }

}
