package database.rzotgorz.indexsystem;

import database.rzotgorz.filesystem.FileManager;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.ResourceBundle;


@Data
@Slf4j
public class IndexHandler {

    private static final ResourceBundle bundle = ResourceBundle.getBundle("configurations");
    private static final int PAGE_SIZE = Integer.parseInt(bundle.getString("PAGE_SIZE"));


    private FileManager fileManager;
    private boolean modified;
    private int fileId;
    private int typeFileId;
    private String dbName;

    public IndexHandler(FileManager fm, String dbName) {
        this.dbName = dbName;
        this.fileManager = fm;
        this.modified = true;
        String path = dbName + File.separator + dbName + ".index";
        String typePath = "data" + File.separator + dbName + File.separator + dbName + ".type";
        File file = new File(path);
        if (!file.exists())
            this.fileId = fm.createFile(path);
        else
            this.fileId = fm.openFile(path);
        File typeFile = new File(typePath);
        if (!typeFile.exists()) {
            try {
                log.info(typeFile.toString());
                typeFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
//            this.typeFileId = fm.createFile(typePath);
//        else
//            this.typeFileId = fm.openFile(typePath);
    }

    public byte[] getPage(int pageId) {
        return fileManager.getPageData(this.fileId, pageId);
    }

    public void putPage(int pageId, byte[] data) {
        this.modified = true;
        fileManager.putPage(fileId, pageId, data);
    }

    public int createNewPage() {
        byte[] data = new byte[PAGE_SIZE];
        byte b = 0;
        Arrays.fill(data, b);
        int pageId = fileManager.createPage(this.fileId, data);
        return pageId;
    }

    public void closeFile() {
        this.fileManager.closeFile(this.fileId);
    }
}
