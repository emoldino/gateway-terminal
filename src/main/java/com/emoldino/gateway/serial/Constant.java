package com.emoldino.gateway.serial;

public class Constant {
    public static final String SOP = "*@";
    public static final String EOP = "\n";
    public static final String SEP = ",";

    public static final char STX1 = '*';
    public static final char STX2 = '@';
    public static final char LF = '\n';

    public static final String STOP ="Stop";
    public static final String START = "Start";
    public static final String CDATA = "Cdata";
    public static final String CONNECT = "Connect";
    public static final String DISCONNECT = "Disconnect";
    public static final String INVALID = "Invalid";
    public static final String ERROR = "Error";

    public static final String BYPASS = "Bypass";

    public static final String TIME = "Time";
    public static final String OK = "OK";
    public static final String NG = "NG";

    public static final int READY = 0;
    public static final int READING = 1;
    public static final int DONE = 2;
}
