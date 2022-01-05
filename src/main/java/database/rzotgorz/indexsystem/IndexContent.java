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
        List<Comparable> list = (List<Comparable>) o;
        for (int i = 0; i < list.size(); i++) {
            int ans = indexList.get(i).compareTo(list.get(i));
            if (ans != 0)
                return ans;
        }
        return 0;
    }
}
