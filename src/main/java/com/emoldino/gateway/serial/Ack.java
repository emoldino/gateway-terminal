package com.emoldino.gateway.serial;

public enum Ack {
    OK("OK"),
    NG("NG");


    private String name;
    Ack(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
