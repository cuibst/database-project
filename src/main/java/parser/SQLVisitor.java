// Generated from D:/git-repo/dbms-homework/src/main/resources\SQL.g4 by ANTLR 4.9.1
package parser;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link SQLParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface SQLVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link SQLParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(SQLParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(SQLParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code create_db}
	 * labeled alternative in {@link SQLParser#db_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_db(SQLParser.Create_dbContext ctx);
	/**
	 * Visit a parse tree produced by the {@code drop_db}
	 * labeled alternative in {@link SQLParser#db_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_db(SQLParser.Drop_dbContext ctx);
	/**
	 * Visit a parse tree produced by the {@code show_dbs}
	 * labeled alternative in {@link SQLParser#db_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_dbs(SQLParser.Show_dbsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code use_db}
	 * labeled alternative in {@link SQLParser#db_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUse_db(SQLParser.Use_dbContext ctx);
	/**
	 * Visit a parse tree produced by the {@code show_tables}
	 * labeled alternative in {@link SQLParser#db_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_tables(SQLParser.Show_tablesContext ctx);
	/**
	 * Visit a parse tree produced by the {@code show_indexes}
	 * labeled alternative in {@link SQLParser#db_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_indexes(SQLParser.Show_indexesContext ctx);
	/**
	 * Visit a parse tree produced by the {@code load_data}
	 * labeled alternative in {@link SQLParser#io_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoad_data(SQLParser.Load_dataContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dump_data}
	 * labeled alternative in {@link SQLParser#io_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDump_data(SQLParser.Dump_dataContext ctx);
	/**
	 * Visit a parse tree produced by the {@code create_table}
	 * labeled alternative in {@link SQLParser#table_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_table(SQLParser.Create_tableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code drop_table}
	 * labeled alternative in {@link SQLParser#table_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_table(SQLParser.Drop_tableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code describe_table}
	 * labeled alternative in {@link SQLParser#table_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescribe_table(SQLParser.Describe_tableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code insert_into_table}
	 * labeled alternative in {@link SQLParser#table_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsert_into_table(SQLParser.Insert_into_tableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code delete_from_table}
	 * labeled alternative in {@link SQLParser#table_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDelete_from_table(SQLParser.Delete_from_tableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code update_table}
	 * labeled alternative in {@link SQLParser#table_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpdate_table(SQLParser.Update_tableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code select_table_}
	 * labeled alternative in {@link SQLParser#table_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_table_(SQLParser.Select_table_Context ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#select_table}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_table(SQLParser.Select_tableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code alter_add_index}
	 * labeled alternative in {@link SQLParser#alter_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_add_index(SQLParser.Alter_add_indexContext ctx);
	/**
	 * Visit a parse tree produced by the {@code alter_drop_index}
	 * labeled alternative in {@link SQLParser#alter_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_drop_index(SQLParser.Alter_drop_indexContext ctx);
	/**
	 * Visit a parse tree produced by the {@code alter_table_drop_pk}
	 * labeled alternative in {@link SQLParser#alter_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_table_drop_pk(SQLParser.Alter_table_drop_pkContext ctx);
	/**
	 * Visit a parse tree produced by the {@code alter_table_drop_foreign_key}
	 * labeled alternative in {@link SQLParser#alter_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_table_drop_foreign_key(SQLParser.Alter_table_drop_foreign_keyContext ctx);
	/**
	 * Visit a parse tree produced by the {@code alter_table_add_pk}
	 * labeled alternative in {@link SQLParser#alter_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_table_add_pk(SQLParser.Alter_table_add_pkContext ctx);
	/**
	 * Visit a parse tree produced by the {@code alter_table_add_foreign_key}
	 * labeled alternative in {@link SQLParser#alter_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_table_add_foreign_key(SQLParser.Alter_table_add_foreign_keyContext ctx);
	/**
	 * Visit a parse tree produced by the {@code alter_table_add_unique}
	 * labeled alternative in {@link SQLParser#alter_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_table_add_unique(SQLParser.Alter_table_add_uniqueContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#field_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitField_list(SQLParser.Field_listContext ctx);
	/**
	 * Visit a parse tree produced by the {@code normal_field}
	 * labeled alternative in {@link SQLParser#field}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNormal_field(SQLParser.Normal_fieldContext ctx);
	/**
	 * Visit a parse tree produced by the {@code primary_key_field}
	 * labeled alternative in {@link SQLParser#field}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimary_key_field(SQLParser.Primary_key_fieldContext ctx);
	/**
	 * Visit a parse tree produced by the {@code foreign_key_field}
	 * labeled alternative in {@link SQLParser#field}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForeign_key_field(SQLParser.Foreign_key_fieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#type_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType_(SQLParser.Type_Context ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#value_lists}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue_lists(SQLParser.Value_listsContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#value_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue_list(SQLParser.Value_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue(SQLParser.ValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#where_and_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhere_and_clause(SQLParser.Where_and_clauseContext ctx);
	/**
	 * Visit a parse tree produced by the {@code where_operator_expression}
	 * labeled alternative in {@link SQLParser#where_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhere_operator_expression(SQLParser.Where_operator_expressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code where_operator_select}
	 * labeled alternative in {@link SQLParser#where_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhere_operator_select(SQLParser.Where_operator_selectContext ctx);
	/**
	 * Visit a parse tree produced by the {@code where_null}
	 * labeled alternative in {@link SQLParser#where_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhere_null(SQLParser.Where_nullContext ctx);
	/**
	 * Visit a parse tree produced by the {@code where_in_list}
	 * labeled alternative in {@link SQLParser#where_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhere_in_list(SQLParser.Where_in_listContext ctx);
	/**
	 * Visit a parse tree produced by the {@code where_in_select}
	 * labeled alternative in {@link SQLParser#where_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhere_in_select(SQLParser.Where_in_selectContext ctx);
	/**
	 * Visit a parse tree produced by the {@code where_like_string}
	 * labeled alternative in {@link SQLParser#where_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhere_like_string(SQLParser.Where_like_stringContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#column}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn(SQLParser.ColumnContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(SQLParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#set_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_clause(SQLParser.Set_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#selectors}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectors(SQLParser.SelectorsContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#selector}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelector(SQLParser.SelectorContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#identifiers}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifiers(SQLParser.IdentifiersContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#operator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperator(SQLParser.OperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#aggregator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregator(SQLParser.AggregatorContext ctx);
}