package database.rzotgorz.managesystem;

import database.rzotgorz.indexsystem.IndexManager;
import database.rzotgorz.managesystem.results.DatabaseChangeResult;
import database.rzotgorz.managesystem.results.ListResult;
import database.rzotgorz.managesystem.results.MessageResult;
import database.rzotgorz.managesystem.results.ResultItem;
import database.rzotgorz.metaSystem.MetaHandler;
import database.rzotgorz.metaSystem.MetaManager;
import database.rzotgorz.metaSystem.TableInfo;
import database.rzotgorz.parser.SQLLexer;
import database.rzotgorz.parser.SQLParser;
import database.rzotgorz.recordsystem.RecordManager;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NotDirectoryException;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class DatabaseController {
    private final RecordManager recordManager;
    private final IndexManager indexManager;
    private final MetaManager metaManager;
    private final String rootPath;
    private final Set<String> databases = new HashSet<>();
    private String currentUsingDatabase = null;
    private SQLTreeVisitor visitor;

    public Set<String> getDatabases() {
        return databases;
    }

    public DatabaseController(SQLTreeVisitor visitor) throws NotDirectoryException {
        recordManager = new RecordManager();
        indexManager = new IndexManager(recordManager.getFileManager());
        metaManager = new MetaManager();
        rootPath = "data";
        this.visitor = visitor;
        visitor.controller = this;

        File rootDirectory = new File(rootPath);
        if (!rootDirectory.isDirectory())
            throw new NotDirectoryException("Path 'data' has already been used by a file");
        File[] files = rootDirectory.listFiles();
        if (files != null && files.length > 0 && files[0] != null)
            for (File file : files)
                databases.add(file.getName());
    }

    private String getDatabasePath(String name) {
        return rootPath + File.separator + name;
    }

    private String getTablePath(String tableName) {
        return currentUsingDatabase + File.separator + tableName;
    }

    public void createDatabase(String name) throws FileAlreadyExistsException {
        if (databases.contains(name)) {
            throw new FileAlreadyExistsException("Database " + name + " already exists!");
        }
        String databasePath = getDatabasePath(name);
        File file = new File(databasePath);
        assert !file.exists();
        file.mkdir();
        databases.add(name);
    }

    public DatabaseChangeResult useDatabase(String name) throws FileNotFoundException {
        if (!databases.contains(name)) {
            throw new FileNotFoundException("Database " + name + " doesn't exist");
        }
        currentUsingDatabase = name;
        return new DatabaseChangeResult(name);
    }

    public DatabaseChangeResult dropDatabase(String name) throws FileNotFoundException {
        if (!databases.contains(name))
            throw new FileNotFoundException("Database " + name + " doesn't exist!");
        File databaseDirectory = new File(getDatabasePath(name));
        assert databaseDirectory.exists() && databaseDirectory.isDirectory();
        indexManager.closeHandler(name);
        metaManager.closeMeta(name);
        File[] files = databaseDirectory.listFiles();
        if (files != null && files.length > 0 && files[0] != null)
            for (File file : files) {
                recordManager.closeFile(file.getName());
                file.delete();
            }
        databaseDirectory.delete();
        databases.remove(name);
        if (currentUsingDatabase.equals(name))
            return new DatabaseChangeResult("null");
        return null;
    }

    public ResultItem showTables() {
        if (currentUsingDatabase == null)
            return new MessageResult("No database is selected.", true);
        File databaseDirectory = new File(getDatabasePath(currentUsingDatabase));
        assert databaseDirectory.exists() && databaseDirectory.isDirectory();
        Set<String> tables = new HashSet<>();
        File[] files = databaseDirectory.listFiles();
        if (files != null && files.length > 0 && files[0] != null)
            for (File file : files) {
                tables.add(file.getName());
            }
        return new ListResult(tables, "TABLES");
    }

    public ResultItem createTable(TableInfo bundle) {
        if (currentUsingDatabase == null)
            return new MessageResult("No database is being used!", true);
        MetaHandler handler = metaManager.openMeta(currentUsingDatabase);
        handler.addTable(bundle);
        int recordLength = bundle.getTotalSize();
        recordManager.createFile(getTablePath(bundle.getName()), recordLength);
        return new MessageResult("ok");
    }

    public void addForeignKey(String tableName, SQLTreeVisitor.ForeignKey foreignKey, String keyName) throws IOException {
        MetaHandler handler = metaManager.openMeta(currentUsingDatabase);
        handler.addForeign(foreignKey.targetTable, foreignKey.name, keyName);
        String indexName = keyName == null ? (foreignKey.targetTable + "." + foreignKey.name) : keyName;
        if (!handler.existsIndex(indexName))
            handler.createIndex(indexName, foreignKey.targetTable, foreignKey.name);
    }

    //FIXME: primary key can be multi-field
//    public void setPrimaryKey(String tableName, List<String> primaryKeyFields) {
//        MetaHandler handler = metaManager.openMeta(tableName);
//        handler.setPrimary();
//    }

    public static class ErrorListener implements ANTLRErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object o, int i, int i1, String s, RecognitionException e) {
            throw new ParseCancellationException("line " + i + ":" + i1 + " " + s);
        }

        @Override
        public void reportAmbiguity(Parser parser, DFA dfa, int i, int i1, boolean b, BitSet bitSet, ATNConfigSet atnConfigSet) {
            throw new ParseCancellationException("line " + i + ":" + i1 + " ambiguity detected.");
        }

        @Override
        public void reportAttemptingFullContext(Parser parser, DFA dfa, int i, int i1, BitSet bitSet, ATNConfigSet atnConfigSet) {
            throw new ParseCancellationException("line " + i + ":" + i1 + " attempting full context.");
        }

        @Override
        public void reportContextSensitivity(Parser parser, DFA dfa, int i, int i1, int i2, ATNConfigSet atnConfigSet) {
            throw new ParseCancellationException("line " + i + ":" + i1 + " report sensitivity problems.");
        }
    }

    public Object execute(String sql) {
        visitor.getTimeDelta();
        CharStream stream = CharStreams.fromString(sql);
        SQLLexer lexer = new SQLLexer(stream);
        lexer.removeErrorListeners();
        lexer.addErrorListener(new ErrorListener());

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SQLParser parser = new SQLParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new ErrorListener());

        SQLParser.ProgramContext context;

        try {
            context = parser.program();
        } catch (ParseCancellationException e) {
            MessageResult result = new MessageResult(e.getMessage(), true);
            result.cost = visitor.getTimeDelta();
            return result;
        }

        try {
            return visitor.visit(context);
        } catch (ParseCancellationException e) {
            MessageResult result = new MessageResult(e.getMessage(), true);
            result.cost = visitor.getTimeDelta();
            return result;
        }
    }

}
