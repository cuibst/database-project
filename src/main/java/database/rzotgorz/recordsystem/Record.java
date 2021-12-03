package database.rzotgorz.recordsystem;

import lombok.Data;

@Data
public class Record {
    private RID rid;
    private byte[] data;

    public Record() {

    }

    public Record(RID rid, byte[] data) {
        this.rid = rid;
        this.data = data;
    }

    public RID getRid() {
        return rid;
    }

    public void setRid(RID rid) {
        this.rid = rid;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
