package database.rzotgorz.managesystem;

import database.rzotgorz.exceptions.ParseError;
import database.rzotgorz.managesystem.clauses.*;
import database.rzotgorz.managesystem.results.*;
import database.rzotgorz.metaSystem.ColumnInfo;
import database.rzotgorz.metaSystem.TableInfo;
import database.rzotgorz.parser.SQLBaseVisitor;
import database.rzotgorz.parser.SQLParser;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public List<ResultItem> visitProgram(SQLParser.ProgramContext ctx) {
        List<ResultItem> results = new ArrayList<>();
        for (SQLParser.StatementContext statementContext : ctx.statement()) {
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
    public MessageResult visitCreate_db(SQLParser.Create_dbContext ctx) {
        try {
            controller.createDatabase(ctx.Identifier().getText());
            return new MessageResult("ok");
        } catch (FileAlreadyExistsException e) {
            return new MessageResult(e.getMessage(), true);
        }
    }

    @Override
    public ResultItem visitDrop_db(SQLParser.Drop_dbContext ctx) {
        try {
            return controller.dropDatabase(ctx.Identifier().getText());
        } catch (FileNotFoundException e) {
            return new MessageResult(e.getMessage(), true);
        }
    }

    @Override
    public ResultItem visitUse_db(SQLParser.Use_dbContext ctx) {
        try {
            return controller.useDatabase(ctx.Identifier().getText());
        } catch (FileNotFoundException e) {
            return new MessageResult(e.getMessage(), true);
        }
    }

    @Override
    public ListResult visitShow_dbs(SQLParser.Show_dbsContext ctx) {
        return new ListResult(controller.getDatabases(), "DATABASES");
    }

    @Override
    public ResultItem visitShow_tables(SQLParser.Show_tablesContext ctx) {
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

    public static class PrimaryKey {
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
    public ResultItem visitCreate_table(SQLParser.Create_tableContext ctx) {
        ColumnBundle bundle = (ColumnBundle) ctx.field_list().accept(this);
        String tableName = ctx.Identifier().getText();
        ResultItem result = controller.createTable(new TableInfo(tableName, bundle.columnInfos));
        if (((MessageResult) result).isError())
            return result;
        try {
            for (Map.Entry<String, ForeignKey> entry : bundle.foreignKeys.entrySet()) {
                controller.addForeignKeyConstraint(tableName, entry.getKey(), entry.getValue(), null);
            }
            if (bundle.primaryKey != null)
                controller.setPrimary(tableName, bundle.primaryKey);
        } catch (Exception e) {
            return new MessageResult(e.getMessage(), true);
        }
        return new MessageResult("ok");
    }

    @Override
    public ResultItem visitDrop_table(SQLParser.Drop_tableContext ctx) {
        String tableName = ctx.Identifier().getText();
        return controller.dropTable(tableName);
    }

    @Override
    public ColumnBundle visitField_list(SQLParser.Field_listContext ctx) {
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
                for (String name : key.fields) {
                    if (!columns.containsKey(name))
                        throw new ParseError("Unknown field " + name);
                }
                if (primaryKey != null)
                    throw new ParseError("Only one primary key field is supported");
                primaryKey = key;
            } else {
                throw new ParseError("Unknown field type");
            }
        }
        return new ColumnBundle(columns, foreignKeyMap, primaryKey);
    }

    @Override
    public ColumnInfo visitNormal_field(SQLParser.Normal_fieldContext ctx) {
        ValueType type = (ValueType) ctx.type_().accept(this);
        return new ColumnInfo(type.type, ctx.Identifier().getText(), type.size, null);
    }

    @Override
    public Object visitPrimary_key_field(SQLParser.Primary_key_fieldContext ctx) {
        return ctx.identifiers().accept(this);
    }

    @Override
    public String[] visitForeign_key_field(SQLParser.Foreign_key_fieldContext ctx) {
        String[] keys = new String[3];
        int cnt = 0;
        for (TerminalNode node : ctx.Identifier())
            keys[cnt++] = node.getText();
        return keys;
    }

    @Override
    public ValueType visitType_(SQLParser.Type_Context ctx) {
        return new ValueType(ctx.getChild(0).getText(), ctx.Integer() == null ? 0 : Integer.parseInt(ctx.Integer().toString()));
    }

    @Override
    public Object visitDescribe_table(SQLParser.Describe_tableContext ctx) {
        return controller.describeTable(ctx.Identifier().getText());
    }

    @Override
    public Object visitLoad_data(SQLParser.Load_dataContext ctx) {
        return controller.loadData(ctx.String().getText() + ".csv", ctx.Identifier().getText());
    }

    @Override
    public Object visitDump_data(SQLParser.Dump_dataContext ctx) {
        return controller.storeData(ctx.String().getText() + ".csv", ctx.Identifier().getText());
    }

    @Override
    public PrimaryKey visitIdentifiers(SQLParser.IdentifiersContext ctx) {
        List<String> keys = new ArrayList<>();
        for (TerminalNode node : ctx.Identifier())
            keys.add(node.getText());
        return new PrimaryKey(keys);
    }

    @Override
    public ResultItem visitInsert_into_table(SQLParser.Insert_into_tableContext ctx) {
        String tableName = ctx.getChild(2).getText();
        List<Object> valueLists = (List<Object>) ctx.value_lists().accept(this);
//        log.info("value list size: {}", valueLists.size());
        for (Object valueList : valueLists)
            controller.insertRecord(tableName, (List<Object>) valueList);
        return new OperationResult("inserted", valueLists.size());
    }

    @Override
    public List<Object> visitValue_lists(SQLParser.Value_listsContext ctx) {
        List<Object> result = new ArrayList<>();
        for (SQLParser.Value_listContext list : ctx.value_list())
            result.add(list.accept(this));
        return result;
    }

    @Override
    public List<Object> visitValue_list(SQLParser.Value_listContext ctx) {
        List<Object> result = new ArrayList<>();
        for (SQLParser.ValueContext value : ctx.value())
            result.add(value.accept(this));
//        log.info("value list size: {}", result.size());
        return result;
    }

    @Override
    public Object visitValue(SQLParser.ValueContext ctx) {
        if (ctx.Integer() != null)
            return Integer.parseInt(ctx.getText());
        else if (ctx.Float() != null)
            return Float.parseFloat(ctx.getText());
        else if (ctx.String() != null)
            return ctx.getText().substring(1, ctx.getText().length() - 1);
        else
            return null;
    }


    @Override
    public Object visitSelect_table(SQLParser.Select_tableContext ctx) {
        List<String> tableNames = ((PrimaryKey) ctx.identifiers().accept(this)).fields;
        List<WhereClause> clauses = (ctx.where_and_clause() == null) ? new ArrayList<>() : (List<WhereClause>) ctx.where_and_clause().accept(this);
        Object selector = ctx.selectors().accept(this);
        List<Selector> selectors = new ArrayList<>();
        if (selector instanceof Selector)
            selectors.add((Selector) selector);
        else
            selectors = (List<Selector>) selector;

        //FIXME: group by later on
        Column column = ctx.column() == null ? null : (Column) ctx.column().accept(this);

        int limit = (ctx.Integer(0) == null) ? -1 : Integer.parseInt(ctx.Integer(0).getText());
        int offset = (ctx.Integer(1) == null) ? 0 : Integer.parseInt(ctx.Integer(1).getText());
        return controller.selectWithLimit(selectors, tableNames, clauses, limit, offset, column);
    }

    @Override
    public Object visitSelectors(SQLParser.SelectorsContext ctx) {
        if (ctx.getChild(0).getText().equals("*"))
            return new Selector(Selector.SelectorType.ALL, "*", "*", Selector.AggregatorType.MAX);
        List<Selector> selectors = new ArrayList<>();
        ctx.selector().forEach(selectorContext -> selectors.add((Selector) selectorContext.accept(this)));
        return selectors;
    }

    @Override
    public Selector visitSelector(SQLParser.SelectorContext ctx) {
        if (ctx.Count() != null)
            return new Selector(Selector.SelectorType.COUNTER, "*", "*", Selector.AggregatorType.MAX);
        Column column = (Column) ctx.column().accept(this);
        if (ctx.aggregator() != null)
            return new Selector(Selector.SelectorType.AGGREGATION, column.tableName, column.columnName, Selector.AggregatorType.valueOf(ctx.aggregator().getText()));
        return new Selector(Selector.SelectorType.FIELD, column.tableName, column.columnName, Selector.AggregatorType.MAX);
    }

    @Override
    public List<WhereClause> visitWhere_and_clause(SQLParser.Where_and_clauseContext ctx) {
        List<WhereClause> result = new ArrayList<>();
        ctx.where_clause().forEach(where_clauseContext -> result.add((WhereClause) where_clauseContext.accept(this)));
        return result;
    }

    @Override
    public WhereClause visitWhere_operator_expression(SQLParser.Where_operator_expressionContext ctx) {
        Column column = (Column) ctx.column().accept(this);
        String op = ctx.operator().getText();
        Object value = ctx.expression().accept(this);
        if (value.getClass() == Column.class)
            return new ColumnOperatorClause(column.tableName, column.columnName, op, ((Column) value).tableName, ((Column) value).columnName);
        else
            return new ValueOperatorClause(column.tableName, column.columnName, op, value);
    }

    @Override
    public ValueOperatorClause visitWhere_operator_select(SQLParser.Where_operator_selectContext ctx) {
        Column column = (Column) ctx.column().accept(this);
        String op = ctx.operator().getText();
        ResultItem resultItem = (ResultItem) ctx.select_table().accept(this);
        //FIXME: Convert the result item to value
        return new ValueOperatorClause(column.tableName, column.columnName, op, resultItem);
    }

    @Override
    public NullClause visitWhere_null(SQLParser.Where_nullContext ctx) {
        Column column = (Column) ctx.column().accept(this);
        boolean isNull = !ctx.getChild(2).getText().equals("NOT");
        return new NullClause(column.tableName, column.columnName, isNull);
    }

    @Override
    public WhereInClause visitWhere_in_list(SQLParser.Where_in_listContext ctx) {
        Column column = (Column) ctx.column().accept(this);
        List<Object> list = (List<Object>) ctx.value_list().accept(this);
        return new WhereInClause(column.tableName, column.columnName, list);
    }

    @Override
    public WhereInClause visitWhere_in_select(SQLParser.Where_in_selectContext ctx) {
        Column column = (Column) ctx.column().accept(this);
        ResultItem resultItem = (ResultItem) ctx.select_table().accept(this);
        //FIXME: Convert the result item to value list
        if (resultItem instanceof MessageResult)
            throw new RuntimeException(((MessageResult) resultItem).getMessage());
        assert resultItem instanceof TableResult;
        TableResult result = (TableResult) resultItem;
        List<Object> values = new ArrayList<>();
        result.getData().forEach(list -> {
            if (list.size() != 1)
                throw new RuntimeException("In clause get multiple columns.");
            values.add(list.get(0));
        });
        return new WhereInClause(column.tableName, column.columnName, values);
    }

    @Override
    public LikeClause visitWhere_like_string(SQLParser.Where_like_stringContext ctx) {
        Column column = (Column) ctx.column().accept(this);
        String pattern = ctx.String().getText().substring(1, ctx.String().getText().length() - 1);
        return new LikeClause(column.tableName, column.columnName, pattern);
    }

    public static class Column {
        public String tableName;
        public String columnName;

        public Column(String tableName, String columnName) {
            this.tableName = tableName;
            this.columnName = columnName;
        }
    }

    @Override
    public Object visitColumn(SQLParser.ColumnContext ctx) {
        if (ctx.Identifier().size() == 1)
            return new Column(null, ctx.Identifier(0).getText());
        return new Column(ctx.Identifier(0).getText(), ctx.Identifier(1).getText());
    }
}
