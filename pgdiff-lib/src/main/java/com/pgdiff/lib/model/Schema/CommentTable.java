package com.pgdiff.lib.model.Schema;

import com.pgdiff.lib.model.Alter;
import com.pgdiff.lib.model.AlterType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

// Таблицы, последовательности, индексы, представления
@Getter
@Setter
public class CommentTable extends CommonSchema<CommentTable> {

    String compareName;
    String tableName;
    String typ;
    String description;


    public boolean compare(CommentTable commentTable) { return this.getCompareName().equals(commentTable.getCompareName()); }

    public String getCreate() { return null; }

    public Alter getAdd(String destinationSchema) {
        return new Alter( AlterType.ADD_COMMENTS,
                String.format("COMMENT ON %s %s.%s IS ''%s''", this.getTyp(), destinationSchema, this.getTableName(), this.getDescription()));
    }

    public Alter getDrop(String destinationSchema) {
        return new Alter( AlterType.DROP_COMMENTS,
                String.format("COMMENT ON %s %s.%s IS null", this.getTyp(), destinationSchema, this.getTableName()));
    }

    public List<Alter> getChange(CommentTable ci) { return new ArrayList<>(); }
}
