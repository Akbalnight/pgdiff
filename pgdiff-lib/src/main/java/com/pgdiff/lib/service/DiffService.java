package com.pgdiff.lib.service;

import com.pgdiff.lib.config.DataSourceManager;
import com.pgdiff.lib.model.*;
import com.pgdiff.lib.model.Schema.CommonSchema;
import com.pgdiff.lib.model.Schema.Grant;
import com.pgdiff.lib.model.Schema.Table;
import com.pgdiff.lib.repository.DiffRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
@Service
public class DiffService {

    @Autowired
    DataSourceManager dataSourceManager;

    @Autowired
    DiffRepository diffRepository;

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

    public List<Alter> diffService(CompareRequest compareRequest){

        try {
            DataSource dataSourceOne = dataSourceManager.createHikariDataSource(compareRequest.getDatabaseSettingsOne());
            DataSource dataSourceTwo = dataSourceManager.createHikariDataSource(compareRequest.getDatabaseSettingsTwo());

            // Схемы
            String sourceSchema = compareRequest.getDatabaseSettingsOne().getSchema();
            String destinationSchema = compareRequest.getDatabaseSettingsTwo().getSchema();

            // withPartitions - сравнение с секционными таблицами
            Boolean withPartitions = compareRequest.getWithPartitions();

            // withTableDDL - вернуть резултат с кодами создания таблиц
            Boolean withTableDDL = compareRequest.getWithTableDDL();

            List<String> operations = compareRequest.getOperations();

            List<Alter> results = new ArrayList<>();

            if(operations.contains("TABLE")) {
                List<Table> tablesOne = diffRepository.getTableList(dataSourceOne, sourceSchema, withPartitions);
                List<Table> tablesTwo = diffRepository.getTableList(dataSourceTwo, destinationSchema, withPartitions);
                results.addAll(doDiff( new ArrayList<>(tablesOne), new ArrayList<>(tablesTwo), destinationSchema, withTableDDL));
            }

            if(operations.contains("GRANT")){
                List<Grant> grantsOne = diffRepository.getGrantList(dataSourceOne, sourceSchema, withPartitions, new String[]{"r", "v", "S", "f"});
                List<Grant> grantsTwo = diffRepository.getGrantList(dataSourceTwo, destinationSchema, withPartitions, new String[]{"r", "v", "S", "f"});
                results.addAll(
                    doDiff( new ArrayList<>(grantsOne), new ArrayList<>(grantsTwo), destinationSchema)
                        .stream().sorted(Comparator.comparing(Alter::getAlterType)).collect(Collectors.toList())
                );
            }

            return results;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private List<Alter> doDiff(List<CommonSchema> compareObjectOne, List<CommonSchema> compareObjectTwo, String destinationSchema) {
        return doDiff(compareObjectOne, compareObjectTwo, destinationSchema, false);
    }
    private List<Alter> doDiff(List<CommonSchema> compareObjectOne, List<CommonSchema> compareObjectTwo, String destinationSchema, Boolean withTableDDL){

        List<Alter> sql = new ArrayList<>();
        // Хранит результат сравнения
        Boolean compareVal;
        for(CommonSchema rowOneObj : compareObjectOne){
            compareVal = false;
            for(CommonSchema rowTwoObj : compareObjectTwo){
                compareVal = rowOneObj.compare(rowTwoObj);
                if(compareVal) {
                    sql.addAll(rowOneObj.getChange(rowTwoObj));

                    // Для таблиц: Начало
                    if(rowOneObj.getClass() == Table.class){
                        // SET SUB DDL = ddl
                        if(withTableDDL)
                            sql.add(new Alter(AlterType.DDL_TABLE, ((Table)rowOneObj).getTableName(), ((Table)rowOneObj).getDDL(destinationSchema), ((Table)rowTwoObj).getDDL(destinationSchema)));

                        List<Alter> diffs;
                        diffs = doDiff(
                                    new ArrayList<>(((Table)rowOneObj).getColumns()),
                                    new ArrayList<>(((Table)rowTwoObj).getColumns()),
                                    destinationSchema);
                        diffs.addAll(
                                doDiff(
                                    new ArrayList<>(((Table)rowOneObj).getConstraints()),
                                    new ArrayList<>(((Table)rowTwoObj).getConstraints()),
                                    destinationSchema));

                        List<Alter> sorted = diffs.stream().sorted(Comparator.comparing(Alter::getAlterType)).collect(Collectors.toList());
                        sql.addAll(sorted);
                    }
                    // Для таблиц: Конец

                    compareObjectTwo.remove(rowTwoObj);
                    break;
                }
            }
            if(!compareVal){
//                if(rowOneObj.getClass() == Table.class && withTableDDL)
//                    sql.add(new Alter(AlterType.DDL_TABLE, ((Table)rowOneObj).getDDL(destinationSchema), null));
//                else
                sql.add(rowOneObj.getAdd(destinationSchema));

                // SET SUB DDL = null
            }
        }
        for(CommonSchema rowTwoObj : compareObjectTwo){
            if(rowTwoObj.getClass() == Table.class && withTableDDL)
                sql.add(new Alter(AlterType.DDL_TABLE, ((Table)rowTwoObj).getTableName(), null, ((Table)rowTwoObj).getDDL(destinationSchema)));

            sql.add(rowTwoObj.getDrop(destinationSchema));
            // SET MAIN DDL = null
            // SET SUB DDL = ddl
        }
        return sql;
    }
}
