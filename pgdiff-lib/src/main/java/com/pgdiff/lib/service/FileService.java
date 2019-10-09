package com.assd.asotintegration.service;

import com.google.common.base.Charsets;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Log4j2
@Service
public class FileService {

    private Logger logger = LoggerFactory.getLogger(FileService.class);
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd_HH.mm.ss.SSS");

    @Value("${storage.path}")
    private String STORAGE_PATH;

    public String saveAsotIntegrationFile(String xmlString, String tableName) throws IOException {

        Path rootPath;
        String path;
        String fileName;
        String savePath;


        try {
            byte[] bytes = xmlString.getBytes(Charsets.ISO_8859_1);
            String currentDate = LocalDateTime.now().format(formatter);
            String currentMonth = LocalDateTime.now().getMonth().toString();
            fileName = currentDate + ".xml";
            path = STORAGE_PATH + currentMonth + "/" + tableName;

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
            log.info("Error save data query to file for [{}]\n{}\n{}", tableName, e.getMessage(), e.getStackTrace());
        }

        return xmlString;
    }
}
