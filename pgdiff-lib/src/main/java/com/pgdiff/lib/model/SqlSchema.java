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
            "SELECT table_schema \n" +
            "    , table_name AS compare_name\n" +
            "\t, table_name\n" +
            "    , CASE table_type \n" +
            "\t  WHEN 'BASE TABLE' THEN 'TABLE' \n" +
            "\t  ELSE table_type END AS table_type\n" +
            "    , is_insertable_into\n" +
            "FROM information_schema.tables \n" +
            "WHERE table_type = 'BASE TABLE'\n" +
            "AND table_schema = :schema\n" +
            "%s"+
            "ORDER BY compare_name;";

    public final static String SqlColumnSchema = "" +"SELECT table_schema\n"+
            "    , table_name || '.' || column_name  AS compare_name\n"+
            "    , table_name\n"+
            "    , column_name\n"+
            "    , data_type\n"+
            "    , is_nullable\n"+
            "    , column_default\n"+
            "    , character_maximum_length\n"+
            "    , is_identity\n"+
            "    , identity_generation\n"+
            "FROM information_schema.columns \n"+
            "WHERE is_updatable = 'YES'\n"+
            "AND table_schema = :schema\n" +
            "%s"+
            "ORDER BY table_name, ordinal_position;"
        ;

    public final static String SqlIndexSchema = "" +
        "SELECT c.relname || '.' || c2.relname AS compare_name\n"+
            "    , n.nspname AS schema_name\n"+
            "    , c.relname AS table_name\n"+
            "    , c2.relname AS index_name\n"+
            "    , i.indisprimary AS pk\n"+
            "    , i.indisunique AS uq\n"+
            "    , pg_catalog.pg_get_indexdef(i.indexrelid, 0, true) AS index_def\n"+
            "    , pg_catalog.pg_get_constraintdef(con.oid, true) AS constraint_def\n"+
            "    , con.contype AS typ\n"+
            "FROM pg_catalog.pg_index AS i\n"+
            "INNER JOIN pg_catalog.pg_class AS c ON (c.oid = i.indrelid)\n"+
            "INNER JOIN pg_catalog.pg_class AS c2 ON (c2.oid = i.indexrelid)\n"+
            "LEFT OUTER JOIN pg_catalog.pg_constraint con\n"+
            "    ON (con.conrelid = i.indrelid AND con.conindid = i.indexrelid AND con.contype IN ('p','u','x'))\n"+
            "INNER JOIN pg_catalog.pg_namespace AS n ON (c2.relnamespace = n.oid)\n"+
            "WHERE true\n"+
            "AND n.nspname = :schema\n"+
            "%s "+
            "ORDER BY c.relname;";

    public final static String SqlForeignKeySchema = "" +
        "SELECT c.relname || '.' || cn.conname AS compare_name\n" +
            "    , ns.nspname AS schema_name\n" +
            "    , c.relname AS table_name\n" +
            "    , cn.conname AS fk_name\n" +
            "    , cc.relname AS fk_table_name\n" +
            "    , col.attname as fk_column_name" +
            "    , pg_catalog.pg_get_constraintdef(cn.oid, true) as constraint_def\n" +
            "FROM pg_catalog.pg_constraint cn\n" +
            "INNER JOIN pg_class AS c ON (cn.conrelid = c.oid)\n" +
            "INNER JOIN pg_namespace AS ns ON (ns.oid = cn.connamespace)\n" +
            "INNER join pg_attribute as col on (col.attnum = any(cn.confkey) and col.attrelid = cc.oid)\n" +
            "WHERE cn.contype = 'f'\n" +
            "AND ns.nspname = :schema\n" +
            "%s ";
}
