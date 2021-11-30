package database.rzotgorz;

import database.rzotgorz.filesystem.FileManager;
import database.rzotgorz.indexsystem.FileIndex;
import database.rzotgorz.indexsystem.IndexHandler;
import database.rzotgorz.indexsystem.IndexManager;
import database.rzotgorz.indexsystem.InterNode;
import database.rzotgorz.parser.SQLLexer;
import database.rzotgorz.parser.SQLParser;
import database.rzotgorz.recordsystem.RID;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

@Slf4j
public class Main {

    public static void run(String expr) {
        CharStream stream = CharStreams.fromString(expr);
        SQLLexer lexer = new SQLLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SQLParser parser = new SQLParser(tokens);
        parser.program();
    }

    public static class A {
        public A() {
        }

        public void print() {
            System.out.println("a");
        }
    }

    public static class B extends A {
        public B() {
            super();
        }

        public void print() {
            System.out.println("b");
        }
    }

    public static void main(String[] args) {
        FileManager fileManager = new FileManager();
        IndexManager indexManager = new IndexManager(fileManager, "llh");
        FileIndex fileIndex = indexManager.createIndex("llh", "llhTest");
        IndexHandler indexHandler = indexManager.getHandler("llh");

        for (int i = 0; i < 30; i++)
            for (int j = 0; j < 30; j++)
                fileIndex.insert(i * 30 + j, new RID(i, j));
//        indexManager.shutdown();
//        fileIndex.remove(340, new RID(11, 10));
        int rootId = fileIndex.getRootId();
        indexManager.closeHandler("llh");
        indexHandler = indexManager.getHandler("llh");
//        indexManager.closeIndex("llhTest", rootId);
//        fileIndex = null;
        fileIndex = indexManager.openedIndex("llh", "llhTest", rootId);

//        for (int i = 0; i < 30; i++)
//            for (int j = 0; j < 30; j++)
//                fileIndex.remove(i * 30 + j, new RID(i, j));
//                fileIndex.remove(0, new RID(0, 0));
//        for (int i = 0; i < 30; i++)
//            for (int j = 40; j < 60; j++)
//                fileIndex.insert(i * 30 + j, new RID(i, j));


        System.out.println(fileIndex.getRootNode().getChildKeys());
        fileIndex.range(20, 500);
        for (int i = 0; i < fileIndex.getRootNode().getChildKeys().size(); i++) {
            System.out.println((((InterNode) (fileIndex.getRootNode())).childNodes.get(i).getChildKeys()));
        }
        indexManager.shutdown();
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
//        for(int pageId = 0; pageId < 10; pageId ++) {
//            byte[] data = manager.getPageData(fid1, pageId);
//            log.info("From fid:{} pid:{} read: {}, {}", fid1, pageId, data[0], data[1]);
//            manager.access(fid1, pageId);
//            data = manager.getPageData(fid2, pageId);
//            log.info("From fid:{} pid:{} read: {}, {}", fid2, pageId, data[0], data[1]);
//            manager.access(fid2, pageId);
//        }
//

//        FileHandler handler=rm.openFile("llh.dat");
//        byte[] bytes=ByteIntegerConverter.intToBytes(22);
//        for(int i=0;i<1640;i++)
//            handler.insertRecord(bytes);
//        rm.closeFile("llh.dat");

    }
}
