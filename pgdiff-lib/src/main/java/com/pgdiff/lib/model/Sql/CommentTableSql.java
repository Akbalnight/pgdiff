package com.pgdiff.lib.model.Sql;

public class CommentTableSql {

    public final static String COMMENT_TABLE_SQL = "" +
            "with \n" +
            "t as ( \n" +
            "    select relname,\n" +
            "        relnamespace, \n" +
            "        case relkind \n" +
            "            when 'r' then 'TABLE'\n" +
            "            when 'f' then 'FOREIGN TABLE'\n" +
            "            when 'i' then 'INDEX'\n" +
            "            when 'S' then 'SEQUENCE'\n" +
            "            when 'v' then 'VIEW'\n" +
            "            when 'm' then 'MATERIALIZED VIEW'\n" +
            "        end relkind,\n" +
            "        obj_description(oid, 'pg_class') as description \n" +
            "    from pg_class\n" +
            "),\n" +
            "t1 as (\n" +
            "    select relnamespace as relnamespace,\n" +
            "        relname as relname,\n" +
            "        relkind as relkind,\n" +
            "        description as description\n" +
            "    from t where relnamespace = :schema::regnamespace::oid\n" +
            ")\n" +
            "select \n" +
            "    c.relname || '.' || c.relkind || '.' || c.description as compare_name,\n" +
            "    c.relname as tableName,\n" +
            "    c.relkind as typ,\n" +
            "    c.description\n" +
            "from t1 c\n" +
            "where 1=1\n" +
            "%s";
}
