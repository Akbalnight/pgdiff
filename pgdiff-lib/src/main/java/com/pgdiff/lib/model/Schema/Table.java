package com.pgdiff.lib.model.Schema;

import com.pgdiff.lib.model.Alter;
import com.pgdiff.lib.model.AlterType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Log4j2
@NoArgsConstructor
public class Table extends CommonSchema<Table> {

    String tableSchema;
    String compareName;
    String tableName;
    String tableType;
    String viewSelect;
    List<Column> columns = new ArrayList<>();
    List<Constraint> constraints = new ArrayList<>();

    public boolean compare(Table Table) {
        return this.getCompareName().equals(Table.getCompareName());
    }

    public String getDDL(String destinationSchema){
        String sql = "";

        if (this.getTableType().equals("TABLE")) {
            sql = String.format("CREATE %s %s.%s (\n\t", this.getTableType(), destinationSchema, this.getTableName());

            List<String> columnSql = new ArrayList();

            for (Column column : this.getColumns()) {
                columnSql.add(column.getCreate());
            }
            for (Constraint constraint : this.getConstraints()) {
                columnSql.add(constraint.getCreate());
            }
            sql += String.join(",\n\t", columnSql);
            sql += "\n);";
        }else if (this.getTableType().equals("VIEW")){
            sql = String.format("%s\n", getDrop(destinationSchema));
            sql += String.format("CREATE OR REPLACE %s %s.%s\n%s\n", this.getTableType(), destinationSchema, this.getTableName(), this.getViewSelect());
        }
//        log.info(sql);
        return sql;
    }

    public Alter getCreate(String destinationSchema) { return new Alter(AlterType.DDL_TABLE, getDDL(destinationSchema)); }

    public Alter getAdd(String destinationSchema) { return new Alter(AlterType.ADD_TABLE, getDDL(destinationSchema)); }

    public Alter getDrop(String destinationSchema){
        return new Alter(AlterType.DROP_TABLE, String.format("DROP %s %s.%s;", this.getTableType(), destinationSchema, this.getTableName()));
    }
}
