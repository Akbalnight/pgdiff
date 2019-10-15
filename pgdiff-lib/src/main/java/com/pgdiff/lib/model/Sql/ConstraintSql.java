package com.pgdiff.lib.model.Sql;

public class ConstraintSql {

    public final static String CONSTRAINT_SQL = "" +
            "select\n" +
            "    c.relname || '.' || a.conname AS compare_name,\n" +
            "    d.nspname AS schema_name,\n" +
            "    c.relname AS table_name,\n" +
            "    a.conname AS index_name,\n" +
            "    pg_get_constraintdef(a.oid) AS constraint_def\n" +
            "from pg_constraint a\n" +
            "join pg_class c on c.oid = a.conrelid\n" +
            "join pg_namespace d ON d.oid = a.connamespace\n" +
            "where a.contype not in ('t')\n" +
            "and d.nspname = :schema\n" +
            "%s \n" +
            "order by a.conrelid, a.contype = 'f', a.contype = 'x', a.contype = 'c', a.contype = 'u', a.contype = 'p'";
}
