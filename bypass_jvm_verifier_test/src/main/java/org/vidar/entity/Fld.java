package org.vidar.entity;

public class Fld {
    private final String name;
    private final String type;
    private final long offset;
    private final boolean isStatic;

    public Fld(String name, String type, long offset, boolean isStatic) {
        this.name = name;
        this.type = type;
        this.offset = offset;
        this.isStatic = isStatic;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public long getOffset() {
        return offset;
    }

    public boolean isStatic() {
        return isStatic;
    }

}