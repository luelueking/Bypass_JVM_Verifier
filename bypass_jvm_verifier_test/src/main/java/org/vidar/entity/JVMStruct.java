package org.vidar.entity;

import java.util.HashMap;
import java.util.Map;

public class JVMStruct {
    private final String name;
    private final Map<String, Fld> fields;

    public JVMStruct(String name) {
        this.name = name;
        this.fields = new HashMap<>();
    }

    public void setField(String fieldName, Fld value) {
        fields.put(fieldName, value);
    }

    public Fld getField(String fieldName) {
        return fields.get(fieldName);
    }

    public String getName() {
        return name;
    }

    public Map<String, Fld> getFields() {
        return fields;
    }
}