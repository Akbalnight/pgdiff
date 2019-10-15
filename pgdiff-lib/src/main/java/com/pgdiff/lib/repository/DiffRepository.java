package com.pgdiff.lib.repository;

import com.pgdiff.lib.model.Grant;
import com.pgdiff.lib.model.SqlSchema;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.util.List;

@Repository
public class DiffRepository {

    private final String DDD = "" +
            "WITH t AS (\n" +
            "    select\n" +
            "        n.nspname AS schema_name\n" +
            "      , c.relkind || '.' || c.relname AS compare_name\n" +
            "      , CASE c.relkind\n" +
            "        WHEN 'r' THEN 'TABLE'\n" +
            "        WHEN 'v' THEN 'VIEW'\n" +
            "        WHEN 'S' THEN 'SEQUENCE'\n" +
            "        WHEN 'f' THEN 'FOREIGN TABLE'\n" +
            "        END as type\n" +
            "      , c.relname AS relationship_name\n" +
            "      , unnest(c.relacl) AS relationship_acl\n" +
            "    FROM pg_class c\n" +
            "    LEFT JOIN pg_namespace n ON (n.oid = c.relnamespace)\n" +
            "    WHERE c.relkind = any(:relkinds)\n" +
            "    AND n.nspname = :schema\n" +
            "    %s\n" +
            "),\n" +
            "t1 as (\n" +
            "    select * from json_each_text\n" +
            "    ('{\n" +
            "    \"a\": \"INSERT\",\n" +
            "    \"r\": \"SELECT\",\n" +
            "    \"w\": \"UPDATE\",\n" +
            "    \"d\": \"DELETE\",\n" +
            "    \"D\": \"TRUNCATE\",\n" +
            "    \"x\": \"REFERENCES\",\n" +
            "    \"t\": \"TRIGGER\",\n" +
            "    \"X\": \"EXECUTE\",\n" +
            "    \"U\": \"USAGE\",\n" +
            "    \"C\": \"CREATE\",\n" +
            "    \"c\": \"CONNECT\",\n" +
            "    \"T\": \"TEMPORARY\"\n" +
            "    }')\n" +
            "),\n" +
            "t2 as (\n" +
            "    SELECT *,\n" +
            "    regexp_matches(relationship_acl::text, '([a-zA-Z0-9]+)*=([rwadDxtXUCcT]+)\\/([a-zA-Z0-9]+)$') as relationship_arr\n" +
            "    FROM t\n" +
            "),\n" +
            "t3 as\n" +
            "(\n" +
            "    select *,\n" +
            "    relationship_arr[1] as role,\n" +
            "    unnest(regexp_split_to_array(relationship_arr[2],'')) as grants\n" +
            "    from t2\n" +
            ")\n" +
            "select t3.schema_name, t3.compare_name, t3.type, t3.relationship_name, t3.relationship_acl, t3.role, string_agg(t1.value, ', ') as grants\n" +
            "from t3\n" +
            "join t1 on t1.key = t3.grants\n" +
            "group by 1,2,3,4,5,6";

    public List<Grant> dddd(DataSource dataSource, String schema, Boolean withPartitions, String[] relkinds) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("schema", schema);
        params.addValue("relkinds", relkinds);
        return
                new NamedParameterJdbcTemplate(dataSource)
                        .query(
                                SqlSchema.setPartitionFiltersForPgClass(DDD, withPartitions),
                                params,
                                BeanPropertyRowMapper.newInstance(Grant.class));

    }

}
