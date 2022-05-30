package com.emoldino.gateway.serial;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CdataPacket {
    String counterId;
    int index;
    int total;

    String data;

    public CdataPacket(String id, int index, int total, String data) {
        this.counterId = id;
        this.index = index;
        this.total = total;
        this.data = data;
    }

    public String toString() {
        return "cdataPacket : [" + counterId + "][" + index + "][" + total + "] " + data;
    }
}
