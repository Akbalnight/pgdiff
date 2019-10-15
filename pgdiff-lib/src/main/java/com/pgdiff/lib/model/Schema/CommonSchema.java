package com.pgdiff.lib.model.Schema;

import com.pgdiff.lib.model.Alter;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CommonSchema<T> {

    public boolean compare(T ci) { return false;}

    public String getCreate() { return null; }

    public Alter getAdd(String destinationSchema) { return null; }

    public Alter getDrop(String destinationSchema) { return null; }

    public List<Alter> getChange(T ci) { return new ArrayList<>(); }
}
