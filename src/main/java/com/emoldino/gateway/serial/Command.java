package com.emoldino.gateway.serial;

public enum Command {
    STOP("Stop"),
    START("Start"),
    CDATA("Cdata"),
    CONNECT("Connect"),
    DISCONNECT("Disconnect"),
    OK("OK"),
    NG("NG");


    private String name;
    Command(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
