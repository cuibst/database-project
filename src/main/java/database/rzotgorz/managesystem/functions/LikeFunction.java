package database.rzotgorz.managesystem.functions;

import java.util.List;
import java.util.regex.Pattern;

public class LikeFunction implements Function {

    private final String pattern;
    private final int index;

    public LikeFunction(String pattern, int index) {
        this.index = index;
        String regex = pattern.replace("%%", "\r").replace("%?", "\n").replace("%_", "\0");
        regex = regex.replace("%", ".*").replace("\\?", ".").replace("_", ".");
        this.pattern = regex.replace("\r", "%").replace("\n", "\\?").replace("\0", "_");
    }

    @Override
    public boolean consume(List<Object> record) {
        if(record.get(index) == null)
            return false;
        return Pattern.matches(pattern, record.get(index).toString());
    }
}
