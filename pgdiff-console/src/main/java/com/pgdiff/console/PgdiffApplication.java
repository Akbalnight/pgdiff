package com.pgdiff;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import static java.lang.System.exit;

@Log4j2
@SpringBootApplication
public class PgdiffApplication implements CommandLineRunner
{

    @Autowired
    Environment env;

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
        log.info("dbname1: {}", env.getProperty("dbname1"));
        log.info("dbname2: {}", env.getProperty("dbname2"));
        exit(0); // завершаем программу
    }
}
