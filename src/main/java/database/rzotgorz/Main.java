package database.rzotgorz;

import database.rzotgorz.filesystem.FileManager;
import database.rzotgorz.recordsystem.ByteIntegerConverter;
import database.rzotgorz.recordsystem.FileHandler;
import database.rzotgorz.recordsystem.RID;
import database.rzotgorz.recordsystem.RecordManager;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import database.rzotgorz.parser.SQLLexer;
import database.rzotgorz.parser.SQLParser;

import java.io.IOException;
import java.io.RandomAccessFile;

@Slf4j
public class Main {

    public static void run(String expr) {
        CharStream stream = CharStreams.fromString(expr);
        SQLLexer lexer = new SQLLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SQLParser parser = new SQLParser(tokens);
        parser.program();
    }

    public static void main(String[] args) {
//        String[] testStrings = {
//                "CREATE DATABASE student;",
//                "ABC 123"
//        };
//
//        for(String s : testStrings) {
//            log.info("Input: {}", s);
//            run(s);
//        }
//
//        FileManager manager = new FileManager();
//        int fid1 = manager.openFile("test1.txt");
//        int fid2 = manager.openFile("test2.txt");
//        for(int pageId = 0; pageId < 256; pageId ++) {
//            byte[] data = manager.getPageRef(fid1, pageId); //"mark as dirty" has been done in getPageRef
//            data[0] = (byte)pageId;
//            data[1] = (byte)fid1;
//
//            data = manager.getPageRef(fid2, pageId);
//            data[0] = (byte)pageId;
//            data[1] = (byte)fid2;
//        }
//
//        manager.printPageData();
//
//        for(int pageId = 0; pageId < 256; pageId ++) {
//            byte[] data = manager.getPageData(fid1, pageId);
//            log.info("From fid:{} pid:{} read: {}, {}", fid1, pageId, data[0], data[1]);
//            manager.access(fid1, pageId);
//            data = manager.getPageData(fid2, pageId);
//            log.info("From fid:{} pid:{} read: {}, {}", fid2, pageId, data[0], data[1]);
//            manager.access(fid2, pageId);
//        }
//
        RecordManager rm=new RecordManager();
        rm.createFile("llh.dat",4);
        FileHandler handler=rm.openFile("llh.dat");
//        int pageId=handler.createPage();
        byte[] bytes=ByteIntegerConverter.intToBytes(22);
//        for(int i=0;i<1640;i++)
        handler.insertRecord(bytes);
//        handler.deletePage(new RID(1,0));
        rm.closeFile("llh.dat");
    }
}
