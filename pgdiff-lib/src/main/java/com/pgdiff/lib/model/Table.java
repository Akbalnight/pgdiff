package com.pgdiff.lib.model;

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
public class Table {

    String tableSchema;
    String compareName;
    String tableName;
    String tableType;
    String viewSelect;
    List<Column> columns = new ArrayList<>();
    List<Constraint> constraints = new ArrayList<>();

    public Boolean equals(Table table){
        return this.getCompareName().equals(table.getCompareName());
    }

    public String getDDL(String destinationSchema){
        String sql = "";

        if (this.getTableType().equals("TABLE")) {
            sql = String.format("CREATE %s %s.%s (\n\t", this.getTableType(), destinationSchema, this.getTableName());

            List<String> columnSql = new ArrayList();

            for (Column column : columns) {
                columnSql.add(column.getCreate());
            }
            for (Constraint constraint : constraints) {
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

    public String getDrop(String destinationSchema){
        String sql = String.format("DROP %s %s.%s;", this.getTableType(), destinationSchema, this.getTableName());
//        log.info(sql);
        return sql;
    }
}
