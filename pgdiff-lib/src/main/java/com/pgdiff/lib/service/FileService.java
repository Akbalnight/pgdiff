package com.pgdiff.lib.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

@Log4j2
@Service
public class FileService {

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd_HH.mm.ss.SSS");

    private String STORAGE_PATH = "./";

    public String saveAsotIntegrationFile(String xmlString) throws IOException {

        Path rootPath;
        String path;
        String fileName;
        String savePath;


        try {
            byte[] bytes = xmlString.getBytes(StandardCharsets.UTF_8);
            fileName =  "delta.sql";
            path = STORAGE_PATH;

            rootPath = (path.startsWith("~/")) ?
                    Paths.get(path.substring(2))
                    : Paths.get(path);

            savePath = rootPath + "/" + fileName;

            File pathFile = rootPath.toFile();
            if (!pathFile.exists()) {
                pathFile.mkdirs();
            }

            Files.write(Paths.get(savePath), bytes);
            log.info("File with data query save to [{}]", savePath);

        } catch (IOException e) {
            log.info("Error save data query to file for {}\n{}", e.getMessage(), e.getStackTrace());
        }

        return xmlString;
    }
}
