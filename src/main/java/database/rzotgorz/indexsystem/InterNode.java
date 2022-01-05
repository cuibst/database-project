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
public class InterNode extends TreeNode {
    public List<TreeNode> childNodes;

    private static final ResourceBundle bundle = ResourceBundle.getBundle("configurations");
    private static final int PAGE_SIZE = Integer.parseInt(bundle.getString("PAGE_SIZE"));

    public InterNode(long pageId, long parentId, List<TreeNode> childNodes, List<IndexContent> childKeys, IndexHandler indexHandler) {
        super(pageId, parentId, childKeys, indexHandler);
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
        if (this.childNodes == null)
            log.info("old root childNodes is null");
        if (newNodes == null)
            log.info("new root childNodes is null");
        return object;
    }

    @Override
    public void insert(IndexContent key, RID rid) {
//        log.info("key: {}", key);
        int index = this.lowerBound(key);
        if (index == -1) {
            this.childKeys.add(key);
            int nodePageId = indexHandler.createNewPage();
            TreeNode node = new LeafNode(nodePageId, this.pageId, 0, 0, new ArrayList<RID>(), new ArrayList<IndexContent>(), indexHandler);
            this.childNodes.add(node);
            node.insert(key, rid);
        } else {
//            log.info("index:{}", index);
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
                    node = new InterNode(newPageId, this.pageId, newVal, newKeys, indexHandler);
                } else {
                    LeafNode leafNode = (LeafNode) currentNode;
                    List<RID> newVal;
                    newVal = (ArrayList<RID>) object.get("newVal");
                    leafNode.setNextId(newPageId);
                    node = new LeafNode(newPageId, leafNode.parentId, leafNode.pageId, leafNode.getNextId(), newVal, newKeys, indexHandler);
                }
//                log.info("nodePageId:{}", newPageId);
                childNodes.add(index + 1, node);
//                log.info("pageId:{}", childNodes.get(index + 1).pageId);
//                for (int i = 0; i < childNodes.size(); i++) {
//                    System.out.println(childNodes.get(i).childKeys);
//                }
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
//            log.info("interNode i :{},j:{}", i, j);
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
        return 24 + childKeys.size() * 16 + 32;
    }

    @Override
    public byte[] turnToBytes() {
        Long[] longs = new Long[PAGE_SIZE / 8];
        Arrays.fill(longs, 0L);
        longs[0] = (long) this.nodeType;
        longs[1] = this.parentId;
        longs[2] = (long) this.childKeys.size();
        for (int i = 0; i < this.childKeys.size(); i++) {
            longs[i * 2 + 3] = this.childKeys.get(i);
            longs[i * 2 + 4] = this.childNodes.get(i).pageId;
//            log.info("pageId: {}", this.childKeys.get(i));
        }
        return processLongsToBytes(longs);
    }

    @Override
    public RID search(IndexContent key) {
        int index = lowerBound(key);
        if (index == childNodes.size())
            index--;
        return childNodes.get(index).search(key);
    }

    public int getChildKeysSize() {
//        System.out.println("in here");
//        System.out.println(childKeys.size());
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
