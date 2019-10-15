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
public class Constraint extends CommonSchema<Constraint> {
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

    public Alter getAdd(String destinationSchema) {
        return this.getConstraintDef() != null ?
                new Alter(AlterType.ADD_CONSTRAINT, String.format("ALTER TABLE %s.%s ADD %s;", destinationSchema, this.getTableName(), getAddSql())) :
                null;
    }

    public Alter getDrop(String destinationSchema) {
        return this.getConstraintDef() != null ?
                new Alter(AlterType.DROP_CONSTRAINT, String.format("ALTER TABLE %s.%s DROP CONSTRAINT %s CASCADE;", destinationSchema, this.getTableName(), this.getIndexName())) :
                null;
    }

    public List<Alter> getChange(Constraint constraint) {
        List<Alter> sql = new ArrayList<>();

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
