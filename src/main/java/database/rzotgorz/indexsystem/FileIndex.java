package database.rzotgorz.indexsystem;


import com.alibaba.fastjson.JSONObject;
import database.rzotgorz.recordsystem.RID;
import database.rzotgorz.utils.ByteLongConverter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
    private List<String> indexType;
    private String dbName;
    private String indexName;
    private String tableName;
    private static final ResourceBundle bundle = ResourceBundle.getBundle("configurations");
    private static final int PAGE_SIZE = Integer.parseInt(bundle.getString("PAGE_SIZE"));
    private int typeSize;

    public void setIndexType(List<String> types) {
        if (indexType == null) {
            indexType = types;
            typeSize = IndexUtility.calcSize(types);
        } else {
            throw new RuntimeException("IndexType Already set!");
        }
    }


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

    public FileIndex(int rootId, IndexHandler handler, String dbName, List<String> types, String tableName, String indexName) {
        this.rootId = rootId;
        this.tableName = tableName;
        this.modified = false;
        this.indexHandler = handler;
        this.dbName = dbName;
        this.indexType = types;
        this.typeSize = IndexUtility.calcSize(types);
        this.indexName = indexName;
        this.rootNode = new InterNode(rootId, -1, new ArrayList<>(), new ArrayList<>(), indexHandler, this.typeSize, this.indexType);
    }

    public FileIndex(int rootId, IndexHandler handler, String dbName, String tableName, String indexName) {
        this.tableName = tableName;
        this.rootId = rootId;
        this.modified = false;
        this.indexHandler = handler;
        this.dbName = dbName;
        this.indexName = indexName;
        this.load();
    }

    public TreeNode buildNode(int pageId) {
        this.modified = true;
        byte[] dataBytes = indexHandler.getPage(pageId);
        byte[] msgBytes = new byte[40];
        System.arraycopy(dataBytes, 0, msgBytes, 0, 40);
        long[] data = processByte(msgBytes);
        TreeNode node;
        long parentId = data[1];
        if (data[0] == 1) {
            int head = 40;
            long prevId = data[2];
            long nextId = data[3];
            long childLen = data[4];
            List<IndexContent> childKeys = new ArrayList<>();
            for (int i = 0; i < childLen; i++) {
                byte[] bytes = new byte[this.typeSize];
                System.arraycopy(dataBytes, head, bytes, 0, this.typeSize);
                childKeys.add(IndexUtility.parserBytes(bytes, this.indexType));
                head = head + this.typeSize + 16;
            }
            List<RID> childRids = new ArrayList<>();
            head = 40 + this.typeSize;
            for (int i = 0; i < childLen; i++) {
                byte[] pageIdByte = new byte[8];
                byte[] slotIdByte = new byte[8];
                System.arraycopy(dataBytes, head, pageIdByte, 0, 8);
                System.arraycopy(dataBytes, head + 8, slotIdByte, 0, 8);
                childRids.add(new RID(ByteLongConverter.bytes2Long(pageIdByte), ByteLongConverter.bytes2Long(slotIdByte)));
                head = head + this.typeSize + 16;
            }
            assert (childKeys.size() == childRids.size());
            node = new LeafNode(pageId, parentId, prevId, nextId, childRids, childKeys, this.indexHandler, this.typeSize, this.indexType);
        } else {
            long childLen = data[2];
            List<IndexContent> childKeys = new ArrayList<>();
            int head = 24;
            for (int i = 0; i < childLen; i++) {
                byte[] bytes = new byte[this.typeSize];
                System.arraycopy(dataBytes, head, bytes, 0, this.typeSize);
                childKeys.add(IndexUtility.parserBytes(bytes, this.indexType));
                head = head + this.typeSize + 8;
            }
            head = 24 + this.typeSize;
            List<TreeNode> childNodes = new ArrayList<>();
            for (int i = 0; i < childLen; i++) {
                byte[] childNode = new byte[8];
                System.arraycopy(dataBytes, head, childNode, 0, 8);
                childNodes.add(this.buildNode((int) ByteLongConverter.bytes2Long(childNode)));
                head += this.typeSize + 8;
            }
//            log.info("childKeys:{}", childKeys);
//            log.info("childNodes:{}", childNodes);
            node = new InterNode(pageId, parentId, childNodes, childKeys, this.indexHandler, this.typeSize, this.indexType);
        }
        return node;
    }

    private static int cnt = 0;

    public void insert(IndexContent key, RID rid) {
//        if(key.indexList.size() == 1 && key.indexList.get(0).equals(3)) {
//            cnt++;
//            log.info("{}", cnt);
//        }
        this.modified = true;
        this.rootNode.insert(key, rid);
        if (this.rootNode.pageSize() > PAGE_SIZE) {
//            log.info("root split");
            int newPageId = indexHandler.createNewPage();
            InterNode root = new InterNode(this.rootId, -1, new ArrayList<>(), new ArrayList<>(), indexHandler, this.typeSize, this.indexType);
            this.rootNode.parentId = this.rootId;
            this.rootNode.setPageId(newPageId);
            int newPageStoreId = indexHandler.createNewPage();
            JSONObject object = this.rootNode.split();
            IndexContent minKey = rootNode.childKeys.get(0);
            List<IndexContent> newKeys = (ArrayList<IndexContent>) object.get("newKeys");
            List<TreeNode> newNodes = (ArrayList<TreeNode>) object.get("newVal");
            InterNode node = new InterNode(newPageStoreId, newPageId, newNodes, newKeys, indexHandler, this.typeSize, this.indexType);
            IndexContent maxKey = (IndexContent) object.get("maxKey");
            root.childKeys.add(minKey);
            root.childKeys.add(maxKey);
            root.addChildNodes(this.rootNode);
            root.addChildNodes(node);
            root.setPageId(this.rootId);
            this.rootNode = root;
        }
    }

    public void load() {
//        byte[] dataBytes = indexHandler.getPage(this.rootId);
//        long[] data = processByte(dataBytes);
//        long nodeType = data[0];
//        long parentId = data[1];
//        assert (nodeType == 0);
//        assert (parentId == -1);
        String typePath = "data" + File.separator + dbName + File.separator + tableName + indexName + ".type";
        File file = new File(typePath);
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            byte[] str = bis.readAllBytes();
            bis.close();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < str.length; i++) {
                builder.append((char) str[i]);
            }
            String[] strs = builder.toString().split(",");
            List<String> list = new ArrayList<>();
            for (int i = 0; i < strs.length; i++) {
                list.add(strs[i]);
            }
            this.indexType = list;
            typeSize = IndexUtility.calcSize(list);
        } catch (IOException e) {
            throw new RuntimeException("IndexType not exists!! This table cannot be used");
        }
