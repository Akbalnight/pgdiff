package com.pgdiff.lib.model;

public class SqlSchema {

    public static String setPartitionFilterForInfoSchema(String SQL, Boolean withPartitions){
        return withPartitions ?
                String.format(SQL, "") :
                String.format(SQL, "and table_name not in (select relname from pg_inherits i join pg_class c on c.oid = inhrelid where relname = table_name)");
    }
    public static String setPartitionFiltersForPgClass(String SQL, Boolean withPartitions){
        return withPartitions ?
                String.format(SQL, "") :
                String.format(SQL, "and c.relname not in (select relname from pg_inherits a join pg_class b on b.oid = a.inhrelid where relname = c.relname)");
    }


    public final static String SqlTableSchema = "" +
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

//            "SELECT table_schema \n" +
//            "    , table_name AS compare_name\n" +
//            "    , table_name\n" +
//            "    , CASE table_type \n" +
//            "      WHEN 'BASE TABLE' THEN 'TABLE' \n" +
//            "      ELSE table_type END AS table_type\n" +
//            "    , is_insertable_into\n" +
//            "FROM information_schema.tables \n" +
//            "WHERE table_type = 'BASE TABLE'\n" +
//            "AND table_schema = :schema\n" +
//            "%s"+
//            "ORDER BY compare_name;";

    public final static String SqlColumnSchema = "" +
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
//            "" +
//            "SELECT table_schema\n"+
//            "    , table_name || '.' || column_name  AS compare_name\n"+
//            "    , table_name\n"+
//            "    , column_name\n"+
//            "    , udt_name as data_type\n"+
//            "    , is_nullable\n"+
//            "    , column_default\n"+
//            "    , character_maximum_length\n"+
//            "    , is_identity\n"+
//            "    , identity_generation\n"+
//            "FROM information_schema.columns \n"+
//            "WHERE is_updatable = 'YES'\n"+
//            "AND table_schema = :schema\n" +
//            "%s"+
//            "ORDER BY table_name, ordinal_position;"
//        ;

    public final static String SqlConstraintSchema = "" +
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

//        "SELECT c.relname || '.' || c2.relname AS compare_name\n"+
//            "    , n.nspname AS schema_name\n"+
//            "    , c.relname AS table_name\n"+
//            "    , c2.relname AS index_name\n"+
//            "    , i.indisprimary AS pk\n"+
//            "    , i.indisunique AS uq\n"+
//            "    , pg_catalog.pg_get_indexdef(i.indexrelid, 0, true) AS index_def\n"+
//            "    , pg_catalog.pg_get_constraintdef(con.oid, true) AS constraint_def\n"+
//            "    , con.contype AS typ\n"+
//            "FROM pg_catalog.pg_index AS i\n"+
//            "INNER JOIN pg_catalog.pg_class AS c ON (c.oid = i.indrelid)\n"+
//            "INNER JOIN pg_catalog.pg_class AS c2 ON (c2.oid = i.indexrelid)\n"+
//            "LEFT OUTER JOIN pg_catalog.pg_constraint con\n"+
//            "    ON (con.conrelid = i.indrelid AND con.conindid = i.indexrelid AND con.contype IN ('p','u','x'))\n"+
//            "INNER JOIN pg_catalog.pg_namespace AS n ON (c2.relnamespace = n.oid)\n"+
//            "WHERE true\n"+
//            "AND n.nspname = :schema\n"+
//            "%s "+
//            "ORDER BY c.relname, con.contype;";

    public final static String SqlTableComment = "" +
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
