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
public class Constraint extends CompareInterface<Constraint>{
    String compareName;
    String schemaName;
    String tableName;
    String indexName;
    String constraintDef;

    public boolean compare(Constraint constraint) {
        if(this.getTableName().length() == 0 || this.getIndexName().length() == 0){
            log.info("-- Comparing (table_name and/or index_name is empty): %s / %s", this.getTableName(), this.getIndexName());
            return false;
        }
        return this.getCompareName().equals(constraint.getCompareName());
    }

    private String getAddSql() {
        return String.format("CONSTRAINT %s %s", this.getIndexName(), this.getConstraintDef());
    }
    public String getCreate() { return getAddSql(); }

    public String getAdd(String destinationSchema) {

        String sql = "";

        if (this.getConstraintDef() != null) {
            // Create the constraint using the index we just created
            sql += String.format("ALTER TABLE %s.%s ADD %s;", destinationSchema, this.getTableName(), getAddSql());
        }
//        log.info(sql);
        return sql; }

    public String getDrop(String destinationSchema) {
        String sql = "";

        if (this.getConstraintDef() != null) {
//            sql += "-- Warning, this may drop foreign keys pointing at this column. Make sure you re-run the FOREIGN_KEY diff after running this SQL.\n";
            sql += String.format("ALTER TABLE %s.%s DROP CONSTRAINT %s CASCADE; -- %s\n", destinationSchema, this.getTableName(), this.getIndexName(), this.getConstraintDef());
        }
//        log.info(sql);
        return sql;
    }

    public List<String> getChange(Constraint constraint) {
        List<String> sql = new ArrayList<>();

        if (this.getConstraintDef() == null && constraint.getConstraintDef() != null) {
            // c1.constraint does not exist, c2.constraint does, so
            // Drop constraint
            sql.add(getDrop(constraint.getSchemaName()));

        } else if (this.getConstraintDef() != null && constraint.getConstraintDef() == null) {
            // c1.constraint exists, c2.constraint does not, so
            sql.add(getAdd(constraint.getSchemaName()));

        } else if ( !this.getConstraintDef().equals(constraint.getConstraintDef())){
            sql.add(getDrop(constraint.getSchemaName()));
            sql.add(getAdd(constraint.getSchemaName()));
        }
//        log.info(sql);
        return sql;
    }
}
