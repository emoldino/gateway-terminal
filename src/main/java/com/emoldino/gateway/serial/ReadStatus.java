package com.emoldino.gateway.serial;

public enum ReadStatus {
    READY(0),
    READING(1),
    DONE(2);

    private Integer num;
    ReadStatus(int num) {
        this.num = num;
    }

    public Integer getNum() {
        return this.num;
    }
}
