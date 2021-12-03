package database.rzotgorz.indexsystem;

import com.alibaba.fastjson.JSONObject;
import database.rzotgorz.recordsystem.RID;
import database.rzotgorz.utils.ByteLongConverter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@Data
@Slf4j
public class TreeNode {
    protected long pageId;
    protected long parentId;
    protected List<Long> childKeys;
    protected IndexHandler indexHandler;
    protected int nodeType;
    private static final ResourceBundle bundle = ResourceBundle.getBundle("configurations");
    private static final int PAGE_SIZE = Integer.parseInt(bundle.getString("PAGE_SIZE"));

    public TreeNode() {
    }

    public TreeNode(long pageId, long parentId, List<Long> childKeys, IndexHandler indexHandler) {
        this.pageId = pageId;
        this.childKeys = childKeys;
        this.parentId = parentId;
        this.indexHandler = indexHandler;
    }

    public int lowerBound(long key) {
//        log.info("key: {}", key);
//        System.out.println(childKeys);
        if (childKeys.size() == 0)
            return -1;
        int head = 0;
        int tail = childKeys.size() - 1;
        int pos = childKeys.size() - 1;
        while (head < tail) {
            int mid = (int) Math.floor((head + tail) / 2.0);
            if (childKeys.get(mid) < key)
                head = mid + 1;
            else
                tail = mid;
        }
        if (childKeys.get(head) >= key)
            pos = head;
//        log.info("pos :{}", pos);
        return pos;
    }

    public int upperBound(long key) {
        if (childKeys.size() == 0)
            return -1;
        int head = 0;
        int tail = childKeys.size() - 1;
        int pos = childKeys.size();
        while (head < tail) {
            int mid = (int) Math.floor((head + tail) / 2.0);
            if (childKeys.get(mid) <= key)
                head = mid + 1;
            else {
                tail = mid;
                pos = tail;
            }
        }
        if (childKeys.get(head) > key)
            pos = head;
        return pos;
    }

    public byte[] processLongsToBytes(Long[] longs) {
        byte[] bytes = new byte[PAGE_SIZE];
        for (int i = 0; i < longs.length; i++) {
            byte[] processByte = ByteLongConverter.long2Bytes(longs[i]);
            for (int j = 0; j < 8; j++) {
                bytes[i * 8 + j] = processByte[j];
            }
        }
        return bytes;
    }


    public void insert(long key, RID rid) {
        log.info("ERROR!!! invalid funciton used in base class");
    }

    public int pageSize() {
        log.info("ERROR!!! invalid funciton used in base class");
        return -1;
    }

    public JSONObject split() {
        log.info("ERROR!!! invalid funciton used in base class");
        return null;
    }

    public void insert(long key, TreeNode node) {
        log.info("ERROR!!! invalid funciton used in base class");
    }

    public int remove(long key, RID val) {
        log.info("ERROR!!! invalid funciton used in base class");
        return -1;
    }

    public byte[] turnToBytes() {
        log.info("ERROR!!! invalid funciton used in base class");
        return null;
    }

    public RID search(long key) {
        log.info("ERROR!!! invalid funciton used in base class");
        return null;
    }

    public ArrayList<RID> range(int low, int high) {
        return null;
    }
}
