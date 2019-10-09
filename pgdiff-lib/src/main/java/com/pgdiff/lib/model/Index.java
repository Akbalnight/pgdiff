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
public class Index extends CompareInterface<Index>{
    String compareName;
    String schemaName;
    String tableName;
    String indexName;
    Boolean pk;
    Boolean uq;
    String indexDef;
    String constraintDef;
    String typ;

    public boolean compare(Index index) {
        if(this.getTableName().length() == 0 || this.getIndexName().length() == 0){
            log.info("-- Comparing (table_name and/or index_name is empty): %s / %s", this.getTableName(), this.getIndexName());
            return false;
        }
        return this.getCompareName().equals(index.getCompareName());
    }

    private String getAddSql() {
        return String.format("CONSTRAINT %s %s", this.getIndexName(), this.getConstraintDef());
    }
    public String getCreate() { return getAddSql(); }

    public String getAdd(String destinationSchema) {

        String sql = "";

        // Assertion
        if (this.getIndexDef() == null || this.getIndexDef().length() == 0){
            log.info(String.format("-- Add Unexpected situation in index.go: there is no index_def for %s.%s %s", destinationSchema, this.getTableName(), this.getIndexName()));
            return "";
        }

        // If we are comparing two different schemas against each other, we need to do some
        // modification of the first index_def so we create the index in the write schema
//        String indexDef = this.getIndexDef();
//        if (this.getSchemaName() != destinationSchema) {
//            indexDef = indexDef.replace(
//                    String.format(" %s.%s ", this.getSchemaName(), this.getTableName()),
//                    String.format(" %s.%s ", destinationSchema, this.getTableName()));
//        }
//
//        sql += String.format("%s;\n", indexDef);

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
//        sql += String.format("DROP INDEX %s.%s;", this.getSchemaName(), this.getIndexName());
//        log.info(sql);
        return sql;
    }

    public List<String> getChange(Index index) {
        List<String> sql = new ArrayList<>();
        // Table and constraint name matches... We need to make sure the details match

        // NOTE that there should always be an index_def for both c and c2 (but we're checking below anyway)
        if (this.getIndexDef().length() == 0) {
            log.info("-- Change: Unexpected situation in index.go: index_def is empty for 1");
            return sql;
        }
        if (index.getIndexDef().length() == 0) {
            log.info("-- Change: Unexpected situation in index.go: index_def is empty for 2");
            return sql;
        }


        if (this.getConstraintDef() == null && index.getConstraintDef() != null) {
            // c1.constraint does not exist, c2.constraint does, so
            // Drop constraint
            sql.add(getDrop(index.getSchemaName()));

        } else if (this.getConstraintDef() != null && index.getConstraintDef() == null) {
            // c1.constraint exists, c2.constraint does not, so
            sql.add(getAdd(index.getSchemaName()));

        } else if ( (this.getConstraintDef() == null && index.getConstraintDef() == null)
                || (this.getConstraintDef() != null && index.getConstraintDef() != null) ){

            // At this point, we know that the constraint_def matches. CompareInterface the index_def
            String indexDef1 = this.getIndexDef();
            String indexDef2 = index.getIndexDef();

            // If we are comparing two different schemas against each other, we need to do
            // some modification of the first index_def so it looks more like the second
            if (!this.getSchemaName().equals(index.getSchemaName())) {
                indexDef1 = indexDef1.replace(
                        String.format(" %s.%s ", this.getSchemaName(), this.getTableName()),
                        String.format(" %s.%s ", index.getSchemaName(), index.getTableName()));
            }

            if (!indexDef1.equals(indexDef2)) {
                // Notice that, if we are here, then the two constraint_defs match (both may be empty)
                // The indexes do not match, but the constraints do
                if (!this.getIndexDef().startsWith(index.getIndexDef())
                        && !index.getIndexDef().startsWith(this.getIndexDef())){
                    // sql += String.format("-- CHANGE: index defs are different for identical constraint defs:\n-- %s\n-- %s\n", this.getIndexDef(), index.getIndexDef());

                    // Drop the index (and maybe the constraint) so we can recreate the index
                    // Recreate the index (and a constraint if specified)
                    sql.add(getDrop(index.getSchemaName()));
                    sql.add(getAdd(index.getSchemaName()));
                }
            }
        }
//        log.info(sql);
        return sql;
    }
}
