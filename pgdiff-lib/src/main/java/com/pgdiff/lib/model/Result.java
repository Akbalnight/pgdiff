package com.pgdiff.lib.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Result{
    String nameTableOne;
    String nameTableTwo;
    String ddlTableOne;
    String ddlTableTwo;
    List<String> alters;
    /**
     * -1 - not action
     * 0 - create table
     * 1 - alters
     * 4 - drop table
     * */
    Integer resultCode;

    public void addAllAlters(List<String> alters){
        this.alters.addAll(alters);
    }
}
