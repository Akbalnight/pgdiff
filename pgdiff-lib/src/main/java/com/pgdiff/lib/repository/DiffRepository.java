package com.pgdiff.lib.repository;

import com.pgdiff.lib.model.Schema.Grant;
import com.pgdiff.lib.model.Sql.CommonSql;
import com.pgdiff.lib.model.Schema.Column;
import com.pgdiff.lib.model.Schema.Constraint;
import com.pgdiff.lib.model.Schema.Table;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.pgdiff.lib.model.Sql.CommonSql.mappingTableFields;
import static com.pgdiff.lib.model.Sql.TableSql.TABLE_SQL;
import static com.pgdiff.lib.model.Sql.ColumnSql.COLUMN_SQL;
import static com.pgdiff.lib.model.Sql.ConstraintSql.CONSTRAINT_SQL;
import static com.pgdiff.lib.model.Sql.GrantSql.GRANT_SQL;

@Repository
public class DiffRepository {

    public List<Table> getTableList(DataSource dataSource, String schema, Boolean withPartitions){

        // Получить таблицы
        List<Table> tables = getTables(dataSource, schema, withPartitions);

        // Получить колонки
        List<Column> columnsOne = getColumnList(dataSource, schema, withPartitions);

        // Получить ограничения
        List<Constraint> constraintsOne = getConstraintList(dataSource, schema, withPartitions);

        // Сгруппировать колонки по таблицам
        Map<String, List<Column>> mapColumnsOne = columnsOne.stream().collect(Collectors.groupingBy(w -> w.getTableName()));

        // Сгруппировать ограничения по таблицам
        Map<String, List<Constraint>> mapConstraintsOne = constraintsOne.stream().collect(Collectors.groupingBy(w -> w.getTableName()));

        // Присвоить таблицам колонки и ограничения
        tables = mappingTableFields(tables, mapColumnsOne, mapConstraintsOne);

        return tables;

    }

    private List<Table> getTables(DataSource dataSource, String schema, Boolean withPartitions){
        return
                new NamedParameterJdbcTemplate(dataSource)
                        .query(
                                CommonSql.setPartitionFiltersForPgClass(TABLE_SQL, withPartitions),
                                new MapSqlParameterSource("schema", schema),
                                BeanPropertyRowMapper.newInstance(Table.class));
    }

    public List<Column> getColumnList(DataSource dataSource, String schema, Boolean withPartitions){
        return
                new NamedParameterJdbcTemplate(dataSource)
                        .query(
                                CommonSql.setPartitionFiltersForPgClass(COLUMN_SQL, withPartitions),
                                new MapSqlParameterSource("schema", schema),
                                BeanPropertyRowMapper.newInstance(Column.class));
    }

    public List<Constraint> getConstraintList(DataSource dataSource, String schema, Boolean withPartitions){
        return
                new NamedParameterJdbcTemplate(dataSource)
                        .query(
                                CommonSql.setPartitionFiltersForPgClass(CONSTRAINT_SQL, withPartitions),
                                new MapSqlParameterSource("schema", schema),
                                BeanPropertyRowMapper.newInstance(Constraint.class));
    }

    public List<Grant> getGrantList(DataSource dataSource, String schema, Boolean withPartitions, String[] relkinds) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("schema", schema);
        params.addValue("relkinds", relkinds);
        return
                new NamedParameterJdbcTemplate(dataSource)
                        .query(
                                CommonSql.setPartitionFiltersForPgClass(GRANT_SQL, withPartitions),
                                params,
                                BeanPropertyRowMapper.newInstance(Grant.class));

    }

}
