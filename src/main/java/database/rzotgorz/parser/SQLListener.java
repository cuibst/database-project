// Generated from C:/Users/dell/Desktop/dbms-homework/src/main/resources\SQL.g4 by ANTLR 4.9.1
package database.rzotgorz.parser;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SQLParser}.
 */
public interface SQLListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SQLParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(SQLParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(SQLParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(SQLParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(SQLParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code create_db}
	 * labeled alternative in {@link SQLParser#db_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_db(SQLParser.Create_dbContext ctx);
	/**
	 * Exit a parse tree produced by the {@code create_db}
	 * labeled alternative in {@link SQLParser#db_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_db(SQLParser.Create_dbContext ctx);
	/**
	 * Enter a parse tree produced by the {@code drop_db}
	 * labeled alternative in {@link SQLParser#db_statement}.
	 * @param ctx the parse tree
	 */
	void enterDrop_db(SQLParser.Drop_dbContext ctx);
	/**
	 * Exit a parse tree produced by the {@code drop_db}
	 * labeled alternative in {@link SQLParser#db_statement}.
	 * @param ctx the parse tree
	 */
	void exitDrop_db(SQLParser.Drop_dbContext ctx);
	/**
	 * Enter a parse tree produced by the {@code show_dbs}
	 * labeled alternative in {@link SQLParser#db_statement}.
	 * @param ctx the parse tree
	 */
	void enterShow_dbs(SQLParser.Show_dbsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code show_dbs}
	 * labeled alternative in {@link SQLParser#db_statement}.
	 * @param ctx the parse tree
	 */
	void exitShow_dbs(SQLParser.Show_dbsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code use_db}
	 * labeled alternative in {@link SQLParser#db_statement}.
	 * @param ctx the parse tree
	 */
	void enterUse_db(SQLParser.Use_dbContext ctx);
	/**
	 * Exit a parse tree produced by the {@code use_db}
	 * labeled alternative in {@link SQLParser#db_statement}.
	 * @param ctx the parse tree
	 */
	void exitUse_db(SQLParser.Use_dbContext ctx);
	/**
	 * Enter a parse tree produced by the {@code show_tables}
	 * labeled alternative in {@link SQLParser#db_statement}.
	 * @param ctx the parse tree
	 */
	void enterShow_tables(SQLParser.Show_tablesContext ctx);
	/**
	 * Exit a parse tree produced by the {@code show_tables}
	 * labeled alternative in {@link SQLParser#db_statement}.
	 * @param ctx the parse tree
	 */
	void exitShow_tables(SQLParser.Show_tablesContext ctx);
	/**
	 * Enter a parse tree produced by the {@code show_indexes}
	 * labeled alternative in {@link SQLParser#db_statement}.
	 * @param ctx the parse tree
	 */
	void enterShow_indexes(SQLParser.Show_indexesContext ctx);
	/**
	 * Exit a parse tree produced by the {@code show_indexes}
	 * labeled alternative in {@link SQLParser#db_statement}.
	 * @param ctx the parse tree
	 */
	void exitShow_indexes(SQLParser.Show_indexesContext ctx);
	/**
	 * Enter a parse tree produced by the {@code load_data}
	 * labeled alternative in {@link SQLParser#io_statement}.
	 * @param ctx the parse tree
	 */
	void enterLoad_data(SQLParser.Load_dataContext ctx);
	/**
	 * Exit a parse tree produced by the {@code load_data}
	 * labeled alternative in {@link SQLParser#io_statement}.
	 * @param ctx the parse tree
	 */
	void exitLoad_data(SQLParser.Load_dataContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dump_data}
	 * labeled alternative in {@link SQLParser#io_statement}.
	 * @param ctx the parse tree
	 */
	void enterDump_data(SQLParser.Dump_dataContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dump_data}
	 * labeled alternative in {@link SQLParser#io_statement}.
	 * @param ctx the parse tree
	 */
	void exitDump_data(SQLParser.Dump_dataContext ctx);
	/**
	 * Enter a parse tree produced by the {@code create_table}
	 * labeled alternative in {@link SQLParser#table_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_table(SQLParser.Create_tableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code create_table}
	 * labeled alternative in {@link SQLParser#table_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_table(SQLParser.Create_tableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code drop_table}
	 * labeled alternative in {@link SQLParser#table_statement}.
	 * @param ctx the parse tree
	 */
	void enterDrop_table(SQLParser.Drop_tableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code drop_table}
	 * labeled alternative in {@link SQLParser#table_statement}.
	 * @param ctx the parse tree
	 */
	void exitDrop_table(SQLParser.Drop_tableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code describe_table}
	 * labeled alternative in {@link SQLParser#table_statement}.
	 * @param ctx the parse tree
	 */
	void enterDescribe_table(SQLParser.Describe_tableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code describe_table}
	 * labeled alternative in {@link SQLParser#table_statement}.
	 * @param ctx the parse tree
	 */
	void exitDescribe_table(SQLParser.Describe_tableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code insert_into_table}
	 * labeled alternative in {@link SQLParser#table_statement}.
	 * @param ctx the parse tree
	 */
	void enterInsert_into_table(SQLParser.Insert_into_tableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code insert_into_table}
	 * labeled alternative in {@link SQLParser#table_statement}.
	 * @param ctx the parse tree
	 */
	void exitInsert_into_table(SQLParser.Insert_into_tableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code delete_from_table}
	 * labeled alternative in {@link SQLParser#table_statement}.
	 * @param ctx the parse tree
	 */
	void enterDelete_from_table(SQLParser.Delete_from_tableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code delete_from_table}
	 * labeled alternative in {@link SQLParser#table_statement}.
	 * @param ctx the parse tree
	 */
	void exitDelete_from_table(SQLParser.Delete_from_tableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code update_table}
	 * labeled alternative in {@link SQLParser#table_statement}.
	 * @param ctx the parse tree
	 */
	void enterUpdate_table(SQLParser.Update_tableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code update_table}
	 * labeled alternative in {@link SQLParser#table_statement}.
	 * @param ctx the parse tree
	 */
	void exitUpdate_table(SQLParser.Update_tableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code select_table_}
	 * labeled alternative in {@link SQLParser#table_statement}.
	 * @param ctx the parse tree
	 */
	void enterSelect_table_(SQLParser.Select_table_Context ctx);
	/**
	 * Exit a parse tree produced by the {@code select_table_}
	 * labeled alternative in {@link SQLParser#table_statement}.
	 * @param ctx the parse tree
	 */
	void exitSelect_table_(SQLParser.Select_table_Context ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#select_table}.
	 * @param ctx the parse tree
	 */
	void enterSelect_table(SQLParser.Select_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#select_table}.
	 * @param ctx the parse tree
	 */
	void exitSelect_table(SQLParser.Select_tableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alter_add_index}
	 * labeled alternative in {@link SQLParser#alter_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_add_index(SQLParser.Alter_add_indexContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alter_add_index}
	 * labeled alternative in {@link SQLParser#alter_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_add_index(SQLParser.Alter_add_indexContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alter_drop_index}
	 * labeled alternative in {@link SQLParser#alter_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_drop_index(SQLParser.Alter_drop_indexContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alter_drop_index}
	 * labeled alternative in {@link SQLParser#alter_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_drop_index(SQLParser.Alter_drop_indexContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alter_table_drop_pk}
	 * labeled alternative in {@link SQLParser#alter_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_table_drop_pk(SQLParser.Alter_table_drop_pkContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alter_table_drop_pk}
	 * labeled alternative in {@link SQLParser#alter_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_table_drop_pk(SQLParser.Alter_table_drop_pkContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alter_table_drop_foreign_key}
	 * labeled alternative in {@link SQLParser#alter_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_table_drop_foreign_key(SQLParser.Alter_table_drop_foreign_keyContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alter_table_drop_foreign_key}
	 * labeled alternative in {@link SQLParser#alter_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_table_drop_foreign_key(SQLParser.Alter_table_drop_foreign_keyContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alter_table_add_pk}
	 * labeled alternative in {@link SQLParser#alter_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_table_add_pk(SQLParser.Alter_table_add_pkContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alter_table_add_pk}
	 * labeled alternative in {@link SQLParser#alter_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_table_add_pk(SQLParser.Alter_table_add_pkContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alter_table_add_foreign_key}
	 * labeled alternative in {@link SQLParser#alter_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_table_add_foreign_key(SQLParser.Alter_table_add_foreign_keyContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alter_table_add_foreign_key}
	 * labeled alternative in {@link SQLParser#alter_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_table_add_foreign_key(SQLParser.Alter_table_add_foreign_keyContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alter_table_add_unique}
	 * labeled alternative in {@link SQLParser#alter_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_table_add_unique(SQLParser.Alter_table_add_uniqueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alter_table_add_unique}
	 * labeled alternative in {@link SQLParser#alter_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_table_add_unique(SQLParser.Alter_table_add_uniqueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alter_table_add_column}
	 * labeled alternative in {@link SQLParser#alter_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_table_add_column(SQLParser.Alter_table_add_columnContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alter_table_add_column}
	 * labeled alternative in {@link SQLParser#alter_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_table_add_column(SQLParser.Alter_table_add_columnContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alter_table_drop_column}
	 * labeled alternative in {@link SQLParser#alter_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_table_drop_column(SQLParser.Alter_table_drop_columnContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alter_table_drop_column}
	 * labeled alternative in {@link SQLParser#alter_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_table_drop_column(SQLParser.Alter_table_drop_columnContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#field_list}.
	 * @param ctx the parse tree
	 */
	void enterField_list(SQLParser.Field_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#field_list}.
	 * @param ctx the parse tree
	 */
	void exitField_list(SQLParser.Field_listContext ctx);
	/**
	 * Enter a parse tree produced by the {@code normal_field}
	 * labeled alternative in {@link SQLParser#field}.
	 * @param ctx the parse tree
	 */
	void enterNormal_field(SQLParser.Normal_fieldContext ctx);
	/**
	 * Exit a parse tree produced by the {@code normal_field}
	 * labeled alternative in {@link SQLParser#field}.
	 * @param ctx the parse tree
	 */
	void exitNormal_field(SQLParser.Normal_fieldContext ctx);
	/**
	 * Enter a parse tree produced by the {@code primary_key_field}
	 * labeled alternative in {@link SQLParser#field}.
	 * @param ctx the parse tree
	 */
	void enterPrimary_key_field(SQLParser.Primary_key_fieldContext ctx);
	/**
	 * Exit a parse tree produced by the {@code primary_key_field}
	 * labeled alternative in {@link SQLParser#field}.
	 * @param ctx the parse tree
	 */
	void exitPrimary_key_field(SQLParser.Primary_key_fieldContext ctx);
	/**
	 * Enter a parse tree produced by the {@code foreign_key_field}
	 * labeled alternative in {@link SQLParser#field}.
	 * @param ctx the parse tree
	 */
	void enterForeign_key_field(SQLParser.Foreign_key_fieldContext ctx);
	/**
	 * Exit a parse tree produced by the {@code foreign_key_field}
	 * labeled alternative in {@link SQLParser#field}.
	 * @param ctx the parse tree
	 */
	void exitForeign_key_field(SQLParser.Foreign_key_fieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#type_}.
	 * @param ctx the parse tree
	 */
	void enterType_(SQLParser.Type_Context ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#type_}.
	 * @param ctx the parse tree
	 */
	void exitType_(SQLParser.Type_Context ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#value_lists}.
	 * @param ctx the parse tree
	 */
	void enterValue_lists(SQLParser.Value_listsContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#value_lists}.
	 * @param ctx the parse tree
	 */
	void exitValue_lists(SQLParser.Value_listsContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#value_list}.
	 * @param ctx the parse tree
	 */
	void enterValue_list(SQLParser.Value_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#value_list}.
	 * @param ctx the parse tree
	 */
	void exitValue_list(SQLParser.Value_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(SQLParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(SQLParser.ValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#where_and_clause}.
	 * @param ctx the parse tree
	 */
	void enterWhere_and_clause(SQLParser.Where_and_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#where_and_clause}.
	 * @param ctx the parse tree
	 */
	void exitWhere_and_clause(SQLParser.Where_and_clauseContext ctx);
	/**
	 * Enter a parse tree produced by the {@code where_operator_expression}
	 * labeled alternative in {@link SQLParser#where_clause}.
	 * @param ctx the parse tree
	 */
	void enterWhere_operator_expression(SQLParser.Where_operator_expressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code where_operator_expression}
	 * labeled alternative in {@link SQLParser#where_clause}.
	 * @param ctx the parse tree
	 */
	void exitWhere_operator_expression(SQLParser.Where_operator_expressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code where_operator_select}
	 * labeled alternative in {@link SQLParser#where_clause}.
	 * @param ctx the parse tree
	 */
	void enterWhere_operator_select(SQLParser.Where_operator_selectContext ctx);
	/**
	 * Exit a parse tree produced by the {@code where_operator_select}
	 * labeled alternative in {@link SQLParser#where_clause}.
	 * @param ctx the parse tree
	 */
	void exitWhere_operator_select(SQLParser.Where_operator_selectContext ctx);
	/**
	 * Enter a parse tree produced by the {@code where_null}
	 * labeled alternative in {@link SQLParser#where_clause}.
	 * @param ctx the parse tree
	 */
	void enterWhere_null(SQLParser.Where_nullContext ctx);
	/**
	 * Exit a parse tree produced by the {@code where_null}
	 * labeled alternative in {@link SQLParser#where_clause}.
	 * @param ctx the parse tree
	 */
	void exitWhere_null(SQLParser.Where_nullContext ctx);
	/**
	 * Enter a parse tree produced by the {@code where_in_list}
	 * labeled alternative in {@link SQLParser#where_clause}.
	 * @param ctx the parse tree
	 */
	void enterWhere_in_list(SQLParser.Where_in_listContext ctx);
	/**
	 * Exit a parse tree produced by the {@code where_in_list}
	 * labeled alternative in {@link SQLParser#where_clause}.
	 * @param ctx the parse tree
	 */
	void exitWhere_in_list(SQLParser.Where_in_listContext ctx);
	/**
	 * Enter a parse tree produced by the {@code where_in_select}
	 * labeled alternative in {@link SQLParser#where_clause}.
	 * @param ctx the parse tree
	 */
	void enterWhere_in_select(SQLParser.Where_in_selectContext ctx);
	/**
	 * Exit a parse tree produced by the {@code where_in_select}
	 * labeled alternative in {@link SQLParser#where_clause}.
	 * @param ctx the parse tree
	 */
	void exitWhere_in_select(SQLParser.Where_in_selectContext ctx);
	/**
	 * Enter a parse tree produced by the {@code where_like_string}
	 * labeled alternative in {@link SQLParser#where_clause}.
	 * @param ctx the parse tree
	 */
	void enterWhere_like_string(SQLParser.Where_like_stringContext ctx);
	/**
	 * Exit a parse tree produced by the {@code where_like_string}
	 * labeled alternative in {@link SQLParser#where_clause}.
	 * @param ctx the parse tree
	 */
	void exitWhere_like_string(SQLParser.Where_like_stringContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#column}.
	 * @param ctx the parse tree
	 */
	void enterColumn(SQLParser.ColumnContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#column}.
	 * @param ctx the parse tree
	 */
	void exitColumn(SQLParser.ColumnContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(SQLParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(SQLParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#set_clause}.
	 * @param ctx the parse tree
	 */
	void enterSet_clause(SQLParser.Set_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#set_clause}.
	 * @param ctx the parse tree
	 */
	void exitSet_clause(SQLParser.Set_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#selectors}.
	 * @param ctx the parse tree
	 */
	void enterSelectors(SQLParser.SelectorsContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#selectors}.
	 * @param ctx the parse tree
	 */
	void exitSelectors(SQLParser.SelectorsContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#selector}.
	 * @param ctx the parse tree
	 */
	void enterSelector(SQLParser.SelectorContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#selector}.
	 * @param ctx the parse tree
	 */
	void exitSelector(SQLParser.SelectorContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#identifiers}.
	 * @param ctx the parse tree
	 */
	void enterIdentifiers(SQLParser.IdentifiersContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#identifiers}.
	 * @param ctx the parse tree
	 */
	void exitIdentifiers(SQLParser.IdentifiersContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#operator}.
	 * @param ctx the parse tree
	 */
	void enterOperator(SQLParser.OperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#operator}.
	 * @param ctx the parse tree
	 */
	void exitOperator(SQLParser.OperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#aggregator}.
	 * @param ctx the parse tree
	 */
	void enterAggregator(SQLParser.AggregatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#aggregator}.
	 * @param ctx the parse tree
	 */
	void exitAggregator(SQLParser.AggregatorContext ctx);
}