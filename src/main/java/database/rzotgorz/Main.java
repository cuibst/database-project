package database.rzotgorz;

import database.rzotgorz.managesystem.DatabaseController;
import database.rzotgorz.managesystem.SQLTreeVisitor;
import database.rzotgorz.managesystem.results.MessageResult;
import database.rzotgorz.managesystem.results.ResultItem;
import database.rzotgorz.parser.SQLLexer;
import database.rzotgorz.parser.SQLParser;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.jline.builtins.Completers;
import org.jline.reader.Completer;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.AggregateCompleter;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.*;
import java.nio.file.NotDirectoryException;
import java.util.ArrayList;
import java.util.Scanner;

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

    public static void main(String[] args) throws IOException {
        InputStream in = System.in;
        PrintStream out = System.out;

        if (args.length != 0) {
            if (args.length == 1) {
                in = new FileInputStream(args[0]);
            } else {
                in = new FileInputStream(args[0]);
                out = new PrintStream(new FileOutputStream(args[1]));
            }
        }

        SQLTreeVisitor visitor = new SQLTreeVisitor();
        DatabaseController controller;
        try {
            controller = new DatabaseController(visitor);
        } catch (NotDirectoryException e) {
            log.error(e.getMessage());
            return;
        }

        Terminal terminal;
        if (in.equals(System.in))
            terminal = TerminalBuilder.builder().system(true).build();
        else {
            Scanner scanner = new Scanner(in);
            StringBuilder builder = new StringBuilder();
            while (scanner.hasNextLine()) {
                String currentLine = scanner.nextLine();
                builder.append(currentLine).append(" ");
                if (currentLine.stripTrailing().endsWith(";")) {
                    Object r = controller.execute(builder.toString());
                    printResults(r, out);
                    builder = new StringBuilder();
                }
            }
            out.close();
            controller.shutdown();
            return;
        }

        Completer createCompleter = new ArgumentCompleter(
                new StringsCompleter("CREATE"),
                new StringsCompleter("DATABASE", "TABLE"),
                NullCompleter.INSTANCE
        );
        Completer dropCompleter = new ArgumentCompleter(
                new StringsCompleter("DROP"),
                new StringsCompleter("DATABASE", "TABLE"),
                NullCompleter.INSTANCE
        );
        Completer showCompleter = new ArgumentCompleter(
                new StringsCompleter("SHOW"),
                new StringsCompleter("TABLES", "DATABASES", "INDEXES"),
                NullCompleter.INSTANCE
        );
        Completer multiCompleter = new ArgumentCompleter(
                new StringsCompleter("DESC", "USE"),
                NullCompleter.INSTANCE
        );
        Completer alterCompleter = new ArgumentCompleter(
                new StringsCompleter("ALTER"),
                new StringsCompleter("TABLE"),
                Completers.AnyCompleter.INSTANCE,
                new StringsCompleter("ADD", "DROP"),
                new StringsCompleter("INDEX", "PRIMARY KEY", "FOREIGN KEY", "CONSTRAINT", "UNIQUE")
        );
        Completer insertCompleter = new ArgumentCompleter(
                new StringsCompleter("INSERT"),
                new StringsCompleter("INTO"),
                Completers.AnyCompleter.INSTANCE,
                new StringsCompleter("VALUES"),
                NullCompleter.INSTANCE
        );
        Completer selectCompleter = new ArgumentCompleter(
                new StringsCompleter("SELECT"),
                Completers.AnyCompleter.INSTANCE,
                new StringsCompleter("FROM"),
                Completers.AnyCompleter.INSTANCE,
                new StringsCompleter("WHERE", "GROUP BY", "LIMIT", "OFFSET")
        );
        Completer deleteCompleter = new ArgumentCompleter(
                new StringsCompleter("DELETE"),
                new StringsCompleter("FROM"),
                Completers.AnyCompleter.INSTANCE,
                new StringsCompleter("WHERE"),
                NullCompleter.INSTANCE
        );
        Completer updateCompleter = new ArgumentCompleter(
                new StringsCompleter("UPDATE"),
                Completers.AnyCompleter.INSTANCE,
                new StringsCompleter("SET"),
                Completers.AnyCompleter.INSTANCE,
                new StringsCompleter("WHERE"),
                NullCompleter.INSTANCE
        );
        Completer loadCompleter = new ArgumentCompleter(
                new StringsCompleter("LOAD FROM FILE"),
                Completers.AnyCompleter.INSTANCE,
                new StringsCompleter("TO TABLE"),
                NullCompleter.INSTANCE
        );
        Completer dumpCompleter = new ArgumentCompleter(
                new StringsCompleter("DUMP TO FILE"),
                Completers.AnyCompleter.INSTANCE,
                new StringsCompleter("FROM TABLE"),
                NullCompleter.INSTANCE
        );
        Completer aggregateCompleter = new AggregateCompleter(
                createCompleter,
                deleteCompleter,
                dumpCompleter,
                dropCompleter,
                loadCompleter,
                updateCompleter,
                showCompleter,
                alterCompleter,
                insertCompleter,
                selectCompleter,
                multiCompleter
        );


        LineReader lineReader = LineReaderBuilder.builder().terminal(terminal).completer(aggregateCompleter).build();
        StringBuilder line = new StringBuilder();
        while (true) {
            String prompt = controller.getCurrentUsingDatabase() == null ? "llhdb> " : String.format("llhdb(%s)> ", controller.getCurrentUsingDatabase());
            if (!line.toString().equals(""))
                prompt = " ".repeat(prompt.length() - 3) + "-> ";
            String currentLine = "";
            try {
                currentLine = lineReader.readLine(prompt);
            } catch (EndOfFileException e) {
                if (!line.toString().equals("")) {
                    line.append(currentLine);
                }
                if (currentLine.stripTrailing().endsWith(";")) {
                    Object r = controller.execute(line.toString());
                    printResults(r, out);
                }
                break;
            }
            line.append(currentLine);
            if (line.toString().equals("exit")) {
                System.out.println("(。・_・)/~~~ bye!");
                break;
            }
            if (currentLine.stripTrailing().endsWith(";")) {
                Object e = controller.execute(line.toString());
                printResults(e, out);
                line = new StringBuilder();
            }
        }
        controller.shutdown();
    }

    private static void printResults(Object e, PrintStream out) {
        if (e != null) {
            if (e.getClass() == MessageResult.class) {
                out.println(e);
                out.println("Result received in " + ((ResultItem) e).cost + "ms.");
                out.println("=".repeat(30));
            } else {
                boolean flag = false;
                assert e.getClass() == ArrayList.class;
                for (ResultItem resultItem : (ArrayList<ResultItem>) e) {
                    if (!flag)
                        flag = true;
                    else
                        out.println();
                    out.println(resultItem);
//                    if (out.equals(System.out))
                    out.println("Result received in " + resultItem.cost + "ms.");
                }
                if (!out.equals(System.out))
                    out.println();
            }
        }
    }
}
