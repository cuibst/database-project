package database.rzotgorz.filesystem;

import database.rzotgorz.exceptions.AlreadyOpenedException;
import database.rzotgorz.exceptions.FailToOpenException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.*;

@Slf4j
@Data
public class FileManager {

    public static class Page {
        public int pageId = -1;
        public int fileId = -1;

        public Page(int pageId, int fileId) {
            this.pageId = pageId;
            this.fileId = fileId;
        }

        public Page() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Page page = (Page) o;
            return pageId == page.pageId && fileId == page.fileId;
        }

        @Override
        public int hashCode() {
            return Objects.hash(pageId, fileId);
        }

        public boolean isEmpty() {
            return pageId == -1 && fileId == -1;
        }
    }

    private String[] idToFilename;

    private byte[][] pageBuffer;

    Queue<Integer> remainingId;
    Map<String, Integer> filenameToId;
    Set<Page>[] cachedPages;

    private boolean[] dirty;
    private LRUListController controller;
    private Page[] indexToPage;
    private Map<Page, Integer> pageToIndex;
    int last = -1;

    private static final ResourceBundle bundle = ResourceBundle.getBundle("configurations");
    private static final int MAX_FILE_NUM = Integer.parseInt(bundle.getString("MAX_FILE_NUM"));
    private static final int CACHE_CAPACITY = Integer.parseInt(bundle.getString("CACHE_CAPACITY"));
    private static final int PAGE_SIZE = Integer.parseInt(bundle.getString("PAGE_SIZE"));
    private static final int PAGE_SIZE_LOG_2 = Integer.parseInt(bundle.getString("PAGE_SIZE_LOG_2"));

    public FileManager() {
        idToFilename = new String[MAX_FILE_NUM];
        remainingId = new LinkedList<>();
        for(int i = 0; i< MAX_FILE_NUM; i++)
            remainingId.add(i);
        filenameToId = new HashMap<>();
        cachedPages = new Set[MAX_FILE_NUM];
        controller = new LRUListController(CACHE_CAPACITY);
        dirty = new boolean[CACHE_CAPACITY];
        indexToPage = new Page[CACHE_CAPACITY];
        for(int i=0;i<CACHE_CAPACITY;i++)
            indexToPage[i] = new Page();
        pageToIndex = new HashMap<>();
        pageBuffer = new byte[CACHE_CAPACITY][PAGE_SIZE];
    }

    private void access(int index) {
        if(index == last)
            return;
        controller.access(index);
        last = index;
    }

    public void access(int fileId, int pageId) {
        Page page = new Page(pageId, fileId);
        if(pageToIndex.containsKey(page))
            access(pageToIndex.get(page));
    }

    public void markAsDirty(int index) {
        dirty[index] = true;
        access(index);
    }

    private void free(int index) {
        dirty[index] = false;
        controller.free(index);
        Page page = indexToPage[index];
        cachedPages[page.fileId].remove(page);
        pageToIndex.remove(page);
        indexToPage[index] = new Page();
    }

    private void writeBack(int index) {
        if(dirty[index])
            writePage(indexToPage[index].fileId, indexToPage[index].pageId, pageBuffer[index]);
        free(index);
    }

    public int openFile(String filename) {
        if(filenameToId.containsKey(filename))
            throw new AlreadyOpenedException(filename);
        try(RandomAccessFile file = new RandomAccessFile(filename, "rw")) {
            int id = remainingId.poll();
            cachedPages[id] = new HashSet<>();
            filenameToId.put(filename, id);
            idToFilename[id] = filename;
            return id;
        } catch (IOException e) {
            e.printStackTrace();
            throw new FailToOpenException(filename);
        }
    }

    public void closeFile(int fileId) {
        Set<Page> pages = cachedPages[fileId];
        cachedPages[fileId] = new HashSet<>();
        for(Page page : pages) {
            int index = pageToIndex.get(page);
            pageToIndex.remove(page);
            indexToPage[index] = new Page();
            controller.free(index);
            if(dirty[index]) {
                writePage(page.fileId, page.pageId, pageBuffer[index]);
                dirty[index] = false;
            }
        }
        String filename = idToFilename[fileId];
        idToFilename[fileId] = "";
        filenameToId.remove(filename);
    }

    public byte[] readPage(int fileId, int pageId) {
        long offset = ((long)pageId) << PAGE_SIZE_LOG_2;
        String filename = idToFilename[fileId];
        byte[] result = new byte[PAGE_SIZE];
        try(RandomAccessFile file = new RandomAccessFile(filename, "rw")) {
            file.seek(offset);
            file.read(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void writePage(int fileId, int pageId, byte[] buf) {
        try (RandomAccessFile file = new RandomAccessFile(idToFilename[fileId], "rw")) {
            long offset = ((long)pageId) << PAGE_SIZE_LOG_2;
            file.seek(offset);
            file.write(buf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int createPage(int fileId, byte[] data) {
        long length = -1;
        try (RandomAccessFile file = new RandomAccessFile(idToFilename[fileId], "rw")) {
            length = file.length();
            file.seek(length);
            file.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (int)(length >> PAGE_SIZE_LOG_2);
    }

   private byte[] getPage(int fileId, int pageId) {
        Page page = new Page(pageId, fileId);
        if(pageToIndex.containsKey(page))
            return pageBuffer[pageToIndex.get(page)];

        int index = controller.findFreeNodeId();
        Page lastPage = indexToPage[index];
        if(!lastPage.isEmpty())
            writeBack(index);
        pageToIndex.put(page, index);
        cachedPages[fileId].add(page);
        indexToPage[index] = page;

        byte[] data = readPage(fileId, pageId);
        pageBuffer[index] = data;
        return data;
    }

    public void putPage(int fileId, int pageId, byte[] data) {
        Page page = new Page(pageId, fileId);
        int index;
        if(!pageToIndex.containsKey(page)) {
            getPage(fileId, pageId);
        }
        index = pageToIndex.get(page);
        pageBuffer[index] = data;
        dirty[index] = true;
        controller.access(index);
    }

    public byte[] getPageRef(int fileId, int pageId) {
        byte[] result = getPage(fileId, pageId);
        Page page = new Page(pageId, fileId);
        markAsDirty(pageToIndex.get(page));
        return result;
    }

    public byte[] getPageData(int fileId, int pageId) {
        Page page = new Page(pageId, fileId);
        return getPage(fileId, pageId).clone();
    }

    public void releaseCache() {
        for(int i = 0; i< CACHE_CAPACITY; i++) {
            if(dirty[i]) {
                writeBack(i);
                Arrays.fill(pageBuffer[i], (byte) 0);
            }
        }

        Arrays.fill(dirty, false);
        Arrays.fill(indexToPage, new Page());
        pageToIndex.clear();
        last = -1;
    }

    public void shutdown() {
        releaseCache();
    }

    public void printPageData() {
        for(Map.Entry<Page, Integer> entry : pageToIndex.entrySet()) {
            log.info("Entry: fid:{} pid:{} index:{}", entry.getKey().fileId, entry.getKey().pageId, entry.getValue());
        }
    }
}
