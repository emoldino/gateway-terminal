package com.emoldino.gateway;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.emoldino.gateway.serial.*;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fazecast.jSerialComm.SerialPort;


public class Gateway extends Thread {


	private static Gson gson = new Gson();
	public static Gateway instance = null;
	private final static String MYDBG = "[Emoldino]";
	
	private final static String VERSION = "3.0.0";
	
	private final static String SUBURL_SENSOR = "/mms/data/seneor";
	private final static String SUBURL_DATA = "/mms/data";
	private final static String SUBURL_HEARTBEAT = "/mms/data/heartbeat";
	
	
	private final static String MODE_READ = "MODE_READ";
	private final static String MODE_WRITE = "MODE_WRITE";
	
	private final static byte CMD_INCRESE_ID = (byte) 0xFF;
	private final static byte CMD_SET_TIME = 0x10;
	private final static byte CMD_OPEN = 0x20;
	private final static byte CMD_CLOSE = 0x21;
	private final static byte CMD_READ = 0x30;
	private final static byte CMD_READ_COUNT = 0x31;
	private final static byte CMD_RECV_ALL= 0x32;
	private final static byte CMD_READ_COMPLETE = 0x33;
	
	private final static byte DATA_OK = 0x06;
	private final static byte DATA_FAULT = 0x15;

	private final static byte STX = 0x02;
	private final static byte ETX = 0x03;
	
	private final int LEN_STX = 1;
	private final int LEN_LENGTH = 2;
	private final int LEN_CMD = 1;
	private final int LEN_CRC = 2;
	private final int LEN_ETX = 1;
	
	SerialPort comPort;
	CrC16Modbus crcModbus;
	
	Dba dba;

	private InputStream in;
	private OutputStream out;
	
	private String mode = MODE_WRITE;

	private Command writeCmd = Command.START;

	private boolean isBypass = false;

	private final ConcurrentHashMap<String, CdataPacket> counterCdataMap = new ConcurrentHashMap<String, CdataPacket>();

	long serialTimer = 0; // System.currentTimeMillis();
	long serverTimer = 0; // System.currentTimeMillis();
	

	private boolean _saveLogfile = false;
	public boolean isSaveLogfile() {
		return _saveLogfile;
	}
	public void setSaveLogfile(boolean saveLogfile) {
		this._saveLogfile = saveLogfile;
	}

	private long default_serialWatchdog = 0;
	public long getDefault_serialWatchdog() {
		return default_serialWatchdog;
	}
	public void setDefault_serialWatchdog(long default_serialWatchdog) {
		this.default_serialWatchdog = default_serialWatchdog;
	}

	private long _serialWatchdog = 0;
	public long get_serialWatchdog() {
		return _serialWatchdog;
	}
	public void set_serialWatchdog(long _serialWatchdog) {
		this._serialWatchdog = _serialWatchdog;
	}
	
	private long _serverWatchdog = 0;
	public long get_serverWatchdog() {
		return _serverWatchdog;
	}
	public void set_serverWatchdog(long _serverWatchdog) {
		this._serverWatchdog = _serverWatchdog;
	}


	private String _terminalId;
	public String getTerminalId() {
		return _terminalId;
	}
	public void setTerminalId(String terminalId) {
		this._terminalId = terminalId.length() > 20 ? terminalId.substring(0, 20) : terminalId ;
	}

	private String _serverUrl;
	public String getServerUrl() {
		return _serverUrl;
	}
	public void setServerUrl(String serverUrl) {
		this._serverUrl = serverUrl;
	}
	
	private byte _sensorNo = 1;
	public byte get_sensorNo() {
		return _sensorNo;
	}
	public void set_sensorNo(byte _sensorNo) {
		this._sensorNo = _sensorNo;
	}

	private boolean _bExit = false;
	public boolean is_bExit() {
		return _bExit;
	}
	public void set_bExit(boolean _bExit) {
		this._bExit = _bExit;
	}

