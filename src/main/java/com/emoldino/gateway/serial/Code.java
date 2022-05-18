package com.emoldino.gateway.serial;

public enum Code {
    SOP("*@", 0x02),
    EOP("\n", 0x03),
    SEP(",", 0x07);

    private String name;
    private Integer num;
    Code(String name, int num) {
        this.name = name;
        this.num = num;
    }

    public String getName() {
        return this.name;
    }

    public Integer getNum() {
        return this.num;
    }
}
