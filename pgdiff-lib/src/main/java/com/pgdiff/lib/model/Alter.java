package com.pgdiff.lib.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Alter {

    AlterType alterType;
    String alter;
    String subAlter;
    String objectName;

    public Alter(){}
    public Alter(AlterType alterType, String objectName, String alter){
        this.alterType = alterType;
        this.objectName = objectName;
        this.alter = alter;
    }
    public Alter(AlterType alterType, String objectName, String alter, String subAlter){
        this.alterType = alterType;
        this.objectName = objectName;
        this.alter = alter;
        this.subAlter = subAlter;
    }


}
