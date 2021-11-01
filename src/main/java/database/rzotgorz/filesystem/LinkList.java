package database.rzotgorz.filesystem;

public class LinkList {

    private class Node {
        public int prev;
        public int next;
    }

    private final int capacity;
    private final Node[] nodes; //last 0 ~ LIST_NUM - 1 nodes are list markers(head and tail), before them are nodes

    public LinkList(int listNum, int cap) {
        capacity = cap;
        nodes = new Node[listNum + cap];
        for(int i=0;i<listNum + cap;i++) {
            nodes[i] = new Node();
            nodes[i].prev = nodes[i].next = i;
        }
    }

    private void link(int prevId, int nextId) {
        nodes[prevId].next = nextId;
        nodes[nextId].prev = prevId;
    }

    public void free(int index) {
        if(nodes[index].prev == index)
            return;
        link(nodes[index].prev, nodes[index].next);
        nodes[index].prev = nodes[index].next = index;
    }

    public void pushBack(int listId, int index) {
        free(index);
        int markerId = listId + capacity;
        int tailId = nodes[markerId].prev;
        link(tailId, index);
        link(index, markerId);
    }

    public void pushFront(int listId, int index) {
        free(index);
        int markerId = listId + capacity;
        int headId = nodes[markerId].next;
        link(markerId, index);
        link(index, headId);
    }

    public int getHeadId(int listId) {
        return nodes[listId + capacity].next;
    }

    public int getNextId(int index) {
        return nodes[index].next;
    }

    public boolean isMarker(int index) {
        return (index >= capacity);
    }

    public boolean isFree(int index) {
        return (nodes[index].next == index);
    }
}
