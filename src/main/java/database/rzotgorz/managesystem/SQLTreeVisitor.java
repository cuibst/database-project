package database.rzotgorz.managesystem;

import database.rzotgorz.Main;
import database.rzotgorz.exceptions.ParseError;
import database.rzotgorz.managesystem.results.ListResult;
import database.rzotgorz.managesystem.results.MessageResult;
import database.rzotgorz.managesystem.results.ResultItem;
import database.rzotgorz.metaSystem.ColumnInfo;
import database.rzotgorz.metaSystem.TableInfo;
import database.rzotgorz.parser.SQLBaseVisitor;
import database.rzotgorz.parser.SQLParser;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.text.ParseException;
import java.util.*;

@Slf4j
public class SQLTreeVisitor extends SQLBaseVisitor<Object> {

    public DatabaseController controller;
    private long startTime;


    public SQLTreeVisitor() {
        controller = null;
        startTime = System.currentTimeMillis();
    }

    public long getTimeDelta() {
        long currentTime = System.currentTimeMillis();
        long result = currentTime - startTime;
        startTime = currentTime;
        return result;
    }

    @Override
    protected Object aggregateResult(Object aggregate, Object nextResult) {
        return (nextResult == null) ? aggregate : nextResult;
    }

    @Override
    public Object visitProgram(SQLParser.ProgramContext ctx) {
        List<ResultItem> results = new ArrayList<>();
        for(SQLParser.StatementContext statementContext : ctx.statement()) {
            try {
                ResultItem result = (ResultItem) statementContext.accept(this);
                long cost = getTimeDelta();
                if (result != null) {
                    result.cost = cost;
                    results.add(result);
                }
            } catch (Exception e) {
                e.printStackTrace();
                ResultItem errorResult = new MessageResult(e.getMessage(), true);
                errorResult.cost = getTimeDelta();
                results.add(errorResult);
                break;
            }
        }
        return results;
    }

    @Override
    public Object visitCreate_db(SQLParser.Create_dbContext ctx) {
        try {
            controller.createDatabase(ctx.Identifier().getText());
            return new MessageResult("ok");
        } catch (FileAlreadyExistsException e) {
            return new MessageResult(e.getMessage(), true);
        }
    }

    @Override
    public Object visitDrop_db(SQLParser.Drop_dbContext ctx) {
        try {
            return controller.dropDatabase(ctx.Identifier().getText());
        } catch (FileNotFoundException e) {
            return new MessageResult(e.getMessage(), true);
        }
    }

    @Override
    public Object visitUse_db(SQLParser.Use_dbContext ctx) {
        try {
            return controller.useDatabase(ctx.Identifier().getText());
        } catch (FileNotFoundException e) {
            return new MessageResult(e.getMessage(), true);
        }
    }

    @Override
    public Object visitShow_dbs(SQLParser.Show_dbsContext ctx) {
        return new ListResult(controller.getDatabases(), "DATABASES");
    }

    @Override
    public Object visitShow_tables(SQLParser.Show_tablesContext ctx) {
        return controller.showTables();
    }

    private static class ValueType {
        public String type;
        public int size;

        public ValueType(String type, int size) {
            this.type = type;
            this.size = size;
        }
    }

    public static class ForeignKey {
        public String targetTable;
        public String name;

        public ForeignKey(String targetTable, String name) {
            this.targetTable = targetTable;
            this.name = name;
        }
    }

    private static class PrimaryKey {
        public List<String> fields;

        public PrimaryKey(List<String> fields) {
            this.fields = fields;
        }
    }

    private static class ColumnBundle {
        public Map<String, ColumnInfo> columnInfos;
        public Map<String, ForeignKey> foreignKeys;
        public PrimaryKey primaryKey;

        public ColumnBundle(Map<String, ColumnInfo> columnInfos, Map<String, ForeignKey> foreignKeys, PrimaryKey primaryKey) {
            this.columnInfos = columnInfos;
            this.foreignKeys = foreignKeys;
            this.primaryKey = primaryKey;
        }
    }

