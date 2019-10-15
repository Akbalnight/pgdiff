package com.pgdiff.console;

import com.pgdiff.lib.model.CompareRequest;
import com.pgdiff.lib.model.DatabaseSettings;
import com.pgdiff.lib.model.Result;
import com.pgdiff.lib.service.FileService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;

@Log4j2
@SpringBootApplication(scanBasePackages={"com.pgdiff.console", "com.pgdiff.lib"})
public class PgdiffApplication implements CommandLineRunner
{

    @Autowired
    Environment env;

    @Autowired
    CompareService compareService;

    @Autowired
    FileService fileService;

    public static void main(String[] args)
    {
        log.info("STARTING THE APPLICATION");
        //отключаем баннер spring boot, если не хотим видеть его лого в консоли
        SpringApplication app = new SpringApplication(PgdiffApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }

    @Override
    public void run(String... args) throws Exception {

        DatabaseSettings databaseSettings1 = new DatabaseSettings();
        databaseSettings1.setHost(env.getProperty("host1"));
        databaseSettings1.setPost(env.getProperty("port1"));
        databaseSettings1.setDbname(env.getProperty("dbname1"));
        databaseSettings1.setSchema(env.getProperty("schema1"));
        databaseSettings1.setUsername(env.getProperty("username1"));
        databaseSettings1.setPassword(env.getProperty("password1"));
        databaseSettings1.setPgParams();

        DatabaseSettings databaseSettings2 = new DatabaseSettings();
        databaseSettings2.setHost(env.getProperty("host2"));
        databaseSettings2.setPost(env.getProperty("port2"));
        databaseSettings2.setDbname(env.getProperty("dbname2"));
        databaseSettings2.setSchema(env.getProperty("schema2"));
        databaseSettings2.setUsername(env.getProperty("username2"));
        databaseSettings2.setPassword(env.getProperty("password2"));
        databaseSettings2.setPgParams();

        CompareRequest compareRequest = new CompareRequest();
        compareRequest.setDatabaseSettingsOne(databaseSettings1);
        compareRequest.setDatabaseSettingsTwo(databaseSettings2);
        compareRequest.setWithPartitions(Boolean.valueOf(env.getProperty("withPartitions")));

        List<Result> results = compareService.initCompare(compareRequest);

        List<String> resultsFile = new ArrayList<>();
        if(results != null)
            for(Result result : results){
                if(result.getResultCode() != -1) {
                    String ddlS = result.getResultCode() == 0 ? "\n" + result.getDdlTableOne() : "";
                    String ddlD = result.getResultCode() == 4 ? "\n" + result.getDdlTableTwo() : "";

                    String columns = result.getAlters() != null ? "\n" + String.join("\n", result.getAlters()) : "";

                    String line = String.format("-- Source: %s\n-- Destination: %s%s%s%s\n\n",
                            result.getNameTableOne(),
                            result.getNameTableTwo(),
                            ddlS, ddlD, columns);

                    resultsFile.add(line);
                    log.info(line);
                }
            }
        fileService.saveAsotIntegrationFile(String.join("", resultsFile));
        exit(0); // завершаем программу
    }
}
