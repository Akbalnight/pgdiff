package com.pgdiff.lib.model.Sql;

public class ColumnSql {

    public final static String COLUMN_SQL = "" +
            "select\n" +
            "    d.nspname as table_schema,\n" +
            "    c.relname || '.' || a.attname AS compare_name,\n" +
            "    c.relname as table_name,\n" +
            "    a.attname as column_name,\n" +
            "    format_type(a.atttypid, a.atttypmod) as data_type,\n" +
            "    case when a.attnotnull then 'NOT NULL' else 'NULL' end as is_nullable,\n" +
            "    case when a.atthasdef then (select adsrc from pg_attrdef ad where ad.adrelid = a.attrelid and ad.adnum = a.attnum) end as column_default\n" +
            "from pg_class c\n" +
            "join pg_attribute a on c.oid = a.attrelid\n" +
            "join pg_namespace d ON d.oid = c.relnamespace\n" +
            "where 1=1 \n" +
            "and d.nspname = :schema\n" +
            "and c.relkind in ('r', 'v') \n" +
            "and a.attnum > 0 and not a.attisdropped\n" +
            "%s";
}
