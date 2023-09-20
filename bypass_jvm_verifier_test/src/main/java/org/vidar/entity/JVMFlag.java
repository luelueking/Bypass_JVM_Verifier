package org.vidar.entity;

public class JVMFlag {
    private final String name;
    private final long address;

    public JVMFlag(String name, long address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public long getAddress() {
        return address;
    }
}