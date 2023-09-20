package org.vidar.entity;

import java.util.HashMap;
import java.util.Map;

public class JVMType {
    private final String type;
    private final String superClass;
    private final int size;
    private final boolean oop;
    private final boolean intType;
    private final boolean unsigned;
    private final Map<String, Fld> fields;

    public JVMType(String type, String superClass, int size, boolean oop, boolean intType, boolean unsigned) {
        this.type = type;
        this.superClass = superClass;
        this.size = size;
        this.oop = oop;
        this.intType = intType;
        this.unsigned = unsigned;
        this.fields = new HashMap<>();
    }

    public Map<String, Fld> getFields() {
        return fields;
    }

    public String getType() {
        return type;
    }

    public String getSuperClass() {
        return superClass;
    }

    public int getSize() {
        return size;
    }

    public boolean isOop() {
        return oop;
    }

    public boolean isIntType() {
        return intType;
    }

    public boolean isUnsigned() {
        return unsigned;
    }
}