	private StringBuffer sb = new StringBuffer();
	
	public String _hostname;

	private int invalidCnt = 0;

	public Gateway(){
		
		try {
			this._hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		crcModbus = new CrC16Modbus();
		dba = new Dba();
	}

	public Gateway(String serialPort, String serialWatchdog, String serverUrl, String serverWatchdog, String isSave) throws UnknownHostException {

		Gateway.instance = this;
		try {
			setSaveLogfile(Boolean.parseBoolean(isSave));
		} catch(Exception e) {
			setSaveLogfile(true);
		}

		this._hostname = InetAddress.getLocalHost().getHostName();
		
		setTerminalId(_hostname);
		
		try {
			setDefault_serialWatchdog(Integer.parseInt(serialWatchdog));
		} catch (Exception e){
			setDefault_serialWatchdog(3000);
		}
		set_serialWatchdog(getDefault_serialWatchdog());
		
		try {
			set_serverWatchdog(Integer.parseInt(serverWatchdog));
		} catch (Exception e){
			set_serverWatchdog(600000);	// Default 10 min
		}
		

		// Serial Open
		try {
			crcModbus = new CrC16Modbus();
			dba = new Dba();
			setServerUrl(serverUrl);

			comPort = SerialPort.getCommPort(serialPort);
			comPort.setBaudRate(Integer.parseInt("115200"));
			comPort.setParity(0);
			comPort.setNumDataBits(8);
			comPort.setNumStopBits(1);
			comPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
			comPort.openPort();
			comPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);

			if (comPort.isOpen()) {


				// 입력 스트림
				in = comPort.getInputStream();
				// 출력 스트림
				out = comPort.getOutputStream();

				myDebug("[OK]serialPort is Opened : " + serialPort);

				comPort.addDataListener(new SerialPortDataListener() {
					@Override
					public int getListeningEvents() {
						System.out.println("USB ComPort is now available !!!");
						Gateway.instance.sendTextFrame(Gateway.instance.out, Constant.SOP + Constant.STOP + Constant.EOP);
						return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
					}

					@Override
					public void serialEvent(SerialPortEvent serialPortEvent) {

//						myDebug("USB ComPort read data is available !!! : " + gson.toJson(serialPortEvent));

					}
				});

			} else {
				try { comPort.closePort(); } catch(Exception ex) { ex.printStackTrace(); }
				myDebug("[NG]serialPort is not Opened : System Exit :" + serialPort);
				System.exit(-1);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public void freeze() throws InterruptedException {
		Object obj = new Object();
		synchronized (obj) {
			obj.wait();
		}
	}

	@Override
	public boolean isInterrupted() {
		// TODO Auto-generated method stub
		return super.isInterrupted();
	}

	@Override
	public void run() {
		



		int pos = 0;
		
//		int sensorNo = 0;
		int sendLen = 0;
		

        
        boolean isSensorOpened = false;	// 센서 열린 상태

		ReadStatus readStatus = ReadStatus.READY;

		while (!this.isInterrupted() && !is_bExit()) {

			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// 루프를 중단하기 위한 인터럽트 였으나,
				// sleep중 인터럽트가 발생하여 sleep만 빠져나가고 메인 루프는 계속 실행됨.
				// 플레그를 세팅하여 빠져 나오게 함.
				set_bExit(true);
				e.printStackTrace();
			}
			
			
			// Send Heartbit at every 10 minute 
			if (System.currentTimeMillis() - serverTimer > get_serverWatchdog()) {
				serverTimer = System.currentTimeMillis(); 
				
				this.reportHeartbeat();
			}

			try {
				switch (mode) {
					case MODE_WRITE: {

						switch (writeCmd.getName()) {
							case Constant.START: {
								myDebug("[write] send cmd = " + writeCmd.getName());
								sendStart();
								set_serialWatchdog(5000);
							}
							break;
							case Constant.CDATA: {
								myDebug("[write] send cmd = " + writeCmd.getName());
								sendAck(Command.CDATA, Ack.OK);
								set_serialWatchdog(1000);
							}
							break;
							default: {
								myDebug("[write] send cmd = " + writeCmd.getName());
								sendTextFrame(out, Code.SOP.getName() + writeCmd.getName() + Code.EOP.getName());
								set_serialWatchdog(5000);
							}
						}

						mode = MODE_READ;
						serialTimer = System.currentTimeMillis();
					}
					break;

					/**
					 * 수신 시퀀스 -----------------------------------------------------
					 */
					case MODE_READ: {


						// 수신 와치독. 모드 변
						if (System.currentTimeMillis() - serialTimer < get_serialWatchdog()) {

							myDebug("[read] Time out!! -----------------");
							System.out.println("");
							continue;
						}

						// 데이터 수신.
						try {
							int recvLen = 0 ;
							byte[] serialBuffer = new byte[1024];
							while((recvLen = this.in.available()) > 0 && (recvLen = this.in.read(serialBuffer)) > 0) {

								sb.append(new String(serialBuffer, 0, recvLen));
							}
						} catch (IOException e) {
							e.printStackTrace();
						}

						myDebug("[READ]" + sb.toString());
						byte[] packets = sb.toString().getBytes();
						String cmdLine = sb.toString(); // for debugging

						if (sb.toString().isEmpty()) {
							continue;
						}

						int end = sb.toString().indexOf(Constant.LF);
						if (cmdLine.startsWith(Constant.SOP)  && end > 0 && end < sb.length()-2) {
							String response = "";
							if (packets[end+1] == 0) {
								response = sb.substring(0, end + 1);
								parseResponse(response);
								sb.delete(0, end+2);
							} else {
								response = sb.substring(0, end + 1);
								parseResponse(response);
								sb.delete(0, end+1);
							}

						} else if (cmdLine.startsWith(Constant.SOP) && (packets[sb.length()-1] == Constant.LF || (packets[sb.length()-2] == Constant.LF && packets[sb.length()-1] == 0))) {
							String response = sb.substring(0, end + 1);
							parseResponse(response);
							sb.delete(0, sb.length());
						} else if (cmdLine.startsWith(Constant.SOP)  && sb.toString().indexOf(Constant.LF) < 0) {
							// Do nothing
						}  else {

//							mode = MODE_WRITE;
//							writeCmd = Command.STOP;
							myDebug("[ERROR} Unknown Read data format: Please checkt this command from BLE Receiver: " + sb.toString());
							sb.delete(0, sb.length());
						}
					}
				}
			} catch(Exception ex) {
				ex.printStackTrace();
			}
	
		} /* ----------- while */
	}

	public void sendStart() {
		Gateway.instance.sendTextFrame(Gateway.instance.out, Constant.SOP + Constant.START + Constant.SEP + Gateway.instance.getTerminalId() + Constant.EOP);
	}

	public void sendStop() {
		Gateway.instance.sendTextFrame(Gateway.instance.out, Constant.SOP + Constant.STOP+ Constant.EOP);
	}
	private void parseResponse(String response) throws Exception {
		myDebug("received from BLE receiver : " + response);
		String cmd = response.substring(Constant.SOP.length(), response.length()-Constant.EOP.length());
		String[] cmdArr = cmd.split(Constant.SEP);
		switch(cmdArr[0]) {
			case Constant.START:
				if (cmdArr.length >1 && cmdArr[1].equals(Constant.OK)) {
					myDebug("Start OK");
					isBypass = true;
				} else {
					myDebug("Start NG"); //Already Started
					isBypass = true;
				}
				break;
			case Constant.STOP:
				if (cmdArr.length >1 && cmdArr[1].equals(Constant.OK)) {
					mode = MODE_WRITE;
					writeCmd = Command.START;
					isBypass = false;
					myDebug("Stop OK");
				} else { // Alreaday stopped
					mode = MODE_WRITE;
					writeCmd = Command.START;
					isBypass = false;
					myDebug("Stop NG"); //Already Stopped
				}
				break;
			case Constant.CONNECT:
			case Constant.DISCONNECT:
				break;
			case Constant.CDATA:
				CdataPacket cdataPacket = new CdataPacket(cmdArr[1], Integer.parseInt(cmdArr[2]), Integer.parseInt(cmdArr[3]),
						response.substring(Constant.SOP.length() +
											Constant.CDATA.length() + Constant.SEP.length() +
											cmdArr[1].length() + Constant.SEP.length() +
											cmdArr[2].length() + Constant.SEP.length() +
											cmdArr[3].length() + Constant.SEP.length() , response.length()-Constant.EOP.length()));
				collectCdataPacket(cdataPacket);
				break;
			default:
				if (!isBypass) {
					serialTimer = System.currentTimeMillis();
					invalidCnt++;
					myDebug("[Unknown Command] " + cmdArr[0] + ":" + invalidCnt);
					if (invalidCnt > 3) {
						mode = MODE_WRITE;
						writeCmd = Command.STOP;
						invalidCnt = 0;
					}
				} else { //Bypass
					writeCmd = Command.STOP;
				}
		}
	}

	public void sendAck(Command cmd, Ack ack) throws Exception {
		String datetime = LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		String ackResponse = Constant.SOP + cmd.getName() + Constant.SEP + ack.getName() + Constant.SEP + Constant.TIME + Constant.SEP + datetime + Constant.EOP;
		sendTextFrame(out, ackResponse);
	}

	private void sendCdataToMms(CdataPacket cdata) throws Exception {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("readtime", getNow());
		jsonObject.put("data", cdata.getData());
		updateRawdata(jsonObject.toString());
	}
	private void collectCdataPacket(CdataPacket cdataPacket) throws Exception {
		myDebug(cdataPacket.toString());
		if (cdataPacket.getIndex() == 1) {
			counterCdataMap.remove(cdataPacket.getCounterId());
			if (cdataPacket.getIndex() == cdataPacket.getTotal()) {
				sendCdataToMms(cdataPacket);
			} else {
				counterCdataMap.put(cdataPacket.getCounterId(), cdataPacket);
			}
			mode = MODE_WRITE;
			writeCmd = Command.CDATA;
		} else {
			CdataPacket cdata = counterCdataMap.get(cdataPacket.getCounterId());
			if (cdata == null) {
				sendAck(Command.CDATA, Ack.NG);
				throw new Exception("The previous cdata packet is null : Please check protocols...");
			}
			if (cdataPacket.getIndex() != cdata.getIndex()+1 || cdataPacket.getTotal() != cdata.getTotal()) {
				sendAck(Command.CDATA, Ack.NG);
				throw new Exception("The cdata packet oder is broken...");
			}
			cdata.setIndex(cdataPacket.getIndex());
			cdata.setData(cdata.getData() + cdataPacket.getData());
			myDebug("cdata : " + cdata.getData());
			counterCdataMap.put(cdata.getCounterId(), cdata);

			if (cdata.getIndex() == cdata.getTotal()) {
				sendCdataToMms(cdata);
			}
			mode = MODE_WRITE;
			writeCmd = Command.CDATA;
		}
	}
	private StringBuffer _lastStr = new StringBuffer();
	public void myDebug(String str) {
		if( !_lastStr.toString().equals(str)) {
			System.out.println(MYDBG +LocalDateTime.now().format(DateTimeFormatter.ofPattern(" yyyy-MM-dd HH:mm:ss.SSS "))+ str);
			_lastStr.delete(0, _lastStr.length());
			_lastStr.append(str);		
		}
		
		saveLog(_lastStr);
		
	}
	
	private String lastDay = "";
	String currDir = "";
	
	public void saveLog(StringBuffer strbuffer) {
		
		if(currDir.isEmpty()) {
			currDir = System.getProperty("user.dir");
			System.out.println("Log save path = "+ currDir);
		}
		
		if(lastDay.isEmpty()) {
			lastDay = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
			System.out.println("Log save date = "+ lastDay);
		}
		
		String toDay = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		if(!toDay.equals(lastDay)) {
			// 파일명 변경. 
			lastDay = new String(toDay);
		}
		
		if(!isSaveLogfile()) {
			return;
		}
		
		
		try {
			File file = new File(currDir +"/"+ this._hostname + "_" + lastDay+".txt");
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true));
			
			if(file.isFile() && file.canWrite()){
				
                //쓰기
                bufferedWriter.write(strbuffer.toString());
                //개행문자쓰기
                bufferedWriter.newLine();
                
                bufferedWriter.close();
            }
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
		
	}

	public String byteArrayToHexString(byte[] bytes) {

		StringBuilder sb = new StringBuilder();

		for(byte b : bytes){ 
			
			sb.append(String.format("%02X", b&0xff)); 
		} 

		return sb.toString();
	}
	
	public String byteArrayToHexString(byte[] bytes, int len) {

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < len; i++) {
			sb.append(String.format("%02X", bytes[i] & 0xff));
		}
//		for(byte b : bytes){ 
//			
//			sb.append(String.format("%02X", b&0xff)); 
//		} 

		return sb.toString();
	}

