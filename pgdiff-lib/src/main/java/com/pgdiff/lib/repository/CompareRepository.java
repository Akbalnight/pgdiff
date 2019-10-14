package com.pgdiff.lib.repository;

import com.pgdiff.lib.model.*;
import com.pgdiff.lib.model.Table.Column;
import com.pgdiff.lib.model.Table.Constraint;
import com.pgdiff.lib.model.Table.Table;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

import static com.pgdiff.lib.model.SqlSchema.*;

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
                                SqlSchema.setPartitionFiltersForPgClass(SqlTableSchema, withPartitions),
                                new MapSqlParameterSource("schema", schema),
                                BeanPropertyRowMapper.newInstance(Table.class));
    }

    public List<Column> getColumnList(DataSource dataSource, String schema, Boolean withPartitions){
        return
                new NamedParameterJdbcTemplate(dataSource)
                        .query(
                                SqlSchema.setPartitionFiltersForPgClass(SqlColumnSchema, withPartitions),
                                new MapSqlParameterSource("schema", schema),
                                BeanPropertyRowMapper.newInstance(Column.class));
    }

    public List<Constraint> getConstraintList(DataSource dataSource, String schema, Boolean withPartitions){
        return
                new NamedParameterJdbcTemplate(dataSource)
                        .query(
                                SqlSchema.setPartitionFiltersForPgClass(SqlConstraintSchema, withPartitions),
                                new MapSqlParameterSource("schema", schema),
                                BeanPropertyRowMapper.newInstance(Constraint.class));
    }
}
