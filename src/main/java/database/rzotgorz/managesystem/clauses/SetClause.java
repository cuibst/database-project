package database.rzotgorz.managesystem.clauses;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class SetClause {
    private String columnName;
    private String value;
}
