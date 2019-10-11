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
public class Alter {

    /**
     * 0 - drop constraint
     * 1 - drop column
     * 2 - change column
     * 3 - add column
     * 4 - add constraint
     * */
    Integer alterType;
    String alter;

}
