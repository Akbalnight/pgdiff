package com.pgdiff.lib.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Getter
@Setter
@NoArgsConstructor
public class ForeignKey extends CompareInterface<ForeignKey>{

    String compareName;
    String schemaName;
    String tableName;
    String fkName;
    String constraintDef;

    public boolean compare(ForeignKey foreignKey) {
        Boolean val;
        val = this.getCompareName().equals(foreignKey.getCompareName());
        if(!val) return val;
        else return this.getConstraintDef().equals(foreignKey.getConstraintDef());
    }

    private String getAddSql() {
        return String.format("CONSTRAINT %s %s", this.getFkName(), this.getConstraintDef());
    }

    public String getCreate() { return getAddSql(); }

    public String getAdd(String destinationSchema) {
        return String.format("ALTER TABLE %s.%s ADD %s;", destinationSchema, this.getTableName(), getAddSql());
    }

    public String getDrop(String destinationSchema) {
        return String.format("ALTER TABLE %s.%s DROP CONSTRAINT %s; -- %s", this.getSchemaName(), this.getTableName(), this.getFkName(), this.getConstraintDef());
    }

    public List<String> getChange(ForeignKey foreignKey) { return new ArrayList<>(); }
}
