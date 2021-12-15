package database.rzotgorz;

import database.rzotgorz.managesystem.DatabaseController;
import database.rzotgorz.managesystem.SQLTreeVisitor;
import database.rzotgorz.managesystem.results.MessageResult;
import database.rzotgorz.managesystem.results.ResultItem;
import database.rzotgorz.metaSystem.MetaManager;
import database.rzotgorz.parser.SQLLexer;
import database.rzotgorz.parser.SQLParser;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.Serializable;
import java.nio.file.NotDirectoryException;
import java.util.ArrayList;

@Slf4j
public class Main {

    public static void run(String expr) {
        CharStream stream = CharStreams.fromString(expr);
        SQLLexer lexer = new SQLLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SQLParser parser = new SQLParser(tokens);
        parser.program();
    }

    public static class A implements Serializable {
        public String name;
        public ArrayList<String> list;

        public A(String name, ArrayList<String> list) {
            this.name = name;
            this.list = list;
        }
    }

    public static void main(String[] args) {

        MetaManager metaManager = new MetaManager();


        SQLTreeVisitor visitor = new SQLTreeVisitor();
        DatabaseController controller;
        try {
            controller = new DatabaseController(visitor);
        } catch (NotDirectoryException e) {
            log.error(e.getMessage());
            return;
        }
//
//        Object e = controller.execute("CREATE DATABASE test;");
//        printResults(e);
//        e = controller.execute("CREATE DATABASE hello_world;");
//        printResults(e);
//        e = controller.execute("SHOW DATABASES;");
//        printResults(e);
        Object e = controller.execute("SHOW TABLES;");
        printResults(e);
        e = controller.execute("USE test;");
        printResults(e);
        e = controller.execute("SHOW TABLES;");
        printResults(e);
        e = controller.execute("CREATE TABLE scholars ( studentid INT, awardname VARCHAR(32) NOT NULL, awardtime INT);");
        printResults(e);
        e = controller.execute("SHOW TABLES;");
        printResults(e);


//        ArrayList<String> list = new ArrayList<>();
//        list.add("1");
//        list.add("2");
//        list.add("3");
//        try {
//            FileOutputStream ff = new FileOutputStream("h.txt");
//            ObjectOutputStream ss = new ObjectOutputStream(ff);
//            A a = new A("llh", list);
//            ss.writeObject(a);
//            ss.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            ObjectInputStream ois = new ObjectInputStream(
//                    new BufferedInputStream(new FileInputStream(new File("h.txt"))));
//            A a = (A) ois.readObject();
//            ois.close();
//            System.out.println(a.name);
//            System.out.println(a.list);
//
//        } catch (IOException | ClassNotFoundException e) {
//            e.printStackTrace();
//        }

        //        FileManager fileManager = new FileManager();
//        IndexManager indexManager = new IndexManager(fileManager, "llh");
//        FileIndex fileIndex = indexManager.createIndex("llh", "llhTest");
//        IndexHandler indexHandler = indexManager.getHandler("llh");
//
//        for (int i = 0; i < 3000; i++)
//            for (int j = 0; j < 30; j++) {
////                log.info("i: {} , j: {}", i, j);
//                fileIndex.insert(i * 30 + j, new RID(i, j));
//            }
////        indexManager.shutdown();
////        fileIndex.remove(340, new RID(11, 10));
//        int rootId = fileIndex.getRootId();
//        indexManager.closeHandler("llh");
//        indexHandler = indexManager.getHandler("llh");
////        indexManager.closeIndex("llhTest", rootId);
////        fileIndex = null;
//        fileIndex = indexManager.openedIndex("llh", "llhTest", rootId);
//
////        for (int i = 0; i < 30; i++)
////            for (int j = 0; j < 30; j++)
////                fileIndex.remove(i * 30 + j, new RID(i, j));
////                fileIndex.remove(0, new RID(0, 0));
////        for (int i = 0; i < 30; i++)
////            for (int j = 40; j < 60; j++)
////                fileIndex.insert(i * 30 + j, new RID(i, j));
//
//
//        System.out.println(fileIndex.getRootNode().getChildKeys());
////        fileIndex.range(20, 500);
//        for (int i = 0; i < fileIndex.getRootNode().getChildKeys().size(); i++) {
//            if (((InterNode) fileIndex.getRootNode()).childNodes.get(i).getClass() == InterNode.class) {
//                System.out.println((((InterNode) (fileIndex.getRootNode())).childNodes.get(i).getChildKeys()));
//            }
//        }
//        for (int i = 0; i < fileIndex.getRootNode().getChildKeys().size(); i++) {
//            if (((InterNode) fileIndex.getRootNode()).childNodes.get(i).getClass() == InterNode.class) {
//                System.out.println("in here");
//                System.out.println(((InterNode) (((InterNode) (fileIndex.getRootNode())).childNodes.get(i))).getChildKeysSize());
//            }
//        }

//        System.out.println("finished");
//        indexManager.shutdown();
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

    private static void printResults(Object e) {
        if (e != null) {
            if (e.getClass() == MessageResult.class) {
                System.out.println(e);
                System.out.println("Result received in " + ((ResultItem) e).cost + "ms.");
                System.out.println("=".repeat(30));
            } else {
                boolean flag = false;
                assert e.getClass() == ArrayList.class;
                for (ResultItem resultItem : (ArrayList<ResultItem>) e) {
                    if (!flag)
                        flag = true;
                    else
                        System.out.println();
                    System.out.println(resultItem);
                    System.out.println("Result received in " + resultItem.cost + "ms.");
                }
                System.out.println("=".repeat(30));
            }
        }
    }
}
