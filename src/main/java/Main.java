import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import parser.SQLLexer;
import parser.SQLParser;

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
        String[] testStrings = {
                "CREATE DATABASE student;",
                "ABC 123"
        };

        for(String s : testStrings) {
            log.info("Input: {}", s);
            run(s);
        }
    }
}