    @Override
    public Object visitCreate_table(SQLParser.Create_tableContext ctx) {
        ColumnBundle bundle = (ColumnBundle) ctx.field_list().accept(this);
        String tableName = ctx.Identifier().getText();
        ResultItem result = controller.createTable(new TableInfo(tableName, bundle.columnInfos));
        if(((MessageResult)result).isError())
            return result;
        try {
            for (Map.Entry<String, ForeignKey> entry : bundle.foreignKeys.entrySet()) {
                controller.addForeignKey(tableName, entry.getValue(), entry.getKey());
            }
        } catch (IOException e) {
            return new MessageResult(e.getMessage(), true);
        }
//        controller.setPrimaryKey(tableName, bundle.primaryKey);
        return new MessageResult("ok");
    }

    @Override
    public Object visitDrop_table(SQLParser.Drop_tableContext ctx) {
        return super.visitDrop_table(ctx);
    }

    @Override
    public Object visitField_list(SQLParser.Field_listContext ctx) {
        Map<String, ColumnInfo> columns = new HashMap<>();
        Map<String, ForeignKey> foreignKeyMap = new HashMap<>();
        PrimaryKey primaryKey = null;
        for (SQLParser.FieldContext field : ctx.field()) {
            if (field.getClass() == SQLParser.Normal_fieldContext.class) {
                String name = ((SQLParser.Normal_fieldContext) field).Identifier().getText();
                ValueType type = (ValueType) ((SQLParser.Normal_fieldContext) field).type_().accept(this);
                columns.put(name, new ColumnInfo(type.type, name, type.size, null));
            } else if (field.getClass() == SQLParser.Foreign_key_fieldContext.class) {
                String[] keys = (String[]) field.accept(this);
                if (keys.length != 3 || foreignKeyMap.containsKey(keys[0]))
                    throw new ParseError("Field list parse error when parsing foreign key");
                foreignKeyMap.put(keys[0], new ForeignKey(keys[1], keys[2]));
            } else if (field.getClass() == SQLParser.Primary_key_fieldContext.class) {
                PrimaryKey key = (PrimaryKey) field.accept(this);
                for(String name : key.fields) {
                    if(!columns.containsKey(name))
                        throw new ParseError("Unknown field " + name);
                }
                if(primaryKey != null)
                    throw new ParseError("Only one primary key field is supported");
                primaryKey = key;
            } else {
                throw new ParseError("Unknown field type");
            }
        }
        return new ColumnBundle(columns, foreignKeyMap, primaryKey);
    }

    @Override
    public Object visitNormal_field(SQLParser.Normal_fieldContext ctx) {
        ValueType type = (ValueType) ctx.type_().accept(this);
        return new ColumnInfo(type.type, ctx.Identifier().getText(), type.size, null);
    }

    @Override
    public Object visitPrimary_key_field(SQLParser.Primary_key_fieldContext ctx) {
        return ctx.identifiers().accept(this);
    }

    @Override
    public Object visitForeign_key_field(SQLParser.Foreign_key_fieldContext ctx) {
        String[] keys = new String[3];
        int cnt = 0;
        for(TerminalNode node : ctx.Identifier())
            keys[cnt++] = node.getText();
        return keys;
    }

    @Override
    public Object visitType_(SQLParser.Type_Context ctx) {
        return new ValueType(ctx.getChild(0).getText(), ctx.Integer() == null ? 0 : Integer.parseInt(ctx.Integer().toString()));
    }

    @Override
    public Object visitIdentifiers(SQLParser.IdentifiersContext ctx) {
        List<String> keys = new ArrayList<>();
        for(TerminalNode node : ctx.Identifier())
            keys.add(node.getText());
        return new PrimaryKey(keys);
    }
}
