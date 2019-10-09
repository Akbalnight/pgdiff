package com.pgdiff.lib.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CompareInterface<T> {

    public boolean compare(T ci) { return false;}

    public String getCreate() { return ""; }

    public String getAdd(String destinationSchema) { return ""; }

    public String getDrop(String destinationSchema) { return ""; }

    public List<String> getChange(T ci) { return new ArrayList<>(); }
}
