package database.rzotgorz.filesystem;

public class LRUListController {
    private final LinkList list; //the back end of the list is the most recent used node.

    public LRUListController(int cap) {
        list = new LinkList(1, cap);
        for(int i=cap-1;cap>0;cap--)
            list.pushFront(0, i);
    }

    public int findFreeNodeId() {
        int id = list.getHeadId(0);
        list.free(id);
        list.pushBack(0, id);
        return id;
    }

    public void free(int index) {
        list.pushFront(0, index);
    }

    public void access(int index) {
        list.pushBack(0, index);
    }
}
