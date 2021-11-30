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
public class FileIndex {
    private int rootId;
    private IndexHandler indexHandler;
    private boolean modified;
    private TreeNode rootNode;

    private static final ResourceBundle bundle = ResourceBundle.getBundle("configurations");
    private static final int PAGE_SIZE = Integer.parseInt(bundle.getString("PAGE_SIZE"));

    private long[] processByte(byte[] data) {
        assert (data.length % 8 == 0);
        long[] longData = new long[data.length / 8];
        byte[] process = new byte[8];
        for (int i = 0; i < data.length; i++) {
            process[i % 8] = data[i];
            if ((i + 1) % 8 == 0) {
                long num = ByteLongConverter.bytes2Long(process);
                longData[(i + 1) / 8 - 1] = num;
            }
        }
        return longData;
    }

    public FileIndex(int rootId, IndexHandler handler) {
        this.rootId = rootId;
        this.modified = false;
        this.indexHandler = handler;
        this.rootNode = new InterNode(rootId, -1, new ArrayList<>(), new ArrayList<>(), indexHandler);
    }

    public TreeNode buildNode(int pageId) {
        this.modified = true;
        byte[] dataBytes = indexHandler.getPage(pageId);
//        for (int i = 0; i < dataBytes.length; i++)
//            if (dataBytes[i] != 0)
//                log.info("dataBytes:{}", dataBytes[i]);
//            System.out.println(dataBytes[i]);
        long[] data = processByte(dataBytes);
        TreeNode node;
        long parentId = data[1];
        if (data[0] == 1) {
            long prevId = data[2];
            long nextId = data[3];
            long childLen = data[4];
            List<Long> childKeys = new ArrayList<>();
            for (int i = 0; i < childLen; i++)
                childKeys.add(data[i * 3 + 5]);
            List<RID> childRids = new ArrayList<>();
            for (int i = 0; i < childLen; i++)
                childRids.add(new RID(data[i * 3 + 6], data[i * 3 + 7]));
            assert (childKeys.size() == childRids.size());
            node = new LeafNode(pageId, parentId, prevId, nextId, childRids, childKeys, this.indexHandler);
        } else {
            long childLen = data[2];
            List<Long> childKeys = new ArrayList<>();
            for (int i = 0; i < childLen; i++)
                childKeys.add(data[i * 2 + 3]);
            List<TreeNode> childNodes = new ArrayList<>();
            for (int i = 0; i < childLen; i++)
                childNodes.add(this.buildNode((int) data[i * 2 + 4]));
            node = new InterNode(pageId, parentId, childNodes, childKeys, this.indexHandler);
        }
        return node;
    }

    public void insert(long key, RID rid) {
        this.modified = true;
        this.rootNode.insert(key, rid);
        if (this.rootNode.pageSize() > PAGE_SIZE) {
            int newPageId = indexHandler.createNewPage();
            InterNode root = new InterNode(newPageId, -1, new ArrayList<>(), new ArrayList<>(), indexHandler);
            this.rootNode.parentId = newPageId;
            int newPageStoreId = indexHandler.createNewPage();
            JSONObject object = this.rootNode.split();
            long minKey = rootNode.childKeys.get(0);
            List<Long> newKeys = (ArrayList<Long>) object.get("newKeys");
            List<TreeNode> newNodes = (ArrayList<TreeNode>) object.get("newNodes");
            InterNode node = new InterNode(newPageStoreId, newPageId, newNodes, newKeys, indexHandler);
            long maxKey = Integer.parseInt(object.get("maxKey").toString());
            root.childKeys.add(minKey);
            root.childKeys.add(maxKey);
            root.addChildNodes(this.rootNode);
            root.addChildNodes(node);
            this.rootNode = root;
        }
    }

    public void load() {
        byte[] dataBytes = indexHandler.getPage(this.rootId);
        long[] data = processByte(dataBytes);
        long nodeType = data[0];
        long parentId = data[1];
        assert (nodeType == 0);
        assert (parentId == -1);
        this.rootNode = this.buildNode(rootId);
    }

    public void build(List<Long> childKeys, List<RID> childRids) {
        this.modified = true;
        assert (childKeys.size() == childRids.size());
        for (int i = 0; i < childKeys.size(); i++)
            this.insert(childKeys.get(i), childRids.get(i));
    }

    public void dump() {
        List<TreeNode> needHandle = new ArrayList<>();
        needHandle.add(this.rootNode);
        int tail = 0;
        while (needHandle.size() != tail) {
            TreeNode t = needHandle.get(tail);
            if (t.getClass() == InterNode.class) {
                for (int i = 0; i < ((InterNode) t).getChildNodes().size(); i++)
                    needHandle.add(((InterNode) t).getChildNodes().get(i));
            }
            indexHandler.putPage((int) t.pageId, t.turnToBytes());
            tail++;
        }
    }

    public void remove(long key, RID rid) {
        this.modified = true;
        this.rootNode.remove(key, rid);
    }

    public RID search(long key) {
        return this.rootNode.search(key);
    }

    public ArrayList<RID> range(int low, int high) {
        return this.rootNode.range(low, high);
    }
}
