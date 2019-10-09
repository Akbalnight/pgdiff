package com.pgdiff.lib.repository;

import com.pgdiff.lib.model.*;
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

    public List<Index> getIndexList(DataSource dataSource, String schema, Boolean withPartitions){
        return
                new NamedParameterJdbcTemplate(dataSource)
                        .query(
                                SqlSchema.setPartitionFiltersForPgClass(SqlIndexSchema, withPartitions),
                                new MapSqlParameterSource("schema", schema),
                                BeanPropertyRowMapper.newInstance(Index.class));
    }

    public List<ForeignKey> getForeignKeyList(DataSource dataSource, String schema, Boolean withPartitions){
        return
                new NamedParameterJdbcTemplate(dataSource)
                        .query(
                                SqlSchema.setPartitionFiltersForPgClass(SqlForeignKeySchema, withPartitions),
                                new MapSqlParameterSource("schema", schema),
                                BeanPropertyRowMapper.newInstance(ForeignKey.class));
    }
}
