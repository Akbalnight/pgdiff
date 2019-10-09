package com.pgdiff.ui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages={"com.pgdiff.ui", "com.pgdiff.lib"})
public class QueriesApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(QueriesApplication.class, args);
    }
}
