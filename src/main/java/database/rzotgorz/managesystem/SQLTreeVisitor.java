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
import java.io.Serializable;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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

    @Override
    public Object visitShow_indexes(SQLParser.Show_indexesContext ctx) {
        return controller.showIndices();
    }

    private static class ValueType {
        public String type;
        public int size;

        public ValueType(String type, int size) {
            this.type = type;
            this.size = size;
        }
    }


    public static class ForeignKey implements Serializable {
        public String foreignKeyName;
        public String targetTable;
        public List<String> columns;
        public List<String> targetColumns;

        public ForeignKey(String foreignKeyName, String targetTable, List<String> columns, List<String> targetColumns) {
            this.foreignKeyName = foreignKeyName;
            this.targetTable = targetTable;
            this.columns = columns;
            this.targetColumns = targetColumns;
        }

        @Override
        public String toString() {
            return String.format("name: %s%n targetTable: %s%n columns: %s%n targetColumns: %s%n", foreignKeyName, targetTable, columns.toString(), targetColumns.toString());
        }
    }

    public static class PrimaryKey {
        public List<String> fields;

        public PrimaryKey(List<String> fields) {
            this.fields = fields;
        }
    }

    private static class ColumnBundle {
        public LinkedHashMap<String, ColumnInfo> columnInfos;
        public Map<String, ForeignKey> foreignKeys;
        public PrimaryKey primaryKey;

        public ColumnBundle(LinkedHashMap<String, ColumnInfo> columnInfos, Map<String, ForeignKey> foreignKeys, PrimaryKey primaryKey) {
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
                controller.addForeignKeyConstraint(tableName, entry.getValue());
            }
            if (bundle.primaryKey != null)
                controller.setPrimary(tableName, bundle.primaryKey);
        } catch (Exception e) {
            e.printStackTrace();
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
        LinkedHashMap<String, ColumnInfo> columns = new LinkedHashMap<>();
        Map<String, ForeignKey> foreignKeyMap = new LinkedHashMap<>();
        PrimaryKey primaryKey = null;
        for (SQLParser.FieldContext field : ctx.field()) {
            if (field.getClass() == SQLParser.Normal_fieldContext.class) {
                String name = ((SQLParser.Normal_fieldContext) field).Identifier().getText();
                ValueType type = (ValueType) ((SQLParser.Normal_fieldContext) field).type_().accept(this);
                ColumnInfo columnInfo = new ColumnInfo(type.type, name, type.size, null);
                if (((SQLParser.Normal_fieldContext) field).Null() != null)
                    columnInfo.setNotNull(true);
                columns.put(name, columnInfo);
            } else if (field.getClass() == SQLParser.Foreign_key_fieldContext.class) {
                ForeignKey foreignKey = (ForeignKey) field.accept(this);
                if (foreignKey.foreignKeyName == null)
                    foreignKey.foreignKeyName = foreignKey.targetTable + "." + foreignKey.targetColumns.toString();
                if (foreignKeyMap.containsKey(foreignKey.foreignKeyName))
                    throw new RuntimeException("Foreign Key parse error, duplicated foreign keys");
                foreignKeyMap.put(foreignKey.foreignKeyName, foreignKey);
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
        ColumnInfo info = new ColumnInfo(type.type, ctx.Identifier().getText(), type.size, null);
        if (ctx.Null() != null) {
            info.setNotNull(true);
        }
        return info;
    }

    @Override
    public Object visitPrimary_key_field(SQLParser.Primary_key_fieldContext ctx) {
        return ctx.identifiers().accept(this);
    }


    @Override
    public ForeignKey visitForeign_key_field(SQLParser.Foreign_key_fieldContext ctx) {
        List<String> columns = ((PrimaryKey) ctx.identifiers(0).accept(this)).fields;
        List<String> targetColumns = ((PrimaryKey) ctx.identifiers(1).accept(this)).fields;
        if (ctx.Identifier().size() == 1) {
            String targetTable = ctx.Identifier(0).getText();
            return new ForeignKey(null, targetTable, columns, targetColumns);
        } else {
            String name = ctx.Identifier(0).getText();
            String targetTable = ctx.Identifier(1).getText();
            return new ForeignKey(name, targetTable, columns, targetColumns);
        }
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
        for (Object valueList : valueLists) {
            try {
                controller.insertRecord(tableName, (List<Object>) valueList);
            } catch (Exception e) {
                e.printStackTrace();
                return new MessageResult(e.getMessage(), true);
            }
        }
        return new OperationResult("inserted", valueLists.size());
    }

    @Override
    public ResultItem visitUpdate_table(SQLParser.Update_tableContext ctx) {
        String tableName = ctx.Identifier().getText();
        List<SetClause> setClauses = (ctx.set_clause() == null) ? new ArrayList<>() : (List<SetClause>) ctx.set_clause().accept(this);
        List<WhereClause> whereClauses = (ctx.where_and_clause() == null) ? new ArrayList<>() : (List<WhereClause>) ctx.where_and_clause().accept(this);
        return controller.updateRecord(tableName, setClauses, whereClauses);
    }

    @Override
    public ResultItem visitDelete_from_table(SQLParser.Delete_from_tableContext ctx) {
        String tableName = ctx.Identifier().getText();
        List<WhereClause> whereClauses = (ctx.where_and_clause() == null) ? new ArrayList<>() : (List<WhereClause>) ctx.where_and_clause().accept(this);
        return controller.deleteRecord(tableName, whereClauses);
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
    public Object visitAlter_add_index(SQLParser.Alter_add_indexContext ctx) {
        String tableName = ctx.Identifier().getText();
        List<String> columns = ((PrimaryKey) ctx.identifiers().accept(this)).fields;
        try {
            controller.createIndex(tableName + "." + columns, tableName, columns);
        } catch (Exception e) {
            return new MessageResult(e.getMessage(), true);
        }
        return new OperationResult("added", columns.size());
    }

    @Override
    public Object visitAlter_drop_index(SQLParser.Alter_drop_indexContext ctx) {
        String tableName = ctx.Identifier().getText();
        List<String> columns = ((PrimaryKey) ctx.identifiers().accept(this)).fields;
        for (String column : columns) {
            try {
                controller.removeIndex(column);
            } catch (Exception e) {
                return new MessageResult(e.getMessage(), true);
            }
        }
        return new OperationResult("dropped", columns.size());
    }

    @Override
    public Object visitAlter_table_drop_pk(SQLParser.Alter_table_drop_pkContext ctx) {
        String tableName = ctx.Identifier(0).getText();
        controller.removePrimary(tableName);
        return new MessageResult(String.format("Primary key for table %s dropped.", tableName));
    }

    @Override
    public Object visitAlter_table_drop_foreign_key(SQLParser.Alter_table_drop_foreign_keyContext ctx) {
        String tableName = ctx.Identifier(0).getText();
        String keyName = ctx.Identifier(1).getText();
        controller.removeForeignKeyConstraint(tableName, keyName);
        return new MessageResult(String.format("Foreign key %s in table %s removed.", keyName, tableName));
    }

    @Override
    public Object visitAlter_table_add_pk(SQLParser.Alter_table_add_pkContext ctx) {
        String tableName = ctx.Identifier(0).getText();
        PrimaryKey primaryKey = (PrimaryKey) ctx.identifiers().accept(this);
        try {
            controller.setPrimary(tableName, primaryKey);
        } catch (Exception e) {
            return new MessageResult(e.getMessage(), true);
        }
        return new MessageResult("Primary key set for table " + tableName);
    }

    @Override
    public Object visitAlter_table_add_foreign_key(SQLParser.Alter_table_add_foreign_keyContext ctx) {
        String tableName = ctx.Identifier(0).getText();
        String keyName = ctx.Identifier(1).getText();
        String targetTable = ctx.Identifier(2).getText();
        List<String> columns = ((PrimaryKey) ctx.identifiers(0).accept(this)).fields;
        List<String> targetColumns = ((PrimaryKey) ctx.identifiers(1).accept(this)).fields;
        try {
            controller.addForeignKeyConstraint(tableName, new ForeignKey(keyName, targetTable, columns, targetColumns));
        } catch (Exception e) {
            return new MessageResult(e.getMessage(), true);
        }
        return new MessageResult(String.format("Foreign key %s for table %s added.", keyName, tableName));
    }

    @Override
    public Object visitAlter_table_add_unique(SQLParser.Alter_table_add_uniqueContext ctx) {
        String tableName = ctx.Identifier().getText();
        List<String> columns = ((PrimaryKey) ctx.identifiers().accept(this)).fields;
        try {
            controller.addUniqueConstraint(tableName, tableName + "." + columns.toString() + ".unique", columns);
        } catch (Exception e) {
            return new MessageResult(e.getMessage(), true);
        }
        return new MessageResult(String.format("Unique constraints %s added", tableName + "." + columns.toString() + ".unique"));
    }

    @Override
    public Object visitAlter_table_add_column(SQLParser.Alter_table_add_columnContext ctx) {
        String tableName = ctx.Identifier().get(0).getText();
        String columnName = ctx.Identifier().get(1).getText();
        ValueType type = (ValueType) ctx.type_().accept(this);
        ColumnInfo columnInfo;
        if (ctx.value() != null) {
            Object value = ctx.value().accept(this);
            columnInfo = new ColumnInfo(type.type, columnName, type.size, value);
        } else
            columnInfo = new ColumnInfo(type.type, columnName, type.size, null);
        if (ctx.Null() != null) {
            if (columnInfo.getDefaultValue() == null)
                throw new RuntimeException("Null value must have a default value! ");
            columnInfo.setNotNull(true);
        }

        controller.insertColumn(tableName, columnInfo);
        return new MessageResult(String.format("Add new column : %s type : %s in table : %s ", columnName, type.type, tableName));
    }

    @Override
    public Object visitAlter_table_drop_column(SQLParser.Alter_table_drop_columnContext ctx) {
        String tableName = ctx.Identifier().get(0).getText();
        String columnName = ctx.Identifier().get(1).getText();
        return new MessageResult(String.format("Drop column : %s in table : %s ", columnName, tableName));
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
    public List<SetClause> visitSet_clause(SQLParser.Set_clauseContext ctx) {
        List<SetClause> result = new ArrayList<>();
        for (int i = 0; i < ctx.value().size(); i++) {
            result.add(new SetClause(ctx.Identifier(i).toString(), ctx.value().get(i).getText()));
        }
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
