package com.pgdiff.lib.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Alter {

    AlterType alterType;
    String alter;
    String tableName;

    public Alter(){}
    public Alter(AlterType alterType, String alter){
        this.alterType = alterType;
        this.alter = alter;
    }
    public Alter(AlterType alterType, String tableName, String alter){
        this.alterType = alterType;
        this.tableName = tableName;
        this.alter = alter;
    }


}
