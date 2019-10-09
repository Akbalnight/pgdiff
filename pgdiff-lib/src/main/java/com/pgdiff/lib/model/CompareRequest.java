package com.pgdiff.lib.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CompareRequest {
    private DatabaseSettings databaseSettingsOne;
    private DatabaseSettings databaseSettingsTwo;
    private List<String> operations;
    private Boolean withPartitions;
}
