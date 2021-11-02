package database.rzotgorz.recordsystem;

import java.util.ResourceBundle;
import database.rzotgorz.exceptions.RecordTooLongException;
public class Header {
    private static final ResourceBundle bundle = ResourceBundle.getBundle("configurations");
    private static final int PAGE_SIZE = Integer.parseInt(bundle.getString("PAGE_SIZE"));
    private static final int HEADER_SIZE=Integer.parseInt(bundle.getString("PAGE_HEADER_SIZE"));
    static public int calPageCapacity(int recordLen)
    {
        int availableLen=PAGE_SIZE-HEADER_SIZE;
        int x=(int)Math.ceil(((float)(availableLen<<3)/(1+(recordLen)<<3)))+1;
        while(((x+7)>>3)+x*recordLen>availableLen)
            x-=1;
        if(x<=0)
            throw new RecordTooLongException("Record length is longer than available length");
        return x;
    }
    static public int calBitmapLength(int recordPerPage)
    {
        return (int)Math.ceil(recordPerPage*1.0/8.0);
    }
}