	public byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	// 문자열을 헥사 스트링으로 변환하는 메서드
	public String stringToHex(String s) {
		String result = "";

		for (int i = 0; i < s.length(); i++) {
			result += String.format("%02X", (int) s.charAt(i));
		}

		return result;
	}
	
	public int createSendFrame(OutputStream out,  byte cmd, byte[] data) {
    	
    	CrC16Modbus crcModbus = new CrC16Modbus();
    	
    	
    	int ret = 0;
    	byte[] writeFrame = new byte[1+2+1+data.length+2+1];
    	
    	// STX
    	writeFrame[ret++] = STX;
    	
    	// length 2
    	int dataLen = data.length;
    	dataLen &= 0xFFFF;
    	
    	writeFrame[ret++] = (byte)((dataLen & 0xFF00) >> 8);
    	writeFrame[ret++] = (byte)(dataLen & 0xFF);
    	
    	// cmd
    	writeFrame[ret++] = cmd;
    	
    	for(int i = 0; i < dataLen; i++) {
    		writeFrame[ret++] = data[i];
    	}
    	
//    	byte[] crc = crcModbus.fn_makeCRC16(writeFrame,0, ret);
    	int[] crc = crcModbus.calculateCRC(writeFrame,0, ret);
    	
    	writeFrame[ret++] = (byte) crc[0];
    	writeFrame[ret++] = (byte) crc[1];
    	
    	writeFrame[ret++] = ETX;
    	
    	myDebug("[write] send frame = 0x"+byteArrayToHexString(writeFrame));
    	try {
			out.write(writeFrame);
		} catch (Exception e) {
			e.printStackTrace();
			ret = -1;
		}
     	
    	return ret;
    }

