package database.rzotgorz.indexsystem;

import com.alibaba.fastjson.JSONObject;
import database.rzotgorz.recordsystem.RID;
import database.rzotgorz.utils.ByteLongConverter;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@Data
@Slf4j
@NoArgsConstructor
public class TreeNode {
    protected long pageId;
    protected long parentId;
    protected List<IndexContent> childKeys;
    protected IndexHandler indexHandler;
    protected int nodeType;
    private static final ResourceBundle bundle = ResourceBundle.getBundle("configurations");
    private static final int PAGE_SIZE = Integer.parseInt(bundle.getString("PAGE_SIZE"));
    protected int typeSize;
    protected List<String> indexType;


    public TreeNode(long pageId, long parentId, List<IndexContent> childKeys, IndexHandler indexHandler, int size, List<String> indexType) {
        this.pageId = pageId;
        this.childKeys = childKeys;
        this.parentId = parentId;
        this.indexHandler = indexHandler;
        this.typeSize = size;
        this.indexType = indexType;
    }

    public int lowerBound(IndexContent key) {
        if (childKeys.size() == 0)
            return -1;
        int head = 0;
        int tail = childKeys.size() - 1;
        int pos = childKeys.size() - 1;
        while (head < tail) {
            int mid = (int) Math.floor((head + tail) / 2.0);
            if (childKeys.get(mid).compareTo(key) < 0)
                head = mid + 1;
            else
                tail = mid;
        }
        if (childKeys.get(head).compareTo(key) >= 0)
            pos = head;
        return pos;
    }

    public int upperBound(IndexContent key) {
        if (childKeys.size() == 0)
            return -1;
        int head = 0;
        int tail = childKeys.size() - 1;
        int pos = childKeys.size();
        while (head < tail) {
            int mid = (int) Math.floor((head + tail) / 2.0);
            if (childKeys.get(mid).compareTo(key) <= 0)
                head = mid + 1;
            else {
                tail = mid;
                pos = tail;
            }
        }
        if (childKeys.get(head).compareTo(key) > 0)
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


    public void insert(IndexContent key, RID rid) {
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

//    public void insert(IndexContent key, TreeNode node) {
//        log.info("ERROR!!! invalid funciton used in base class");
//    }

    public IndexContent remove(IndexContent key, RID val) {
        log.info("ERROR!!! invalid funciton used in base class");
        return null;
    }

    public byte[] turnToBytes() {
        log.info("ERROR!!! invalid funciton used in base class");
        return null;
    }

    public RID search(IndexContent key) {
        log.info("ERROR!!! invalid funciton used in base class");
        return null;
    }

    public ArrayList<RID> range(IndexContent low, IndexContent high) {
        return null;
    }
}
