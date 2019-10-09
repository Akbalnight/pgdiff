package com.queries.controller;

import com.queries.model.CompareRequest;
import com.queries.model.DatabaseSettings;
import com.queries.model.ResultSchema;
import com.queries.service.v1.CompareService;
import com.queries.service.CompareServiceV2;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CompareController {

    @Autowired
    CompareService compareService;

    @Autowired
    CompareServiceV2 compareServiceV2;

    @PostMapping("/find-diff")
    public ResponseEntity findDiff( @RequestBody CompareRequest compareRequest ){
        compareRequest.getDatabaseSettingsOne().setPgParams();
        compareRequest.getDatabaseSettingsTwo().setPgParams();

        List<ResultSchema> results = compareService.CompareTables(compareRequest);

        if(results != null)
            return ResponseEntity.ok().body(results);
        else
            return ResponseEntity.status(500).body(new resultConnect(500,"Diff failure"));
    }

    @PostMapping("/find-diff-v2")
    public ResponseEntity findDiffV2( @RequestBody CompareRequest compareRequest ){
        compareRequest.getDatabaseSettingsOne().setPgParams();
        compareRequest.getDatabaseSettingsTwo().setPgParams();

        List results = compareServiceV2.initCompare(compareRequest);

        if(results != null)
            return ResponseEntity.ok().body(results);
        else
            return ResponseEntity.status(500).body(new resultConnect(500,"Diff failure"));
    }

    @PostMapping("/test-connect")
    public ResponseEntity testConnecting( @RequestBody DatabaseSettings databaseSettingsOne ){
        databaseSettingsOne.setPgParams();
        if(compareService.testConnect(databaseSettingsOne))
            return ResponseEntity.ok(new resultConnect(200, "Connect success"));
        else
            return ResponseEntity.badRequest().body(new resultConnect(400,"Connect failure"));
    }

    @Setter
    @Getter
    @AllArgsConstructor
    class resultConnect{
        Integer code;
        String msg;
    }
}