	public void sendTextFrame(OutputStream out,  String cmdLine) {

		CrC16Modbus crcModbus = new CrC16Modbus();
		myDebug("[write] send frame = " + cmdLine);

		try {
			out.write(cmdLine.getBytes());
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
	}
	
	public String getNowTime() {
		return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
	}
	
	public String getNow() {
		return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
	}
	
	public int compareYear(LocalDateTime date1, LocalDateTime date2) {
		LocalDateTime dayDate1 = date1.truncatedTo(ChronoUnit.YEARS);
		LocalDateTime dayDate2 = date2.truncatedTo(ChronoUnit.YEARS);
		int compareResult = dayDate1.compareTo(dayDate2);
		return compareResult;
	}
	
	public int compareMonth(LocalDateTime date1, LocalDateTime date2) {
		LocalDateTime dayDate1 = date1.truncatedTo(ChronoUnit.MONTHS);
		LocalDateTime dayDate2 = date2.truncatedTo(ChronoUnit.MONTHS);
		int compareResult = dayDate1.compareTo(dayDate2);
		return compareResult;
	}
	
	public int compareDays(LocalDateTime date1, LocalDateTime date2) {
		LocalDateTime dayDate1 = date1.truncatedTo(ChronoUnit.DAYS);
		LocalDateTime dayDate2 = date2.truncatedTo(ChronoUnit.DAYS);
		int compareResult = dayDate1.compareTo(dayDate2);
		return compareResult;
	}
	
	public int compareHour(LocalDateTime date1, LocalDateTime date2) {
		LocalDateTime dayDate1 = date1.truncatedTo(ChronoUnit.HOURS);
		LocalDateTime dayDate2 = date2.truncatedTo(ChronoUnit.HOURS);
		int compareResult = dayDate1.compareTo(dayDate2);
		return compareResult;
	}
	
	public int compareMinute(LocalDateTime date1, LocalDateTime date2) {
		LocalDateTime dayDate1 = date1.truncatedTo(ChronoUnit.MINUTES);
		LocalDateTime dayDate2 = date2.truncatedTo(ChronoUnit.MINUTES);
		int compareResult = dayDate1.compareTo(dayDate2);
//		System.out.println("=== 시간 단위 비교 ===");
//		System.out.println("date1.truncatedTo(ChronoUnit.HOURS) : " + dayDate1);
//		System.out.println("date2.truncatedTo(ChronoUnit.HOURS) : " + dayDate2);
//		System.out.println("결과 : " + compareResult);
		return compareResult;
	}

		
	
	
	public int sendREST(String sendUrl, String jsonValue) throws IllegalStateException {
		
		myDebug("[server] url = "+ sendUrl +", data = " + jsonValue);
		
		int ret = 404;
		String inputLine = null;
		StringBuffer outResult = new StringBuffer();

		try {
			myDebug("[server] REST API Start");
			
			URL url = new URL(sendUrl);
			
			if(url.getProtocol().equals("https")) {
				// https
				

				TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

					@Override
					public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
						
					}

					@Override
					public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
						
					}

					@Override
					public X509Certificate[] getAcceptedIssuers() {
						return null;
					}

				}};
				
				
				// install the all-trusting trust manager
				SSLContext sc = SSLContext.getInstance("SSL");
				sc.init(null, trustAllCerts, new SecureRandom());
				HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
				
