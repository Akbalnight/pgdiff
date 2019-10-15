package com.pgdiff.lib.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AlterType {

    /**
     * Последовательность в pg_dump
     * drop constraint
     * drop trigger
     * drop index
     * drop table
     * drop function
     * drop schema
     *
     * create schema (+ set owner)
     * create function (+ set owner)
     * create table (+ set owner + comments)
     * add constraint
     * create index
     * alter index attach partition
     * create trigger
     * grants
     * */

    DROP_FUNCTION(  0),
    ADD_FUNCTION(   1),
    DROP_SEQUENCE(  2),
    ADD_SEQUENCE(   3),
    DROP_TABLE(     4),
    ADD_TABLE(      5),
    DROP_CONSTRAINT(6),
    DROP_COLUMN(    7),
    CHANGE_COLUMN(  8),
    ADD_COLUMN(     9),
    ADD_CONSTRAINT( 10),
    ADD_INDEX(      11),
    ADD_TRIGGER(    12),
    ADD_COMMENTS(   13),
    ADD_GRANT(      14),
    DROP_GRANT(     15);

    Integer sortCode;
}


