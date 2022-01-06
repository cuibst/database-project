// Generated from C:/Users/dell/Desktop/dbms-homework/src/main/resources\SQL.g4 by ANTLR 4.9.1
package database.rzotgorz.parser;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class SQLLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.9.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, T__22=23, T__23=24, 
		T__24=25, T__25=26, T__26=27, T__27=28, T__28=29, T__29=30, T__30=31, 
		T__31=32, T__32=33, T__33=34, T__34=35, T__35=36, T__36=37, T__37=38, 
		T__38=39, T__39=40, T__40=41, T__41=42, T__42=43, T__43=44, T__44=45, 
		T__45=46, T__46=47, T__47=48, T__48=49, T__49=50, T__50=51, T__51=52, 
		EqualOrAssign=53, Less=54, LessEqual=55, Greater=56, GreaterEqual=57, 
		NotEqual=58, Count=59, Average=60, Max=61, Min=62, Sum=63, Null=64, Identifier=65, 
		Integer=66, String=67, Float=68, Whitespace=69, Annotation=70;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
			"T__9", "T__10", "T__11", "T__12", "T__13", "T__14", "T__15", "T__16", 
			"T__17", "T__18", "T__19", "T__20", "T__21", "T__22", "T__23", "T__24", 
			"T__25", "T__26", "T__27", "T__28", "T__29", "T__30", "T__31", "T__32", 
			"T__33", "T__34", "T__35", "T__36", "T__37", "T__38", "T__39", "T__40", 
			"T__41", "T__42", "T__43", "T__44", "T__45", "T__46", "T__47", "T__48", 
			"T__49", "T__50", "T__51", "EqualOrAssign", "Less", "LessEqual", "Greater", 
			"GreaterEqual", "NotEqual", "Count", "Average", "Max", "Min", "Sum", 
			"Null", "Identifier", "Integer", "String", "Float", "Whitespace", "Annotation"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "';'", "'CREATE'", "'DATABASE'", "'DROP'", "'SHOW'", "'DATABASES'", 
			"'USE'", "'TABLES'", "'INDEXES'", "'LOAD'", "'FROM'", "'FILE'", "'TO'", 
			"'TABLE'", "'DUMP'", "'('", "')'", "'DESC'", "'INSERT'", "'INTO'", "'VALUES'", 
			"'DELETE'", "'WHERE'", "'UPDATE'", "'SET'", "'SELECT'", "'GROUP'", "'BY'", 
			"'LIMIT'", "'OFFSET'", "'ALTER'", "'ADD'", "'INDEX'", "'PRIMARY'", "'KEY'", 
			"'FOREIGN'", "'CONSTRAINT'", "'REFERENCES'", "'UNIQUE'", "'COLUMN'", 
			"'NOT'", "'DEFAULT'", "','", "'INT'", "'VARCHAR'", "'FLOAT'", "'AND'", 
			"'IS'", "'IN'", "'LIKE'", "'.'", "'*'", "'='", "'<'", "'<='", "'>'", 
			"'>='", "'<>'", "'COUNT'", "'AVG'", "'MAX'", "'MIN'", "'SUM'", "'NULL'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, "EqualOrAssign", "Less", "LessEqual", "Greater", 
			"GreaterEqual", "NotEqual", "Count", "Average", "Max", "Min", "Sum", 
			"Null", "Identifier", "Integer", "String", "Float", "Whitespace", "Annotation"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public SQLLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "SQL.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2H\u020d\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"+
		"\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\4<\t<\4=\t="+
		"\4>\t>\4?\t?\4@\t@\4A\tA\4B\tB\4C\tC\4D\tD\4E\tE\4F\tF\4G\tG\3\2\3\2\3"+
		"\3\3\3\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\5\3\5"+
		"\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3\n\3\n\3\n\3\n"+
		"\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r"+
		"\3\r\3\16\3\16\3\16\3\17\3\17\3\17\3\17\3\17\3\17\3\20\3\20\3\20\3\20"+
		"\3\20\3\21\3\21\3\22\3\22\3\23\3\23\3\23\3\23\3\23\3\24\3\24\3\24\3\24"+
		"\3\24\3\24\3\24\3\25\3\25\3\25\3\25\3\25\3\26\3\26\3\26\3\26\3\26\3\26"+
		"\3\26\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\30\3\30\3\30\3\30\3\30\3\30"+
		"\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\32\3\32\3\32\3\32\3\33\3\33\3\33"+
		"\3\33\3\33\3\33\3\33\3\34\3\34\3\34\3\34\3\34\3\34\3\35\3\35\3\35\3\36"+
		"\3\36\3\36\3\36\3\36\3\36\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3 \3 \3 "+
		"\3 \3 \3 \3!\3!\3!\3!\3\"\3\"\3\"\3\"\3\"\3\"\3#\3#\3#\3#\3#\3#\3#\3#"+
		"\3$\3$\3$\3$\3%\3%\3%\3%\3%\3%\3%\3%\3&\3&\3&\3&\3&\3&\3&\3&\3&\3&\3&"+
		"\3\'\3\'\3\'\3\'\3\'\3\'\3\'\3\'\3\'\3\'\3\'\3(\3(\3(\3(\3(\3(\3(\3)\3"+
		")\3)\3)\3)\3)\3)\3*\3*\3*\3*\3+\3+\3+\3+\3+\3+\3+\3+\3,\3,\3-\3-\3-\3"+
		"-\3.\3.\3.\3.\3.\3.\3.\3.\3/\3/\3/\3/\3/\3/\3\60\3\60\3\60\3\60\3\61\3"+
		"\61\3\61\3\62\3\62\3\62\3\63\3\63\3\63\3\63\3\63\3\64\3\64\3\65\3\65\3"+
		"\66\3\66\3\67\3\67\38\38\38\39\39\3:\3:\3:\3;\3;\3;\3<\3<\3<\3<\3<\3<"+
		"\3=\3=\3=\3=\3>\3>\3>\3>\3?\3?\3?\3?\3@\3@\3@\3@\3A\3A\3A\3A\3A\3B\3B"+
		"\7B\u01de\nB\fB\16B\u01e1\13B\3C\6C\u01e4\nC\rC\16C\u01e5\3D\3D\7D\u01ea"+
		"\nD\fD\16D\u01ed\13D\3D\3D\3E\5E\u01f2\nE\3E\6E\u01f5\nE\rE\16E\u01f6"+
		"\3E\3E\7E\u01fb\nE\fE\16E\u01fe\13E\3F\6F\u0201\nF\rF\16F\u0202\3F\3F"+
		"\3G\3G\3G\6G\u020a\nG\rG\16G\u020b\2\2H\3\3\5\4\7\5\t\6\13\7\r\b\17\t"+
		"\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21!\22#\23%\24\'\25)\26+\27"+
		"-\30/\31\61\32\63\33\65\34\67\359\36;\37= ?!A\"C#E$G%I&K\'M(O)Q*S+U,W"+
		"-Y.[/]\60_\61a\62c\63e\64g\65i\66k\67m8o9q:s;u<w=y>{?}@\177A\u0081B\u0083"+
		"C\u0085D\u0087E\u0089F\u008bG\u008dH\3\2\b\5\2C\\aac|\6\2\62;C\\aac|\3"+
		"\2\62;\3\2))\5\2\13\f\17\17\"\"\3\2==\2\u0214\2\3\3\2\2\2\2\5\3\2\2\2"+
		"\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3"+
		"\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2"+
		"\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2"+
		"\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2"+
		"\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2"+
		"\2\2\2A\3\2\2\2\2C\3\2\2\2\2E\3\2\2\2\2G\3\2\2\2\2I\3\2\2\2\2K\3\2\2\2"+
		"\2M\3\2\2\2\2O\3\2\2\2\2Q\3\2\2\2\2S\3\2\2\2\2U\3\2\2\2\2W\3\2\2\2\2Y"+
		"\3\2\2\2\2[\3\2\2\2\2]\3\2\2\2\2_\3\2\2\2\2a\3\2\2\2\2c\3\2\2\2\2e\3\2"+
		"\2\2\2g\3\2\2\2\2i\3\2\2\2\2k\3\2\2\2\2m\3\2\2\2\2o\3\2\2\2\2q\3\2\2\2"+
		"\2s\3\2\2\2\2u\3\2\2\2\2w\3\2\2\2\2y\3\2\2\2\2{\3\2\2\2\2}\3\2\2\2\2\177"+
		"\3\2\2\2\2\u0081\3\2\2\2\2\u0083\3\2\2\2\2\u0085\3\2\2\2\2\u0087\3\2\2"+
		"\2\2\u0089\3\2\2\2\2\u008b\3\2\2\2\2\u008d\3\2\2\2\3\u008f\3\2\2\2\5\u0091"+
		"\3\2\2\2\7\u0098\3\2\2\2\t\u00a1\3\2\2\2\13\u00a6\3\2\2\2\r\u00ab\3\2"+
		"\2\2\17\u00b5\3\2\2\2\21\u00b9\3\2\2\2\23\u00c0\3\2\2\2\25\u00c8\3\2\2"+
		"\2\27\u00cd\3\2\2\2\31\u00d2\3\2\2\2\33\u00d7\3\2\2\2\35\u00da\3\2\2\2"+
		"\37\u00e0\3\2\2\2!\u00e5\3\2\2\2#\u00e7\3\2\2\2%\u00e9\3\2\2\2\'\u00ee"+
		"\3\2\2\2)\u00f5\3\2\2\2+\u00fa\3\2\2\2-\u0101\3\2\2\2/\u0108\3\2\2\2\61"+
		"\u010e\3\2\2\2\63\u0115\3\2\2\2\65\u0119\3\2\2\2\67\u0120\3\2\2\29\u0126"+
		"\3\2\2\2;\u0129\3\2\2\2=\u012f\3\2\2\2?\u0136\3\2\2\2A\u013c\3\2\2\2C"+
		"\u0140\3\2\2\2E\u0146\3\2\2\2G\u014e\3\2\2\2I\u0152\3\2\2\2K\u015a\3\2"+
		"\2\2M\u0165\3\2\2\2O\u0170\3\2\2\2Q\u0177\3\2\2\2S\u017e\3\2\2\2U\u0182"+
		"\3\2\2\2W\u018a\3\2\2\2Y\u018c\3\2\2\2[\u0190\3\2\2\2]\u0198\3\2\2\2_"+
		"\u019e\3\2\2\2a\u01a2\3\2\2\2c\u01a5\3\2\2\2e\u01a8\3\2\2\2g\u01ad\3\2"+
		"\2\2i\u01af\3\2\2\2k\u01b1\3\2\2\2m\u01b3\3\2\2\2o\u01b5\3\2\2\2q\u01b8"+
		"\3\2\2\2s\u01ba\3\2\2\2u\u01bd\3\2\2\2w\u01c0\3\2\2\2y\u01c6\3\2\2\2{"+
		"\u01ca\3\2\2\2}\u01ce\3\2\2\2\177\u01d2\3\2\2\2\u0081\u01d6\3\2\2\2\u0083"+
		"\u01db\3\2\2\2\u0085\u01e3\3\2\2\2\u0087\u01e7\3\2\2\2\u0089\u01f1\3\2"+
		"\2\2\u008b\u0200\3\2\2\2\u008d\u0206\3\2\2\2\u008f\u0090\7=\2\2\u0090"+
		"\4\3\2\2\2\u0091\u0092\7E\2\2\u0092\u0093\7T\2\2\u0093\u0094\7G\2\2\u0094"+
		"\u0095\7C\2\2\u0095\u0096\7V\2\2\u0096\u0097\7G\2\2\u0097\6\3\2\2\2\u0098"+
		"\u0099\7F\2\2\u0099\u009a\7C\2\2\u009a\u009b\7V\2\2\u009b\u009c\7C\2\2"+
		"\u009c\u009d\7D\2\2\u009d\u009e\7C\2\2\u009e\u009f\7U\2\2\u009f\u00a0"+
		"\7G\2\2\u00a0\b\3\2\2\2\u00a1\u00a2\7F\2\2\u00a2\u00a3\7T\2\2\u00a3\u00a4"+
		"\7Q\2\2\u00a4\u00a5\7R\2\2\u00a5\n\3\2\2\2\u00a6\u00a7\7U\2\2\u00a7\u00a8"+
		"\7J\2\2\u00a8\u00a9\7Q\2\2\u00a9\u00aa\7Y\2\2\u00aa\f\3\2\2\2\u00ab\u00ac"+
		"\7F\2\2\u00ac\u00ad\7C\2\2\u00ad\u00ae\7V\2\2\u00ae\u00af\7C\2\2\u00af"+
		"\u00b0\7D\2\2\u00b0\u00b1\7C\2\2\u00b1\u00b2\7U\2\2\u00b2\u00b3\7G\2\2"+
		"\u00b3\u00b4\7U\2\2\u00b4\16\3\2\2\2\u00b5\u00b6\7W\2\2\u00b6\u00b7\7"+
		"U\2\2\u00b7\u00b8\7G\2\2\u00b8\20\3\2\2\2\u00b9\u00ba\7V\2\2\u00ba\u00bb"+
		"\7C\2\2\u00bb\u00bc\7D\2\2\u00bc\u00bd\7N\2\2\u00bd\u00be\7G\2\2\u00be"+
		"\u00bf\7U\2\2\u00bf\22\3\2\2\2\u00c0\u00c1\7K\2\2\u00c1\u00c2\7P\2\2\u00c2"+
		"\u00c3\7F\2\2\u00c3\u00c4\7G\2\2\u00c4\u00c5\7Z\2\2\u00c5\u00c6\7G\2\2"+
		"\u00c6\u00c7\7U\2\2\u00c7\24\3\2\2\2\u00c8\u00c9\7N\2\2\u00c9\u00ca\7"+
		"Q\2\2\u00ca\u00cb\7C\2\2\u00cb\u00cc\7F\2\2\u00cc\26\3\2\2\2\u00cd\u00ce"+
		"\7H\2\2\u00ce\u00cf\7T\2\2\u00cf\u00d0\7Q\2\2\u00d0\u00d1\7O\2\2\u00d1"+
		"\30\3\2\2\2\u00d2\u00d3\7H\2\2\u00d3\u00d4\7K\2\2\u00d4\u00d5\7N\2\2\u00d5"+
		"\u00d6\7G\2\2\u00d6\32\3\2\2\2\u00d7\u00d8\7V\2\2\u00d8\u00d9\7Q\2\2\u00d9"+
		"\34\3\2\2\2\u00da\u00db\7V\2\2\u00db\u00dc\7C\2\2\u00dc\u00dd\7D\2\2\u00dd"+
		"\u00de\7N\2\2\u00de\u00df\7G\2\2\u00df\36\3\2\2\2\u00e0\u00e1\7F\2\2\u00e1"+
		"\u00e2\7W\2\2\u00e2\u00e3\7O\2\2\u00e3\u00e4\7R\2\2\u00e4 \3\2\2\2\u00e5"+
		"\u00e6\7*\2\2\u00e6\"\3\2\2\2\u00e7\u00e8\7+\2\2\u00e8$\3\2\2\2\u00e9"+
		"\u00ea\7F\2\2\u00ea\u00eb\7G\2\2\u00eb\u00ec\7U\2\2\u00ec\u00ed\7E\2\2"+
		"\u00ed&\3\2\2\2\u00ee\u00ef\7K\2\2\u00ef\u00f0\7P\2\2\u00f0\u00f1\7U\2"+
		"\2\u00f1\u00f2\7G\2\2\u00f2\u00f3\7T\2\2\u00f3\u00f4\7V\2\2\u00f4(\3\2"+
		"\2\2\u00f5\u00f6\7K\2\2\u00f6\u00f7\7P\2\2\u00f7\u00f8\7V\2\2\u00f8\u00f9"+
		"\7Q\2\2\u00f9*\3\2\2\2\u00fa\u00fb\7X\2\2\u00fb\u00fc\7C\2\2\u00fc\u00fd"+
		"\7N\2\2\u00fd\u00fe\7W\2\2\u00fe\u00ff\7G\2\2\u00ff\u0100\7U\2\2\u0100"+
		",\3\2\2\2\u0101\u0102\7F\2\2\u0102\u0103\7G\2\2\u0103\u0104\7N\2\2\u0104"+
		"\u0105\7G\2\2\u0105\u0106\7V\2\2\u0106\u0107\7G\2\2\u0107.\3\2\2\2\u0108"+
		"\u0109\7Y\2\2\u0109\u010a\7J\2\2\u010a\u010b\7G\2\2\u010b\u010c\7T\2\2"+
		"\u010c\u010d\7G\2\2\u010d\60\3\2\2\2\u010e\u010f\7W\2\2\u010f\u0110\7"+
		"R\2\2\u0110\u0111\7F\2\2\u0111\u0112\7C\2\2\u0112\u0113\7V\2\2\u0113\u0114"+
		"\7G\2\2\u0114\62\3\2\2\2\u0115\u0116\7U\2\2\u0116\u0117\7G\2\2\u0117\u0118"+
		"\7V\2\2\u0118\64\3\2\2\2\u0119\u011a\7U\2\2\u011a\u011b\7G\2\2\u011b\u011c"+
		"\7N\2\2\u011c\u011d\7G\2\2\u011d\u011e\7E\2\2\u011e\u011f\7V\2\2\u011f"+
		"\66\3\2\2\2\u0120\u0121\7I\2\2\u0121\u0122\7T\2\2\u0122\u0123\7Q\2\2\u0123"+
		"\u0124\7W\2\2\u0124\u0125\7R\2\2\u01258\3\2\2\2\u0126\u0127\7D\2\2\u0127"+
		"\u0128\7[\2\2\u0128:\3\2\2\2\u0129\u012a\7N\2\2\u012a\u012b\7K\2\2\u012b"+
		"\u012c\7O\2\2\u012c\u012d\7K\2\2\u012d\u012e\7V\2\2\u012e<\3\2\2\2\u012f"+
		"\u0130\7Q\2\2\u0130\u0131\7H\2\2\u0131\u0132\7H\2\2\u0132\u0133\7U\2\2"+
		"\u0133\u0134\7G\2\2\u0134\u0135\7V\2\2\u0135>\3\2\2\2\u0136\u0137\7C\2"+
		"\2\u0137\u0138\7N\2\2\u0138\u0139\7V\2\2\u0139\u013a\7G\2\2\u013a\u013b"+
		"\7T\2\2\u013b@\3\2\2\2\u013c\u013d\7C\2\2\u013d\u013e\7F\2\2\u013e\u013f"+
		"\7F\2\2\u013fB\3\2\2\2\u0140\u0141\7K\2\2\u0141\u0142\7P\2\2\u0142\u0143"+
		"\7F\2\2\u0143\u0144\7G\2\2\u0144\u0145\7Z\2\2\u0145D\3\2\2\2\u0146\u0147"+
		"\7R\2\2\u0147\u0148\7T\2\2\u0148\u0149\7K\2\2\u0149\u014a\7O\2\2\u014a"+
		"\u014b\7C\2\2\u014b\u014c\7T\2\2\u014c\u014d\7[\2\2\u014dF\3\2\2\2\u014e"+
		"\u014f\7M\2\2\u014f\u0150\7G\2\2\u0150\u0151\7[\2\2\u0151H\3\2\2\2\u0152"+
		"\u0153\7H\2\2\u0153\u0154\7Q\2\2\u0154\u0155\7T\2\2\u0155\u0156\7G\2\2"+
		"\u0156\u0157\7K\2\2\u0157\u0158\7I\2\2\u0158\u0159\7P\2\2\u0159J\3\2\2"+
		"\2\u015a\u015b\7E\2\2\u015b\u015c\7Q\2\2\u015c\u015d\7P\2\2\u015d\u015e"+
		"\7U\2\2\u015e\u015f\7V\2\2\u015f\u0160\7T\2\2\u0160\u0161\7C\2\2\u0161"+
		"\u0162\7K\2\2\u0162\u0163\7P\2\2\u0163\u0164\7V\2\2\u0164L\3\2\2\2\u0165"+
		"\u0166\7T\2\2\u0166\u0167\7G\2\2\u0167\u0168\7H\2\2\u0168\u0169\7G\2\2"+
		"\u0169\u016a\7T\2\2\u016a\u016b\7G\2\2\u016b\u016c\7P\2\2\u016c\u016d"+
		"\7E\2\2\u016d\u016e\7G\2\2\u016e\u016f\7U\2\2\u016fN\3\2\2\2\u0170\u0171"+
		"\7W\2\2\u0171\u0172\7P\2\2\u0172\u0173\7K\2\2\u0173\u0174\7S\2\2\u0174"+
		"\u0175\7W\2\2\u0175\u0176\7G\2\2\u0176P\3\2\2\2\u0177\u0178\7E\2\2\u0178"+
		"\u0179\7Q\2\2\u0179\u017a\7N\2\2\u017a\u017b\7W\2\2\u017b\u017c\7O\2\2"+
		"\u017c\u017d\7P\2\2\u017dR\3\2\2\2\u017e\u017f\7P\2\2\u017f\u0180\7Q\2"+
		"\2\u0180\u0181\7V\2\2\u0181T\3\2\2\2\u0182\u0183\7F\2\2\u0183\u0184\7"+
		"G\2\2\u0184\u0185\7H\2\2\u0185\u0186\7C\2\2\u0186\u0187\7W\2\2\u0187\u0188"+
		"\7N\2\2\u0188\u0189\7V\2\2\u0189V\3\2\2\2\u018a\u018b\7.\2\2\u018bX\3"+
		"\2\2\2\u018c\u018d\7K\2\2\u018d\u018e\7P\2\2\u018e\u018f\7V\2\2\u018f"+
		"Z\3\2\2\2\u0190\u0191\7X\2\2\u0191\u0192\7C\2\2\u0192\u0193\7T\2\2\u0193"+
		"\u0194\7E\2\2\u0194\u0195\7J\2\2\u0195\u0196\7C\2\2\u0196\u0197\7T\2\2"+
		"\u0197\\\3\2\2\2\u0198\u0199\7H\2\2\u0199\u019a\7N\2\2\u019a\u019b\7Q"+
		"\2\2\u019b\u019c\7C\2\2\u019c\u019d\7V\2\2\u019d^\3\2\2\2\u019e\u019f"+
		"\7C\2\2\u019f\u01a0\7P\2\2\u01a0\u01a1\7F\2\2\u01a1`\3\2\2\2\u01a2\u01a3"+
		"\7K\2\2\u01a3\u01a4\7U\2\2\u01a4b\3\2\2\2\u01a5\u01a6\7K\2\2\u01a6\u01a7"+
		"\7P\2\2\u01a7d\3\2\2\2\u01a8\u01a9\7N\2\2\u01a9\u01aa\7K\2\2\u01aa\u01ab"+
		"\7M\2\2\u01ab\u01ac\7G\2\2\u01acf\3\2\2\2\u01ad\u01ae\7\60\2\2\u01aeh"+
		"\3\2\2\2\u01af\u01b0\7,\2\2\u01b0j\3\2\2\2\u01b1\u01b2\7?\2\2\u01b2l\3"+
		"\2\2\2\u01b3\u01b4\7>\2\2\u01b4n\3\2\2\2\u01b5\u01b6\7>\2\2\u01b6\u01b7"+
		"\7?\2\2\u01b7p\3\2\2\2\u01b8\u01b9\7@\2\2\u01b9r\3\2\2\2\u01ba\u01bb\7"+
		"@\2\2\u01bb\u01bc\7?\2\2\u01bct\3\2\2\2\u01bd\u01be\7>\2\2\u01be\u01bf"+
		"\7@\2\2\u01bfv\3\2\2\2\u01c0\u01c1\7E\2\2\u01c1\u01c2\7Q\2\2\u01c2\u01c3"+
		"\7W\2\2\u01c3\u01c4\7P\2\2\u01c4\u01c5\7V\2\2\u01c5x\3\2\2\2\u01c6\u01c7"+
		"\7C\2\2\u01c7\u01c8\7X\2\2\u01c8\u01c9\7I\2\2\u01c9z\3\2\2\2\u01ca\u01cb"+
		"\7O\2\2\u01cb\u01cc\7C\2\2\u01cc\u01cd\7Z\2\2\u01cd|\3\2\2\2\u01ce\u01cf"+
		"\7O\2\2\u01cf\u01d0\7K\2\2\u01d0\u01d1\7P\2\2\u01d1~\3\2\2\2\u01d2\u01d3"+
		"\7U\2\2\u01d3\u01d4\7W\2\2\u01d4\u01d5\7O\2\2\u01d5\u0080\3\2\2\2\u01d6"+
		"\u01d7\7P\2\2\u01d7\u01d8\7W\2\2\u01d8\u01d9\7N\2\2\u01d9\u01da\7N\2\2"+
		"\u01da\u0082\3\2\2\2\u01db\u01df\t\2\2\2\u01dc\u01de\t\3\2\2\u01dd\u01dc"+
		"\3\2\2\2\u01de\u01e1\3\2\2\2\u01df\u01dd\3\2\2\2\u01df\u01e0\3\2\2\2\u01e0"+
		"\u0084\3\2\2\2\u01e1\u01df\3\2\2\2\u01e2\u01e4\t\4\2\2\u01e3\u01e2\3\2"+
		"\2\2\u01e4\u01e5\3\2\2\2\u01e5\u01e3\3\2\2\2\u01e5\u01e6\3\2\2\2\u01e6"+
		"\u0086\3\2\2\2\u01e7\u01eb\7)\2\2\u01e8\u01ea\n\5\2\2\u01e9\u01e8\3\2"+
		"\2\2\u01ea\u01ed\3\2\2\2\u01eb\u01e9\3\2\2\2\u01eb\u01ec\3\2\2\2\u01ec"+
		"\u01ee\3\2\2\2\u01ed\u01eb\3\2\2\2\u01ee\u01ef\7)\2\2\u01ef\u0088\3\2"+
		"\2\2\u01f0\u01f2\7/\2\2\u01f1\u01f0\3\2\2\2\u01f1\u01f2\3\2\2\2\u01f2"+
		"\u01f4\3\2\2\2\u01f3\u01f5\t\4\2\2\u01f4\u01f3\3\2\2\2\u01f5\u01f6\3\2"+
		"\2\2\u01f6\u01f4\3\2\2\2\u01f6\u01f7\3\2\2\2\u01f7\u01f8\3\2\2\2\u01f8"+
		"\u01fc\7\60\2\2\u01f9\u01fb\t\4\2\2\u01fa\u01f9\3\2\2\2\u01fb\u01fe\3"+
		"\2\2\2\u01fc\u01fa\3\2\2\2\u01fc\u01fd\3\2\2\2\u01fd\u008a\3\2\2\2\u01fe"+
		"\u01fc\3\2\2\2\u01ff\u0201\t\6\2\2\u0200\u01ff\3\2\2\2\u0201\u0202\3\2"+
		"\2\2\u0202\u0200\3\2\2\2\u0202\u0203\3\2\2\2\u0203\u0204\3\2\2\2\u0204"+
		"\u0205\bF\2\2\u0205\u008c\3\2\2\2\u0206\u0207\7/\2\2\u0207\u0209\7/\2"+
		"\2\u0208\u020a\n\7\2\2\u0209\u0208\3\2\2\2\u020a\u020b\3\2\2\2\u020b\u0209"+
		"\3\2\2\2\u020b\u020c\3\2\2\2\u020c\u008e\3\2\2\2\13\2\u01df\u01e5\u01eb"+
		"\u01f1\u01f6\u01fc\u0202\u020b\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}