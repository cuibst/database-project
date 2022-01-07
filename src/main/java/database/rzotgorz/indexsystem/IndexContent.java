package database.rzotgorz.indexsystem;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IndexContent implements Comparable {

    List<Comparable> indexList;

    @Override
    public int compareTo(Object o) {
        List<Comparable> list = ((IndexContent) o).indexList;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == null && indexList.get(i) == null)
                continue;
            if (indexList.get(i) == null)
                return -1;
            if (list.get(i) == null)
                return 1;
            if (list.get(i).getClass() == Integer.class || list.get(i).getClass() == Long.class) {
                long val1;
                try {
                    val1 = (Long) list.get(i);
                } catch (ClassCastException e) {
                    val1 = (Integer) list.get(i);
                }
                long val2;
                try {
                    val2 = (Long) indexList.get(i);
                } catch (ClassCastException e) {
                    val2 = (Integer) indexList.get(i);
                }
                if (val1 != val2)
                    return val1 < val2 ? 1 : -1;
                continue;
            }
            log.info(list.toString());
            log.info(indexList.toString());
            int ans = indexList.get(i).compareTo(list.get(i));
            if (ans != 0)
                return ans;
        }
        return 0;
    }
}
