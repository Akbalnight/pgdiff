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
    String isInsertableInto;
    List<Column> columns = new ArrayList<>();
    List<Index> indexs = new ArrayList<>();
    List<ForeignKey> foreignKeys;

    public Boolean equals(Table table){
        return this.getCompareName().equals(table.getCompareName());
    }

    public String getDDL(String destinationSchema){
        String sql = String.format("CREATE %s %s.%s (\n\t", this.getTableType(), destinationSchema, this.getTableName());

        List<String> columnSql = new ArrayList();

        for(Column column : columns){
                columnSql.add(column.getCreate());
        }
        for(Index index : indexs){
            columnSql.add(index.getCreate());
        }
        for(ForeignKey foreignKey : foreignKeys){
            columnSql.add(foreignKey.getCreate());
        }
        sql += String.join(",\n\t", columnSql);
        sql += "\n);";
//        log.info(sql);
        return sql;
    }

    public String getDrop(String destinationSchema){
        String sql = String.format("DROP %s %s.%s;", this.getTableType(), destinationSchema, this.getTableName());
//        log.info(sql);
        return sql;
    }
}
