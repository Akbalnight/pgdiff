package com.pgdiff.lib.service;

import com.pgdiff.lib.config.DataSourceManager;
import com.pgdiff.lib.model.*;
import com.pgdiff.lib.repository.CompareRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Service
public class CompareService {

    @Autowired
    DataSourceManager dataSourceManager;

    @Autowired
    CompareRepository compareRepository;

    public boolean testConnect(DatabaseSettings databaseSettings){
        try {
            DataSource ds = dataSourceManager.createHikariDataSource(databaseSettings);
            if (ds != null) return true;
            else return false;
        }catch (Exception e){
            log.info("Bad test connect");
            return false;
        }
    }

    public List<Result> initCompare(CompareRequest compareRequest){
        /**
         * Схемы
         * Роли
         * Последовательности (Sequences)
         * + Таблицы + колонки + ограничения (pk / un / fk)
         * + Вьюхи
         * Функции
         * Триггеры
         * Владельцы
         * Права на отношения
         * Права на атрибутты
         */
        try {
            DataSource dataSourceOne = dataSourceManager.createHikariDataSource(compareRequest.getDatabaseSettingsOne());
            DataSource dataSourceTwo = dataSourceManager.createHikariDataSource(compareRequest.getDatabaseSettingsTwo());

            List<Table> tablesOne = compareRepository.getTableList(dataSourceOne, compareRequest.getDatabaseSettingsOne().getSchema(), compareRequest.getWithPartitions());
            List<Table> tablesTwo = compareRepository.getTableList(dataSourceTwo, compareRequest.getDatabaseSettingsTwo().getSchema(), compareRequest.getWithPartitions());

            List<Column> columnsOne = compareRepository.getColumnList(dataSourceOne, compareRequest.getDatabaseSettingsOne().getSchema(), compareRequest.getWithPartitions());
            List<Column> columnsTwo = compareRepository.getColumnList(dataSourceTwo, compareRequest.getDatabaseSettingsTwo().getSchema(), compareRequest.getWithPartitions());

            List<Constraint> constraintsOne = compareRepository.getConstraintList(dataSourceOne, compareRequest.getDatabaseSettingsOne().getSchema(), compareRequest.getWithPartitions());
            List<Constraint> constraintsTwo = compareRepository.getConstraintList(dataSourceTwo, compareRequest.getDatabaseSettingsTwo().getSchema(), compareRequest.getWithPartitions());

            Map<String, List<Column>> mapColumnsOne = columnsOne.stream().collect(Collectors.groupingBy(w -> w.getTableName()));
            Map<String, List<Column>> mapColumnsTwo = columnsTwo.stream().collect(Collectors.groupingBy(w -> w.getTableName()));

            Map<String, List<Constraint>> mapConstraintsOne = constraintsOne.stream().collect(Collectors.groupingBy(w -> w.getTableName()));
            Map<String, List<Constraint>> mapConstraintsTwo = constraintsTwo.stream().collect(Collectors.groupingBy(w -> w.getTableName()));

            tablesOne = mapping(tablesOne, mapColumnsOne, mapConstraintsOne);
            tablesTwo = mapping(tablesTwo, mapColumnsTwo, mapConstraintsTwo);


            // Хранит результат сравнения
            Boolean compareVal;
            // Схема назначения
            String destinationSchema = compareRequest.getDatabaseSettingsTwo().getSchema();

            List<Result> results = new ArrayList<>();

            for(Table tableOne : tablesOne){
                compareVal = false;
                Result result = new Result();
                result.setNameTableOne(tableOne.getTableName());
                result.setDdlTableOne(tableOne.getDDL(destinationSchema));

                for(Table tableTwo : tablesTwo) {
                    // Вызов сравнения таблиц
                    compareVal = tableOne.equals(tableTwo);

                    // Если таблицы равны, то проверяем колонки, индексы и внешние ключи
                    if(compareVal) {
                        result.setNameTableTwo(tableTwo.getTableName());
                        result.setDdlTableTwo(tableTwo.getDDL(destinationSchema));

                        result.setAlters(
                            doDiff(
                                new ArrayList<>(tableOne.getColumns()),
                                new ArrayList<>(tableTwo.getColumns()),
                                destinationSchema));

                        result.addAllAlters(
                            doDiff(
                                    new ArrayList<>(tableOne.getConstraints()),
                                    new ArrayList<>(tableTwo.getConstraints()),
                                    destinationSchema)
                        );

                        if(result.getAlters() != null && result.getAlters().size() > 0) result.setResultCode(1);
                        else result.setResultCode(-1);

//                        log.info(colSql);
                        tablesTwo.remove(tableTwo);
                        break;
                    }
                }
                // Если таблица для сравнения не найдена, то создаем таблицу
                if(!compareVal){
                    result.setNameTableTwo(null);
                    result.setDdlTableTwo(null);
                    result.setResultCode(0);
//                    tableOne.getDDL(destinationSchema);
                }
                results.add(result);
            }

            for(Table tableTwo : tablesTwo){
                results.add(
                    new Result(
                            null,
                            tableTwo.getTableName(),
                            null,
                            tableTwo.getDrop(destinationSchema),
                            null,
                            4));
//                log.info(tableTwo.getDrop(destinationSchema));
            }

            return results;

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }

    private List<Table> mapping(List<Table> tables, Map<String, List<Column>> columns, Map<String, List<Constraint>> constraints){
        for(Table table : tables){
            table.setColumns(
                    columns.get(table.getTableName()) != null ?
                            columns.get(table.getTableName()) :
                            new ArrayList<>()
            );
            table.setConstraints(
                    constraints.get(table.getTableName()) != null ?
                            constraints.get(table.getTableName()) :
                            new ArrayList<>()
                    );
        }
        return tables;
    }


    private List<String> doDiff(List<CompareInterface> compareObjectOne, List<CompareInterface> compareObjectTwo, String destinationSchema){

        List<String> sql = new ArrayList<>();

        // Хранит результат сравнения
        Boolean compareVal;

        for(CompareInterface rowOneObj : compareObjectOne){

            compareVal = false;

            for(CompareInterface rowTwoObj : compareObjectTwo){

                compareVal = rowOneObj.compare(rowTwoObj);

                if(compareVal) {
//                    String resultVal = rowOneObj.getChange(rowTwoObj, destinationSchema);

                    sql.addAll(rowOneObj.getChange(rowTwoObj));

//                    if(resultVal != "") compareObjectTwo.logResult(rowOne, rowTwo, resultVal, 0);
                    compareObjectTwo.remove(rowTwoObj);
                    break;
                }
            }

            if(!compareVal){
                sql.add(rowOneObj.getAdd(destinationSchema));
//                compareObjectTwo.logResult(rowOne, null, compareObjectOne.addQuery(rowOne, destinationSchema), 1);
            }
        }
        for(CompareInterface rowTwoObj : compareObjectTwo){
            sql.add(rowTwoObj.getDrop(destinationSchema));
//            compareObjectTwo.logResult(null, rowTwo, compareObjectTwo.dropQuery(rowTwo), -1);
        }

//        compareObjectOne.getResults().addAll(compareObjectTwo.getResults());
        return sql; //compareObjectOne.getResults();
    }
}
