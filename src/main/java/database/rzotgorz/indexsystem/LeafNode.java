package database.rzotgorz.indexsystem;

import com.alibaba.fastjson.JSONObject;
import database.rzotgorz.recordsystem.RID;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

@Data
@Slf4j
public class LeafNode extends TreeNode {
    private List<RID> childRids;
    private long prevId;
    private long nextId;

    private static final ResourceBundle bundle = ResourceBundle.getBundle("configurations");
    private static final int PAGE_SIZE = Integer.parseInt(bundle.getString("PAGE_SIZE"));

    public LeafNode(long pageId, long parentId, long prevId, long nextId, List<RID> childRids, List<Long> childKeys, IndexHandler indexHandler) {
        super(pageId, parentId, childKeys, indexHandler);
//        log.info("pageId:{}", pageId);
        this.prevId = prevId;
        this.nextId = nextId;
        this.childRids = childRids;
        this.nodeType = 1;
    }


    public JSONObject split() {
        int mid = (int) Math.floor((childKeys.size() + 1) / 2.0);
        ArrayList<Long> newKeys = new ArrayList<>();
        newKeys.addAll(this.childKeys.subList(mid, childKeys.size()));
        ArrayList<RID> newRids = new ArrayList<>();
        newRids.addAll(this.childRids.subList(mid, childRids.size()));
        JSONObject object = new JSONObject();
        object.put("newKeys", newKeys);
        object.put("newVal", newRids);
        this.childRids = this.childRids.subList(0, mid);
        this.childKeys = this.childKeys.subList(0, mid);
        object.put("maxKey", newKeys.get(0));
        return object;
    }

    @Override
    public void insert(long key, RID rid) {
        int index = this.upperBound(key);
        if (index == -1)
            index = 0;
        this.childKeys.add(index, key);
        this.childRids.add(index, rid);
//        System.out.println("childkeys:" + childKeys.toString());
    }

    @Override
    public int remove(long key, RID val) {
        int lower = lowerBound(key);
        int upper = upperBound(key);
        if (lower == -1 || upper == -1)
            return -1;
        if (upper != childKeys.size())
            upper++;
//        log.info("actual size:{}", childKeys.size() - (upper - lower));
//        log.info("needed key:{} ,val:{}", key, val.toString());
//        log.info("upper:{},lower:{}", upper, lower);
        for (int index = lower; index < upper; index++) {
//            log.info("actual key:{} ,val:{}", childKeys.get(index), childRids.get(index).toString());
            if (childKeys.get(index).equals(key) && childRids.get(index).equals(val)) {
                childKeys.remove(index);
                childRids.remove(index);
                index--;
                upper--;
//                break;
            }
        }
//        log.info("lower:{},upper:{}", lower, upper);
//        log.info("size:{}", childKeys.size());
        if (childKeys.size() == 0)
            return -1;
        return (int) (long) childKeys.get(0);
    }

    public int pageSize() {
        //FIXME::Why 32???
        return 40 + childKeys.size() * 24 + 32;
    }

    public RID search(long key) {
        int index = lowerBound(key);
        if (index == -1 || index == childKeys.size())
            return null;
        if (childKeys.get(index) == key)
            return childRids.get(index);
        else
            return null;
    }

    public byte[] turnToBytes() {
        Long[] longs = new Long[PAGE_SIZE / 8];
        Arrays.fill(longs, 0L);
        longs[0] = 1L;
        longs[1] = parentId;
        longs[2] = prevId;
        longs[3] = nextId;
        longs[4] = (long) childRids.size();
        for (int i = 0; i < childKeys.size(); i++) {
            longs[i * 3 + 5] = childKeys.get(i);
            longs[i * 3 + 6] = (long) childRids.get(i).getPageId();
            longs[i * 3 + 7] = (long) childRids.get(i).getSlotId();
        }
        return processLongsToBytes(longs);
    }

    public ArrayList<RID> range(int low, int high) {
        ArrayList<RID> res = new ArrayList<>();
        int lower = lowerBound(low);
        int upper = upperBound(high);
        if (lower == -1 || upper == -1)
            return null;
        if (upper != childKeys.size())
            upper++;
        if (lower != 0)
            lower--;
//        log.info("lower:{},upper:{}", lower, upper);
        for (int i = lower; i < upper; i++) {
            if (childKeys.get(i) >= low && childKeys.get(i) <= high)
                res.add(childRids.get(i));
        }
//        log.info("rangeRes:{}", res.toString());
        return res;
    }
}
