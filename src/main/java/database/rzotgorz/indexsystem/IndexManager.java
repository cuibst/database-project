package database.rzotgorz.indexsystem;

import com.alibaba.fastjson.JSONObject;
import database.rzotgorz.filesystem.FileManager;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
public class IndexManager {
    private FileManager fileManager;
    private String dbName;
    private Map<String, IndexHandler> openedIndexHandlers;
    private Map<JSONObject, FileIndex> openedFileIndex;

    public IndexManager(FileManager fm, String dbName) {
        this.openedIndexHandlers = new HashMap<>();
        this.openedFileIndex = new HashMap<>();
        this.fileManager = fm;
        this.dbName = dbName;
    }

    public IndexHandler getHandler(String dbName) {
        if (openedIndexHandlers.containsKey(dbName)) {
            return openedIndexHandlers.get(dbName);
        } else {
//            log.info("in getHandler");
            IndexHandler handler = new IndexHandler(fileManager, dbName);
            openedIndexHandlers.put(dbName, handler);
            return handler;
        }
    }

    public void closeHandler(String dbName) {
        if (!openedIndexHandlers.containsKey(dbName))
            return;
        IndexHandler handler = openedIndexHandlers.get(dbName);
        this.closeAllRelevantIndex(dbName);
        openedIndexHandlers.remove(dbName);
        handler.closeFile();
    }

    public void closeAllRelevantIndex(String dbName) {
        IndexHandler handler = getHandler(dbName);
        List<JSONObject> list = new ArrayList<>(openedFileIndex.keySet());
        for (int i = 0; i < list.size(); i++) {
            if (openedFileIndex.get(list.get(i)).getIndexHandler().equals(handler)) {
                closeIndex(list.get(i).get("tableName").toString(), Integer.parseInt(list.get(i).get("rootId").toString()));
            }
        }
    }

    public FileIndex createIndex(String dbName, String tableName) {
        IndexHandler handler = getHandler(dbName);
        FileIndex fileIndex = new FileIndex(handler.createNewPage(), handler);
        fileIndex.dump();
        JSONObject object = new JSONObject();
//        log.info("rootId:{}", fileIndex.getRootId());
        object.put("rootId", fileIndex.getRootId());
        object.put("tableName", tableName);
        openedFileIndex.put(object, fileIndex);
        return fileIndex;
    }

    public FileIndex openedIndex(String dbName, String tableName, int rootId) {
        JSONObject object = new JSONObject();
        object.put("rootId", rootId);
        object.put("tableName", tableName);
        if (openedFileIndex.containsKey(object))
            return openedFileIndex.get(object);
        IndexHandler handler = getHandler(dbName);
        FileIndex fileIndex = new FileIndex(rootId, handler);
        fileIndex.load();
        openedFileIndex.put(object, fileIndex);
        return fileIndex;
    }

    public void closeIndex(String tableName, int rootId) {
        JSONObject object = new JSONObject();
        object.put("rootId", rootId);
        object.put("tableName", tableName);
        if (!openedFileIndex.containsKey(object))
            return;
        FileIndex fileIndex = openedFileIndex.get(object);
        if (fileIndex.isModified())
            fileIndex.dump();
        openedFileIndex.remove(object);
        return;
    }

    public void shutdown() {
        List<String> list = new ArrayList<>(openedIndexHandlers.keySet());
        for (int i = 0; i < list.size(); i++) {
            this.closeHandler(list.get(i));
        }
    }
}
