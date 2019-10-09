package com.pgdiff.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatabaseSettings {

    private static String DEFAULT_DRIVER =  "org.h2.Driver";
    private static String DEFAULT_URL = "jdbc:h2:mem:test";
    private static String DEFAULT_UNAME = "SA";
    private static String DEFAULT_PWD = "";
    private static Integer DEFAULT_MAX_POOL_SIZE = 5;

    private String driver = DEFAULT_DRIVER;
    private String url = DEFAULT_URL;
    private String username = DEFAULT_UNAME;
    private String password = DEFAULT_PWD;
    private String host;
    private String post;
    private String dbname;
    private String schema;
    private Integer poolSize = DEFAULT_MAX_POOL_SIZE;

    public void setPgParams(){
        this.driver = "org.postgresql.Driver";
        this.url = String.format("jdbc:postgresql://%s:%s/%s",host, post, dbname);
    }
}
