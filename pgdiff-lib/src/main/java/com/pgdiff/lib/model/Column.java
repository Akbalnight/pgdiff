package com.pgdiff.lib.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Column extends CompareInterface<Column>{

    String tableSchema;
    String compareName;
    String tableName;
    String columnName;
    String dataType;
    String isNullable;
    String columnDefault;
    Long characterMaximumLength;
    String isIdentity;
    String identityGeneration;

    public boolean compare(Column column) {
        return this.getCompareName().equals(column.getCompareName());
    }

    private String getAddSql() {

        String sql = "";
        if (this.getDataType().equals("character varying")) {
            Long maxLength = this.getCharacterMaximumLength();
            if (maxLength == null) {
                sql += String.format("%s character varying", this.getColumnName());
            } else {
                sql += String.format("%s character varying(%s)", this.getColumnName(), maxLength);
            }
        } else {
            sql += String.format("%s %s", this.getColumnName(), this.getDataType());
        }

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

    public String getAdd(String schema) {
        return String.format("ALTER TABLE %s.%s ADD COLUMN %s", schema, this.getTableName(), getAddSql());
    }

    public String getDrop(String destinationSchema) {
        return String.format("ALTER TABLE %s.%s DROP COLUMN IF EXISTS %s;", destinationSchema, this.getTableName(), this.getColumnName());
    }

    public List<String> getChange(Column column) {
//        String sql = "";
        List<String> sql = new ArrayList<>();
        // Detect column type change (mostly varchar length, or number size increase)
        // (integer to/from bigint is OK)
        if (this.getDataType().equals(column.getDataType())) {
            if (this.getDataType().equals("character varying")){
                Long max1 = this.getCharacterMaximumLength();
                Long max2 = column.getCharacterMaximumLength();
                if (max1 == null && max2 == null) {
                    // Leave them alone, they both have undefined max lengths
                } else if ((max1 != null || max2 == null) && (!max1.equals(max2))) {
                    if (max1 == null) {
                        sql.add("-- WARNING: столбец varchar не имеет максимальной длины. Установка на 1024.\n");
                        max1 = 1024L;
                    }
                    if (max1 < max2) {
                        sql.add("-- WARNING: Следующее запрос укорачивает столбец, что может привести к потере данных.\n");
                    }
//                    log.info("-- max1Valid: %v  max2Valid: %v \n", max1Valid, max2Valid)
                    sql.add(String.format("ALTER TABLE %s.%s ALTER COLUMN %s TYPE character varying(%s);", column.getTableSchema(), this.getTableName(), this.getColumnName(), max1));
                }
            }
        }

        // Code and test a column change from integer to bigint
        if (!this.getDataType().equals(column.getDataType())) {
            sql.add(String.format("-- WARNING: Это изменение типа может не сработать: (%s to %s).\n", column.getDataType(), this.getDataType()));
            if (this.getDataType().startsWith("character")) {
                Long max1 = this.getCharacterMaximumLength();
                if (max1 == null) {
                    sql.add("-- WARNING: столбец varchar не имеет максимальной длины. Установка на 1024.\n");
                    max1 = 1024L;
                }
                sql.add(String.format("ALTER TABLE %s.%s ALTER COLUMN %s TYPE %s(%s);", column.getTableSchema(), this.getTableName(), this.getColumnName(), this.getDataType(), max1));
            } else {
                sql.add(String.format("ALTER TABLE %s.%s ALTER COLUMN %s TYPE %s;", column.getTableSchema(), this.getTableName(), this.getColumnName(), this.getDataType()));
            }
        }

        // Detect column default change (or added, dropped)
        if (this.getColumnDefault() == null) {
            if (column.getColumnDefault() != null) {
                sql.add(String.format("ALTER TABLE %s.%s ALTER COLUMN %s DROP DEFAULT;", column.getTableSchema(), this.getTableName(), this.getColumnName()));
            }
        } else if (!this.getColumnDefault().equals(column.getColumnDefault())) {
            sql.add(String.format("ALTER TABLE %s.%s ALTER COLUMN %s SET DEFAULT %s;", column.getTableSchema(), this.getTableName(), this.getColumnName(), this.getColumnDefault()));
        }

        // Detect identity column change
        // Save result to variable instead of printing because order for adding/removing
        // is_nullable affects identity columns
        String identitySql = "";
        if (!this.getIsIdentity().equals(column.getIsIdentity())) {
            // Knowing the version of db2 would eliminate the need for this warning
//            log.info("-- WARNING: identity columns are not supported in PostgreSQL versions < 10.");
//            log.info("-- Attempting to create identity columns in earlier versions will probably result in errors.");
            if (this.getIsIdentity().equals("YES")) {
                identitySql = String.format("ALTER TABLE \"%s\".\"%s\" ALTER COLUMN \"%s\" ADD GENERATED %s AS IDENTITY;", column.getTableSchema(), this.getTableName(), this.getColumnName(), this.getIdentityGeneration());
            } else {
                identitySql = String.format("ALTER TABLE \"%s\".\"%s\" ALTER COLUMN \"%s\" DROP IDENTITY;", column.getTableSchema(), this.getTableName(), this.getColumnName());
            }
        }

        // Detect not-null and nullable change
        if (!this.getIsNullable().equals(column.getIsNullable())) {
            if (this.getIsNullable().equals("YES")) {
                if (!identitySql.equals("")) {
                    sql.add(identitySql);
                }
                sql.add(String.format("ALTER TABLE %s.%s ALTER COLUMN %s DROP NOT NULL;", column.getTableSchema(), this.getTableName(), this.getColumnName()));
            } else {
                sql.add(String.format("ALTER TABLE %s.%s ALTER COLUMN %s SET NOT NULL;", column.getTableSchema(), this.getTableName(), this.getColumnName()));
                if (!identitySql.equals("")) {
                    sql.add(identitySql);
                }
            }
        } else {
            if (!identitySql.equals("")) {
                sql.add(identitySql);
            }
        }
        return sql;
    }
}
