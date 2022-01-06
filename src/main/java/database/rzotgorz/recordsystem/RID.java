package database.rzotgorz.recordsystem;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RID implements Comparable<RID> {
    private int pageId;
    private int slotId;
    public RID(int pageId,int slotId){
        this.pageId=pageId;
        this.slotId=slotId;
    }

    public RID(long pageId,long slotId){
        this.pageId=(int)pageId;
        this.slotId=(int)slotId;
    }

    public int getPageId() {
        return pageId;
    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
    }

    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    @Override
    public String toString() {
        return String.format("(%d, %d)", pageId, slotId);
    }

    @Override
    public int compareTo(RID o) {
        if(pageId != o.pageId)
            return pageId < o.pageId ? -1 : 1;
        else if(slotId != o.slotId)
            return slotId < o.slotId ? -1 : 1;
        return 0;
    }
}
