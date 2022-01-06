package database.rzotgorz.indexsystem;

import com.alibaba.fastjson.JSONObject;
import database.rzotgorz.recordsystem.RID;
import database.rzotgorz.utils.ByteLongConverter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

@Data
@Slf4j
public class InterNode extends TreeNode {
    public List<TreeNode> childNodes;

    private static final ResourceBundle bundle = ResourceBundle.getBundle("configurations");
    private static final int PAGE_SIZE = Integer.parseInt(bundle.getString("PAGE_SIZE"));

    public InterNode(long pageId, long parentId, List<TreeNode> childNodes, List<IndexContent> childKeys, IndexHandler indexHandler, int size, List<String> list) {
        super(pageId, parentId, childKeys, indexHandler, size, list);
        this.childNodes = childNodes;
        this.nodeType = 0;
    }

    public void addChildNodes(TreeNode t) {
        childNodes.add(t);
    }

    public JSONObject split() {
        int mid = (int) Math.floor((childKeys.size() + 1) / 2.0);
        List<IndexContent> newKeys = new ArrayList<>();
        newKeys.addAll(this.childKeys.subList(mid, childKeys.size()));
        List<TreeNode> newNodes = new ArrayList<>();
        newNodes.addAll(this.childNodes.subList(mid, childNodes.size()));
        JSONObject object = new JSONObject();
        object.put("newKeys", newKeys);
        object.put("newVal", newNodes);
        this.childNodes = this.childNodes.subList(0, mid);
        this.childKeys = this.childKeys.subList(0, mid);
        object.put("maxKey", newKeys.get(0));
        return object;
    }

    @Override
    public void insert(IndexContent key, RID rid) {
        int index = this.lowerBound(key);
        if (index == -1) {
            this.childKeys.add(key);
            int nodePageId = indexHandler.createNewPage();
            TreeNode node = new LeafNode(nodePageId, this.pageId, 0, 0, new ArrayList<RID>(), new ArrayList<IndexContent>(), indexHandler, this.typeSize, this.indexType);
            this.childNodes.add(node);
            node.insert(key, rid);
        } else {
            TreeNode currentNode = this.childNodes.get(index);
            currentNode.insert(key, rid);
            if (childKeys.get(index).compareTo(key) > 0)
                childKeys.set(index, key);
            if (currentNode.pageSize() > PAGE_SIZE) {
                JSONObject object = currentNode.split();
                assert (object != null);
                List<IndexContent> newKeys;
                newKeys = (ArrayList<IndexContent>) object.get("newKeys");
                IndexContent maxKey = (IndexContent) object.get("maxKey");
                childKeys.add(index + 1, maxKey);
                int newPageId = indexHandler.createNewPage();
                TreeNode node;
                if (currentNode.nodeType == 0) {
                    List<TreeNode> newVal;
                    newVal = (ArrayList<TreeNode>) object.get("newVal");
                    node = new InterNode(newPageId, this.pageId, newVal, newKeys, indexHandler, this.typeSize, this.indexType);
                } else {
                    LeafNode leafNode = (LeafNode) currentNode;
                    List<RID> newVal;
                    newVal = (ArrayList<RID>) object.get("newVal");
                    leafNode.setNextId(newPageId);
                    node = new LeafNode(newPageId, leafNode.parentId, leafNode.pageId, leafNode.getNextId(), newVal, newKeys, indexHandler, this.typeSize, this.indexType);
                }
                childNodes.add(index + 1, node);
            }
        }
    }

    @Override
    public IndexContent remove(IndexContent key, RID val) {
        int head = lowerBound(key);
        int tail = upperBound(key);
        if (tail < childKeys.size())
            tail += 1;
        int shift = 0;
        IndexContent ret = null;
        if (head == -1 || tail == -1)
            return null;
        for (int i = head; i < tail; i++) {
            int j = i - shift;
            TreeNode node = childNodes.get(j);
            IndexContent changeKey = node.remove(key, val);
            if (changeKey != null) {
                childKeys.set(j, changeKey);
            }
            if (node.childKeys.size() == 0) {
                childKeys.remove(j);
                childNodes.remove(j);
                shift += 1;

            }
            if (j == 0 && childKeys.size() > 0) {
                ret = childKeys.get(0);
            }
        }
        return ret;
    }

    @Override
    public int pageSize() {
        return 24 + childKeys.size() * (this.typeSize + 8) + 32;
    }

    @Override
    public byte[] turnToBytes() {
        Long[] longs = new Long[3];
        byte[] bytes = new byte[PAGE_SIZE];
        Arrays.fill(longs, 0L);
        longs[0] = (long) this.nodeType;
        longs[1] = this.parentId;
        longs[2] = (long) this.childKeys.size();
        byte[] headBytes = processLongsToBytes(longs);
        System.arraycopy(headBytes, 0, bytes, 0, headBytes.length);
        int head = headBytes.length;
        for (int i = 0; i < this.childKeys.size(); i++) {
            byte[] dataBytes;
            dataBytes = IndexUtility.turnToBytes(this.typeSize, this.childKeys.get(i), this.indexType);
            System.arraycopy(dataBytes, 0, bytes, head, dataBytes.length);
            head += this.typeSize;
            byte[] pageIdByte = ByteLongConverter.long2Bytes(this.childNodes.get(i).pageId);
            System.arraycopy(pageIdByte, 0, bytes, head, 8);
            head += 8;
        }
        byte[] test = new byte[8];
        System.arraycopy(bytes, 16, test, 0, 8);
        return bytes;
    }

    @Override
    public List<RID> search(IndexContent key) {
        int low = lowerBound(key);
        int high = upperBound(key);
        List<RID> list = new ArrayList<>();
        if (low == childNodes.size())
            low--;
        if (high == childNodes.size())
            high--;
        if (high == -1)
            return null;
        if (low != 0)
            low--;
        for (int i = low; i <= high; i++) {
            List<RID> list1 = childNodes.get(i).search(key);
            if (list1 != null)
                list.addAll(list1);
        }
        return list;
    }

    public int getChildKeysSize() {
        return childKeys.size();
    }

    @Override
    public ArrayList<RID> range(IndexContent low, IndexContent high) {
        ArrayList<RID> res = new ArrayList<>();
        int lower = lowerBound(low);
        int upper = upperBound(high);
        if (lower == -1 || upper == -1)
            return null;
        if (upper != childKeys.size())
            upper++;
        if (lower != 0)
            lower--;
        for (int i = lower; i < upper; i++) {
            ArrayList<RID> subRes = childNodes.get(i).range(low, high);
            if (subRes != null)
                res.addAll(subRes);
        }
        return res;
    }
}
