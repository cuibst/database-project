package database.rzotgorz.metaSystem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class NameAndTypePack {
    private String type;
    private String name;
    private int length;
}
