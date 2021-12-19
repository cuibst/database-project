package database.rzotgorz.metaSystem;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Data
@Slf4j
public class ColumnInfo implements Serializable {
    private String type;
    private String name;
    private Integer size;
    private Object defaultValue;

    public ColumnInfo(String type, String name, int size, Object defaultValue) {
        this.type = type;
        this.name = name;
        this.defaultValue = defaultValue;
        if (this.type.equals("INT")) {
            this.size = 5;
        } else if (this.type.equals("DATE")) {
            this.size = 9;
        } else if (this.type.equals("FLOAT")) {
            this.size = 5;
        } else if (this.type.equals("VARCHAR")) {
            this.size = size + 1;
        }
    }

    public int getSize() {
        return size;
    }

    public String description() {
        return String.format("name:%s , type:%s , size:%d , default", name, type, size - 1, defaultValue);
    }

    public String[] getDescription() {
        String[] object = new String[6];
        object[0] = this.name;
        if (!this.type.equals("VARCHAR"))
            object[1] = this.type;
        else
            object[1] = String.format("%s(%d)", this.type, this.size);
        object[2] = "NO";
        object[3] = "";
        object[4] = this.defaultValue == null ? "null" : this.defaultValue.toString();
        object[5] = "";
        return object;
    }

}
