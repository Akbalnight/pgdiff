package com.pgdiff.lib.model.Schema;

import com.pgdiff.lib.model.Alter;
import com.pgdiff.lib.model.AlterType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Column extends CommonSchema<Column> {

    String tableSchema;
    String compareName;
    String tableName;
    String columnName;
    String dataType;
    String isNullable;
    String columnDefault;

    public boolean compare(Column column) {
        return this.getCompareName().equals(column.getCompareName());
    }

    private String getAddSql() {

        String sql = "";
        sql += String.format("%s %s", this.getColumnName(), this.getDataType());

        if (this.getIsNullable().equals("NO"))
            sql += " NOT NULL";
        else
            sql += " NULL";

        if (this.getColumnDefault() != null)
            sql += String.format(" DEFAULT %s", this.getColumnDefault());

        return sql;
    }

    public String getCreate() {
        return getAddSql();
    }

    public Alter getAdd(String schema) {
        return new Alter(AlterType.ADD_COLUMN, this.getTableName(), String.format("ALTER TABLE %s.%s ADD COLUMN %s", schema, this.getTableName(), getAddSql()));
    }

    public Alter getDrop(String destinationSchema) {
        return new Alter(AlterType.DROP_COLUMN, this.getTableName(), String.format("ALTER TABLE %s.%s DROP COLUMN IF EXISTS %s;", destinationSchema, this.getTableName(), this.getColumnName()));
    }

    public List<Alter> getChange(Column column) {
//        String sql = "";
        List<Alter> sql = new ArrayList<>();

        // Code and test a column change from integer to bigint
        if (!this.getDataType().equals(column.getDataType())) {
            sql.add(
                new Alter( AlterType.CHANGE_COLUMN, this.getTableName(),
                    String.format("-- WARNING: Это изменение типа может не сработать: (%s to %s).", column.getDataType(), this.getDataType())));
            sql.add(
                new Alter( AlterType.CHANGE_COLUMN, this.getTableName(),
                    String.format("ALTER TABLE %s.%s ALTER COLUMN %s TYPE %s;", column.getTableSchema(), this.getTableName(), this.getColumnName(), this.getDataType())));
        }

        // Detect column default change (or added, dropped)
        if (this.getColumnDefault() == null) {
            if (column.getColumnDefault() != null) {
                sql.add(
                    new Alter( AlterType.CHANGE_COLUMN, this.getTableName(),
                        String.format("ALTER TABLE %s.%s ALTER COLUMN %s DROP DEFAULT;", column.getTableSchema(), this.getTableName(), this.getColumnName())));
            }
        } else if (!this.getColumnDefault().equals(column.getColumnDefault())) {
            sql.add(
                new Alter( AlterType.CHANGE_COLUMN, this.getTableName(),
                    String.format("ALTER TABLE %s.%s ALTER COLUMN %s SET DEFAULT %s;", column.getTableSchema(), this.getTableName(), this.getColumnName(), this.getColumnDefault())));
        }


        // Detect not-null and nullable change
        if (!this.getIsNullable().equals(column.getIsNullable())) {
            if (this.getIsNullable().equals("NULL")) {
                sql.add(
                    new Alter( AlterType.CHANGE_COLUMN, this.getTableName(),
                        String.format("ALTER TABLE %s.%s ALTER COLUMN %s DROP NOT NULL;", column.getTableSchema(), this.getTableName(), this.getColumnName())));
            } else {
                sql.add(
                    new Alter( AlterType.CHANGE_COLUMN, this.getTableName(),
                        String.format("ALTER TABLE %s.%s ALTER COLUMN %s SET NOT NULL;", column.getTableSchema(), this.getTableName(), this.getColumnName())));
            }
        } else {
        }
        return sql;
    }
}
