package com.pgdiff.lib.model.Sql;

import com.pgdiff.lib.model.Schema.Column;
import com.pgdiff.lib.model.Schema.Constraint;
import com.pgdiff.lib.model.Schema.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommonSql {

    public static String setPartitionFilterForInfoSchema(String SQL, Boolean withPartitions){
        return withPartitions ?
                String.format(SQL, "") :
                String.format(SQL, "and table_name not in (select relname from pg_inherits i join pg_class c on c.oid = inhrelid where relname = table_name)");
    }
    public static String setPartitionFiltersForPgClass(String SQL, Boolean withPartitions){
        return withPartitions ?
                String.format(SQL, "") :
                String.format(SQL, "and c.relname not in (select relname from pg_inherits a join pg_class b on b.oid = a.inhrelid where relname = c.relname)");
    }


    public static List<Table> mappingTableFields(List<Table> tables, Map<String, List<Column>> columns, Map<String, List<Constraint>> constraints){
        for(Table table : tables){
            table.setColumns(
                    columns.get(table.getTableName()) != null ?
                            columns.get(table.getTableName()) :
                            new ArrayList<>()
            );
            table.setConstraints(
                    constraints.get(table.getTableName()) != null ?
                            constraints.get(table.getTableName()) :
                            new ArrayList<>()
            );
        }
        return tables;
    }
}