//        this.rootNode = new InterNode(rootId, -1, new ArrayList<>(), new ArrayList<>(), indexHandler, this.typeSize, this.indexType);
//        log.info("rootId:{}", rootId);
        this.rootNode = this.buildNode(rootId);
    }

    public void build(List<IndexContent> childKeys, List<RID> childRids) {
        this.modified = true;
        assert (childKeys.size() == childRids.size());
        for (int i = 0; i < childKeys.size(); i++)
            this.insert(childKeys.get(i), childRids.get(i));
    }

    public void dump() {
        List<TreeNode> needHandle = new ArrayList<>();
        String typePath = "data" + File.separator + dbName + File.separator + tableName + indexName + ".type";
        File file = new File(typePath);
        try {
            FileOutputStream fis = new FileOutputStream(file);
            BufferedOutputStream bis = new BufferedOutputStream(fis);
            String s = "";
            for (int i = 0; i < indexType.size(); i++) {
                s += indexType.get(i) + ",";
            }
            byte[] str = s.getBytes(StandardCharsets.UTF_8);
            bis.write(str);
            bis.close();
        } catch (IOException e) {
            throw new RuntimeException("IndexType not exists!! This table cannot be used");
        }
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

    public void remove(IndexContent key, RID rid) {
        this.modified = true;
        this.rootNode.remove(key, rid);
    }

    public List<RID> search(IndexContent key) {
        return this.rootNode.search(key);
    }

    public ArrayList<RID> range(IndexContent low, IndexContent high, boolean left, boolean right) {
//        log.info(low.toString());
//        log.info(high.toString());
        return this.rootNode.range(low, high, left, right);
    }
}
