package com.pgdiff.lib.model.Comment;

import com.pgdiff.lib.model.Alter;
import com.pgdiff.lib.model.CompareInterface;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

// Таблицы, последовательности, индексы, представления
@Getter
@Setter
public class TableComment extends CompareInterface<TableComment> {

    String compareName;
    String tableName;
    String typ;
    String description;


    public boolean compare(TableComment tableComment) { return this.getCompareName().equals(tableComment.getCompareName()); }

    public String getCreate() { return null; }

    public Alter getAdd(String destinationSchema) {
        return new Alter( 5,
                String.format("COMMENT ON %s %s.%s IS ''%s''", this.getTyp(), destinationSchema, this.getTableName(), this.getDescription()));
    }

    public Alter getDrop(String destinationSchema) {
        return new Alter( 5,
                String.format("COMMENT ON %s %s.%s IS null", this.getTyp(), destinationSchema, this.getTableName()));
    }

    public List<Alter> getChange(TableComment ci) { return new ArrayList<>(); }
}