				// Create all-trusting host name verifier
				HostnameVerifier allHostsValid = new HostnameVerifier() {

					@Override
					public boolean verify(String hostname, SSLSession session) {
						return true;
					}
				};
				
				// Install the all-trueting host verifier
				
				HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
				
				
				
				HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
				
//				myDebug("HttpsURLConnection conn="+conn.);
				
								
				conn.setDoOutput(true);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/json");
				conn.setRequestProperty("Accept-Charset", "UTF-8");
				conn.setConnectTimeout(10000);
				conn.setReadTimeout(10000);
				
				OutputStream os = conn.getOutputStream();
				os.write(jsonValue.getBytes("UTF-8"));
				os.flush();

				// 리턴된 결과 읽기
				BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
				while ((inputLine = in.readLine()) != null) {
					outResult.append(inputLine);
				}
				
				ret = conn.getResponseCode();

				conn.disconnect();
				
			} else {
				// http
				
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();


				conn.setDoOutput(true);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/json");
				conn.setRequestProperty("Accept-Charset", "UTF-8");
				conn.setConnectTimeout(10000);
				conn.setReadTimeout(10000);
				/******
				 * 인증이 있는경우 String id_pass = "id:password"; String base64Credentials = new
				 * String(Base64.getEncoder().encode(id_pass.getBytes()));
				 * conn.setRequestProperty("Authorization", "Basic " + base64Credentials);
				 */

				OutputStream os = conn.getOutputStream();
				os.write(jsonValue.getBytes("UTF-8"));
				os.flush();

				// 리턴된 결과 읽기
				BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
				while ((inputLine = in.readLine()) != null) {
					outResult.append(inputLine);
				}
				
				ret = conn.getResponseCode();

				conn.disconnect();
				
			}
			
			myDebug("[server] REST API End : ret code = "+ret);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	private String getServerIp() {
		
		InetAddress local = null;
		try {
			local = InetAddress.getLocalHost();
		}
		catch ( UnknownHostException e ) {
			e.printStackTrace();
		}
			
		if( local == null ) {
			return "";
		}
		else {
			String ip = local.getHostAddress();
			return ip;
		}
			
	}
	
	public void updateRawdata(String data) {
		
		JSONObject json = new JSONObject(data);
		
		// 1. 일단 DB 에 넣고. 
		if( !dba.insertRecvData(json)) {
			myDebug("[updateRawdata] data already exist!");
		};
		
		// 2. 전송안된 데이터 뽑아서 전송. 
		JSONArray jsonarray = dba.getLastdata();
		
		myDebug(jsonarray.toString());
		
		myDebug("aa="+(new JSONArray(jsonarray.toString())).toString());
		
		
		 // send to server
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("terminal_id", getTerminalId());
		
		
		
		jsonObject.put("rawdata", jsonarray);

		new Runnable() {
			@Override
			public void run() {
				int ret = sendREST(getServerUrl()+SUBURL_DATA, jsonObject.toString());

				if( ret != 200) {

					myDebug("[updateRawdataArray] Failed to connect to server !!!!!!!!!!!!!!!!!!!!!!!!!!!");

					dba.updateRecvData(jsonarray, false);

				} else {
					System.out.println("updateRecvData="+dba.updateRecvData(jsonarray, true));

					// Update

					System.out.println("[updateSensorReadTime] : "+dba.updateSensorReadTime(Integer.toString(get_sensorNo()), json.get("readtime").toString()));
					serverTimer = System.currentTimeMillis();
				}
			}
		}.run() ;

	}
	
	
	public void reportHeartbeat() {
	
		
		// /mms/data
		
		JSONObject jsonObject = new JSONObject();
		JSONObject items;
		
		items = new JSONObject();
		items.put("id", getTerminalId());
		items.put("sw_ver", VERSION);
		jsonObject.put("terminal", items);
		
		items = new JSONObject();
		items.put("type", "unknown");
		items.put("ip", getServerIp());
		jsonObject.put("network", items);
		
		items = new JSONObject();
		
		items.put("list", dba.getSensorList());
		jsonObject.put("sensor", items);

		new Runnable() {
			@Override
			public void run() {
				int retCode = sendREST(getServerUrl()+SUBURL_HEARTBEAT, jsonObject.toString());

				myDebug("[reportHeartbeat] "+jsonObject.toString());
				myDebug("[reportHeartbeat] Send : "+(retCode == 200 ? "Success" : "Fail"));
			}
		}.run() ;
	}
	
	

	public static void main(String[] args) throws UnknownHostException {

		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("*---------------------------------- Emoldino Gateway version "+VERSION+" ---------------------------------------*");
		
		if (args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				System.out.println("arg[" + i + "] " + args[i]);
			}
		}

		if (args.length >= 2) {
			Gateway gw = new Gateway(args[0], args[1], args[2], args[3], args[4]);
			gw.run();
		} else {
			
			System.out.println(" ");
			System.out.println("* usage : Gateway [serialPort] [serialWatchdog] [serverUrl] [serverWatchdog] [log save true/false]");
			 
			System.out.println(" ");
			System.out.println("*--------------------------------------------------------------------------------------------------------*");
			System.out.println(" ");
			System.out.println(" ");
			System.out.println(" ");
			System.out.println(" ");
			System.out.println(" ");
			
		}
	}

}

