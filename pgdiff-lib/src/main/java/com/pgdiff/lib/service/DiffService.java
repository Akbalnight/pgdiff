package com.pgdiff.lib.service;

import com.pgdiff.lib.config.DataSourceManager;
import com.pgdiff.lib.model.Alter;
import com.pgdiff.lib.model.CompareInterface;
import com.pgdiff.lib.model.CompareRequest;
import com.pgdiff.lib.model.Grant;
import com.pgdiff.lib.model.Table.Column;
import com.pgdiff.lib.model.Table.Constraint;
import com.pgdiff.lib.model.Table.Table;
import com.pgdiff.lib.repository.DiffRepository;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
@Service
public class DiffService {

    @Autowired
    DataSourceManager dataSourceManager;

    @Autowired
    DiffRepository diffRepository;

    Map<String, String> permMap = new HashMap<>();

    @PostConstruct
    void init(){
        permMap.put("a", "INSERT");
        permMap.put("r", "SELECT");
        permMap.put("w", "UPDATE");
        permMap.put("d", "DELETE");
        permMap.put("D", "TRUNCATE");
        permMap.put("x", "REFERENCES");
        permMap.put("t", "TRIGGER");
        permMap.put("X", "EXECUTE");
        permMap.put("U", "USAGE");
        permMap.put("C", "CREATE");
        permMap.put("c", "CONNECT");
        permMap.put("T", "TEMPORARY");
    }

    public List<Alter> parceGrant(CompareRequest compareRequest){

        try {
            DataSource dataSourceOne = dataSourceManager.createHikariDataSource(compareRequest.getDatabaseSettingsOne());
            DataSource dataSourceTwo = dataSourceManager.createHikariDataSource(compareRequest.getDatabaseSettingsTwo());


            List<Grant> grantsOne = diffRepository.dddd(
                    dataSourceOne,
                    compareRequest.getDatabaseSettingsOne().getSchema(),
                    compareRequest.getWithPartitions(), new String[]{"r"});

            List<Grant> grantsTwo = diffRepository.dddd(
                    dataSourceTwo,
                    compareRequest.getDatabaseSettingsTwo().getSchema(),
                    compareRequest.getWithPartitions(), new String[]{"r"});

            // Схема назначения
            String destinationSchema = compareRequest.getDatabaseSettingsTwo().getSchema();

            List<Alter> results =
            doDiff(
                    new ArrayList<>(grantsOne),
                    new ArrayList<>(grantsTwo),
                    destinationSchema);
//            Pattern pattern = Pattern.compile("([a-zA-Z0-9]+)*=([rwadDxtXUCcT]+)/([a-zA-Z0-9]+)$");
////            String[] words = pattern.split(input);
//            for(Grant grant : grants){
//                log.info(" --- ");
//                log.info("grant.getRelationshipAcl() = [{}]", grant.getRelationshipAcl());
//                Matcher matcher = pattern.matcher(grant.getRelationshipAcl());
//                if (matcher.find()) {
//                    log.info("role = [{}]", matcher.group(1));
//                    log.info("perms = [{}]", matcher.group(2));
//                }
//                List<String> perms = new ArrayList<>();
//                for (String perm : matcher.group(2).split("")){
////                    log.info("perm = [{}]", permMap.get(perm));
//                    perms.add(permMap.get(perm));
//                }
//                log.info(String.format("GRANT %s ON %s.%s TO %s; -- Add\n",
//                        String.join(", ", perms),
//                        grant.getSchemaName(),
//                        grant.getRelationshipName(),
//                        matcher.group(1)));
//
////                String[] ss = pattern.split(grant.getRelationshipAcl());
////                for(String s : ss){
////                    log.info(s);
////                }
//            }
            return results;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<Alter> doDiff(List<CompareInterface> compareObjectOne, List<CompareInterface> compareObjectTwo, String destinationSchema){

        List<Alter> sql = new ArrayList<>();
        // Хранит результат сравнения
        Boolean compareVal;
        for(CompareInterface rowOneObj : compareObjectOne){
            if(rowOneObj.getClass() == Column.class)
                log.info("Колонки");
            else if (rowOneObj.getClass() == Constraint.class)
                log.info("Ограничения");
            compareVal = false;
            for(CompareInterface rowTwoObj : compareObjectTwo){
                compareVal = rowOneObj.compare(rowTwoObj);
                if(compareVal) {
                    sql.addAll(rowOneObj.getChange(rowTwoObj));
                    compareObjectTwo.remove(rowTwoObj);
                    break;
                }
            }
            if(!compareVal){
                sql.add(rowOneObj.getAdd(destinationSchema));
            }
        }
        for(CompareInterface rowTwoObj : compareObjectTwo){
            sql.add(rowTwoObj.getDrop(destinationSchema));
        }
        return sql;
    }
}
