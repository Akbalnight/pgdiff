package com.pgdiff.lib.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AlterType {

    /**
     * --- PG_DUMP ---
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

    /**
     * --- PG_DIFF ---
     * Схемы
     * Роли
     * Последовательности (Sequences)
     * + Таблицы + колонки + ограничения (pk / un / fk)
     * + Вьюхи
     * Функции
     * Триггеры
     * Владельцы
     * Права на отношения
     * Права на атрибутты
     */

    /**
     * --- CLONE_MODEL ---
     * Функции
     * триггерные функции
     * последовательности
     * Таблицы
     *      колонки
     *      ограничения
     * индексы
     * триггеры
     * привилегии
     * comments
     *      -- таблицы, последовательности, индексы, представления
     *      -- столбцы
     *      -- ограничения
     *      -- функции
     *      -- триггеры
     *
     */

    DROP_FUNCTION(  0),
    ADD_FUNCTION(   1),
    DROP_SEQUENCE(  2),
    ADD_SEQUENCE(   3),
    DDL_TABLE(      4),
    DROP_TABLE(     5),
    ADD_TABLE(      6),
    DROP_CONSTRAINT(7),
    DROP_COLUMN(    8),
    CHANGE_COLUMN(  9),
    ADD_COLUMN(     10),
    ADD_CONSTRAINT( 11),
    ADD_INDEX(      12),
    ADD_TRIGGER(    13),
    ADD_COMMENTS(   14),
    DROP_COMMENTS(   14),
    DROP_GRANT(     15),
    ADD_GRANT(      16);


    Integer sortCode;
}


