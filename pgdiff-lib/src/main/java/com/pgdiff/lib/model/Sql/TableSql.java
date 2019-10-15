package com.pgdiff.lib.model.Sql;

public class TableSql {

    public final static String TABLE_SQL = "" +
            "with recursive t1 as (\n" +
            "    select \n" +
            "            d.nspname as table_schema,\n" +
            "            e.typname AS compare_name,\n" +
            "            c.relname as table_name,\n" +
            "            CASE c.relkind \n" +
            "                WHEN 'r' THEN 'TABLE'\n" +
            "                WHEN 'v' THEN 'VIEW'\n" +
            "            END AS table_type,\n" +
            "            CASE c.relkind \n" +
            "                WHEN 'r' THEN null\n" +
            "                WHEN 'v' THEN pg_get_viewdef(c.oid)\n" +
            "            END AS view_select" +
            "        from pg_class c\n" +
            "        join pg_namespace d ON d.oid = c.relnamespace\n" +
            "        join pg_type e on e.oid = c.reltype\n" +
            "        where 1=1\n" +
            "        and d.nspname = :schema\n" +
            "        and c.relkind in ('r', 'v')\n" +
            "        %s\n" +
            "),\n" +
            "t2 as (\n" +
            "    select  replace(conrelid::regclass::text, concat(quote_ident(:schema), '.'), '')::name  as name1, \n" +
            "        replace(confrelid::regclass::text, concat(quote_ident(:schema), '.'), '')::name as name2 \n" +
            "        from pg_constraint\n" +
            "        where contype = 'f' and connamespace = :schema::regnamespace::oid\n" +
            "),\n" +
            "t3 as (\n" +
            "    select t1.table_name, t2.name2, count(t2.name2) over (partition by t1.table_name) as cnt from t1\n" +
            "    left join t2 on t2.name1 = t1.table_name\n" +
            "),\n" +
            "t4 as (\n" +
            "    select t3.table_name, '{}'::name[] as path, 0 as level from t3 where t3.cnt = 0\n" +
            "    union \n" +
            "    select t3.table_name, array_append(t4.path, t4.table_name) as path, t4.level + 1 as level from t3 \n" +
            "    join t4 on t3.name2 = t4.table_name\n" +
            "    where not array[t3.name2] <@ t4.path\n" +
            "),\n" +
            "t5 as (\n" +
            "    select t1.table_schema, t1.compare_name, t1.table_name, t1.table_type, t1.view_select, level - coalesce(sign(array_position(path, t4.table_name)), 0) as level from t4, t1\n" +
            "    where t4.table_name = t1.table_name \n" +
            "),\n" +
            "t6 as (\n" +
            "    select t5.table_schema, t5.compare_name, t5.table_name, t5.table_type, t5.view_select, level, max(level) over (partition by t5.table_name) as level2 from t5\n" +
            ") \n" +
            "select distinct table_schema, compare_name, table_name, table_type, view_select, level from t6 where level = level2 order by level";
}
