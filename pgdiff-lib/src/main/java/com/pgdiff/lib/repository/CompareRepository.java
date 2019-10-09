package com.pgdiff.repository;

import com.pgdiff.model.Column;
import com.pgdiff.model.SqlSchema;
import com.pgdiff.model.Table;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

import static com.pgdiff.model.SqlSchema.SqlColumnSchema;
import static com.pgdiff.model.SqlSchema.SqlTableSchema;

@Repository
public class CompareRepository {

    public static List getListForCompare(
            DataSource dataSource,
            String schema,
            String sql,
            BeanPropertyRowMapper ROW_MAPPER){

        NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("schema", schema);

        return jdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    public List<Table> getTableList(DataSource dataSource, String schema, Boolean withPartitions){
        return
                new NamedParameterJdbcTemplate(dataSource)
                        .query(
                                SqlSchema.setPartitionFilterForInfoSchema(SqlTableSchema, withPartitions),
                                new MapSqlParameterSource("schema", schema),
                                BeanPropertyRowMapper.newInstance(Table.class));
    }

    public List<Column> getColumnList(DataSource dataSource, String schema, Boolean withPartitions){
        return
                new NamedParameterJdbcTemplate(dataSource)
                        .query(
                                SqlSchema.setPartitionFilterForInfoSchema(SqlColumnSchema, withPartitions),
                                new MapSqlParameterSource("schema", schema),
                                BeanPropertyRowMapper.newInstance(Column.class));
    }
}
