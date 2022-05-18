package com.emoldino.demo.gateway;

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
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fazecast.jSerialComm.SerialPort;


public class Gateway extends Thread {
	
	private final static String MYDBG = "[Emoldino]";
	
	private final static String VERSION = "1.1.2";
	
	private final static String SUBURL_SENSOR = "/mms/data/seneor";
	private final static String SUBURL_DATA = "/mms/data";
	private final static String SUBURL_HEARTBIT = "/mms/data/heartbit";
	
	
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
		this._terminalId = terminalId;
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
	
	public String _hostname;

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
			comPort = SerialPort.getCommPort(serialPort);

			comPort.setBaudRate(Integer.parseInt("115200"));
			comPort.openPort();
			comPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);

			// 입력 스트림
			in = comPort.getInputStream();

			// 출력 스트림
			out = comPort.getOutputStream();

			myDebug("[OK]serialPort is Opened : " + serialPort);

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		setServerUrl(serverUrl);
		
		crcModbus = new CrC16Modbus();
		dba = new Dba();


	}

	@Override
	public boolean isInterrupted() {
		// TODO Auto-generated method stub
		return super.isInterrupted();
	}

	@Override
	public void run() {
		

		long serialTimer = 0; // System.currentTimeMillis();
		long serverTimer = 0; // System.currentTimeMillis();

		int pos = 0;
		
		StringBuffer sb = new StringBuffer();
		
//		int sensorNo = 0;
		int sendLen = 0;
		
        byte writeCmd = CMD_SET_TIME;
        byte readStatus = DATA_FAULT;
        
        boolean isSensorOpened = false;	// 센서 열린 상태 
        
        
        
		// Debug;
//        String strRow = "02008C3043444154412F53435F303030303030332F32303231303130313030303435352F32303231303130313030303530382F30303030382F392F342F303236302F303030382F30303032303030302F6261626162332F41444154412F53435F303030303030332F30303030312F32303231303130313030303530332F302E302C302E3034352C302E352C302E303432E0FD03";
//		sb.append(strRow);
             
             
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
				
				reportHeartbit();
				
			}
			
			
			switch(mode) {
				default:
				case MODE_WRITE: {
					
					// Set Time
					
					
					switch(writeCmd) {
						default:
						case CMD_SET_TIME: {
							
							String nowtime = getNowTime();
							byte[] noetime_b = nowtime.getBytes();
							for(int i = 0; i < noetime_b.length; i++) {
								noetime_b[i] -= 0x30;
							}
							
							myDebug("[write] send cmd = CMD_SET_TIME");
							sendLen = createSendFrame( this.out, CMD_SET_TIME, noetime_b);
							set_serialWatchdog(20000);
							
						}
						break;
						
						case CMD_OPEN: {
							byte[] sendCmd = new byte[1];
							sendCmd[0] = get_sensorNo();
							
							myDebug("[write] send cmd = CMD_OPEN " + get_sensorNo());
							sendLen = createSendFrame( this.out, CMD_OPEN, sendCmd);
							set_serialWatchdog(20000);
							
							
							isSensorOpened = false;
						
						}
						break;
						
						case CMD_READ: {
							byte[] sendCmd = new byte[1];
							sendCmd[0] = get_sensorNo();
							
							myDebug("[write] send cmd = CMD_READ " +get_sensorNo());	
							sendLen = createSendFrame( this.out, CMD_READ, sendCmd);
							set_serialWatchdog(20000);
						
						}
						break;
						
						case CMD_CLOSE: {
							
							byte[] sendCmd = new byte[1];
							sendCmd[0] = get_sensorNo();
							
							myDebug("[write] send cmd = CMD_CLOSE " +get_sensorNo());	
							sendLen = createSendFrame( this.out, CMD_CLOSE, sendCmd);
							set_serialWatchdog(20000);
						
						}
						break;
						
						case CMD_INCRESE_ID: {
							byte sno = get_sensorNo();
							if(  sno++ >= 5) {
								sno = 1;
							} 
							
							set_sensorNo(sno);
							
							myDebug("[write] send cmd = CMD_INCRESE_ID " +get_sensorNo());	
						}
						case CMD_READ_COUNT: {
							
							byte[] sendCmd = new byte[1];
							sendCmd[0] = get_sensorNo();
							
							myDebug("[write] send cmd = CMD_READ_COUNT " +get_sensorNo());	
							sendLen = createSendFrame( this.out, CMD_READ_COUNT, sendCmd);
							set_serialWatchdog(60000);
						
						}
						break;
						//
						case CMD_READ_COMPLETE: {
							
							byte[] sendCmd = new byte[1];
							sendCmd[0] = readStatus;
							
							myDebug("[write] send cmd = CMD_READ_COMPLETE " +get_sensorNo());	
							sendLen = createSendFrame( this.out, CMD_READ_COMPLETE, sendCmd);
							set_serialWatchdog(1000);
							
							// CMD_CLOSE로 전환을 위해 강제로 1조산 쉰다. 
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								// 루프를 중단하기 위한 인터럽트 였으나,
								// sleep중 인터럽트가 발생하여 sleep만 빠져나가고 메인 루프는 계속 실행됨.
								// 플레그를 세팅하여 빠져 나오게 함.
								set_bExit(true);
								e.printStackTrace();
							}
						}
					}
					
//					myDebug("[write] send len = "+sendLen);
					System.out.println("");
					
					
					mode = MODE_READ;
					serialTimer = System.currentTimeMillis(); 
					
				}
				break;
				
				/**
				 * 수신 시퀀스 -----------------------------------------------------
				 */
				case MODE_READ: {
					
					// 수신 와치독. 모드 변
					if (System.currentTimeMillis() - serialTimer > get_serialWatchdog()) {
						
						myDebug("[read] Time out!! -----------------");
						System.out.println("");
						
						mode = MODE_WRITE;
						
						continue;
					}
					
					readStatus = DATA_FAULT;
					
					
					// 데이터 수신. 
					byte[] serialBuffer = null;
					try {
						int recvLen = this.in.available();
						if(recvLen > 0) {
							serialBuffer = new byte[recvLen];
							int readLen = this.in.read(serialBuffer);
							
							sb.append(byteArrayToHexString(serialBuffer));
//							myDebug("[read] InputStream = 0x"+byteArrayToHexString(serialBuffer));
//							myDebug("[read] recvLen = "+recvLen+", readLen = "+readLen);
							
							serialTimer = System.currentTimeMillis(); 
							
						}
						
					} catch (IOException e) {
						e.printStackTrace();
						
						mode = MODE_WRITE;
					} finally {
						serialBuffer = null;
					}
					
					
					
					
					
					//  최소 길이 확인. 
					if(sb.length() < 8*2) {

						continue;
					}
					
					// 적재된거 파싱.
					String stxLen = sb.substring(0, 3*2);
					byte[] byteStxLen = hexStringToByteArray(stxLen);
					
					pos = 0;
					
					// Get STX
					byte stx = byteStxLen[pos++];
					if( stx != STX) {
						myDebug("[read] STX Error = "+ stx+"<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
						sb.deleteCharAt(0);
						sb.deleteCharAt(0);
						
						continue;
					}
					myDebug("[read] STX OK!");
					
					// Get Length
					
				
					
					int firstI = (byteStxLen[pos++] & 0xFF) << 8;
					int secondI = (byteStxLen[pos++] & 0xFF);					
					int dataLen = firstI | secondI ;
					myDebug("[read] Data Length = "+dataLen);
					
					if(sb.length()/2 < LEN_STX + LEN_LENGTH + LEN_CMD + dataLen + LEN_CRC + LEN_ETX ) { // stx + len 2 + cmd + datalen + crc + etx
						
						myDebug("[read] ERROR! buffer length("+sb.length()/2+") < frame length("+dataLen + 1+2+1+2+1+")"+"<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
//						myDebug("[read] ERROR! Framedata = 0x"+sb.toString());
						continue;
					}
					
					//  전체 프레임 획득. 
					String strFrame = sb.substring(0, (LEN_STX + LEN_LENGTH + LEN_CMD + dataLen + LEN_CRC + LEN_ETX)*2);
					byte[] frame = hexStringToByteArray( strFrame );
//					myDebug("[read] frame = 0x"+byteArrayToHexString(frame));
					
					// Get CMD
					int recvCmd = frame[pos++];
					myDebug("[read] recvCmd = 0x"+ Integer.toHexString(recvCmd));
					
					// Get Data Frame
					byte[] data = Arrays.copyOfRange(frame, pos, pos+dataLen);
					pos += dataLen;
//					myDebug("[read] data : hex = 0x" + byteArrayToHexString(data));
					
					
					// Get CRC
					byte[] crcFrame = Arrays.copyOfRange(frame, 0, (pos+2));
//					myDebug("[read] crcFrame = 0x"+byteArrayToHexString(crcFrame));
					
					int[] crcByte = crcModbus.calculateCRC(crcFrame, 0, crcFrame.length);
					
					if( crcByte[2] != 0x0000 ) {
						
						myDebug("[read] CRC Error! 0x" + Integer.toHexString(crcByte[1])+Integer.toHexString(crcByte[0])+"<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
						// CRC 에러 이므로 1바이트 쉬프팅 
//						sb.deleteCharAt(0);
//						sb.deleteCharAt(0);
//						continue;
					} else {
						myDebug("[read] CRC OK!");
					}
					pos+=2;
					
					
					// Get ETX
					if(frame[pos++] != ETX) {
						myDebug("[read] ERROR ETX!!"+"<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
						// 우연히 들어맞은 것이므로 1바이트 쉬프팅 
						sb.deleteCharAt(0);
						sb.deleteCharAt(0);
						continue;
					} else { 
						myDebug("[read] ETX OK!");
					}
					
					// 데이터 정상적으로 수신. 
					readStatus = DATA_OK;
					
					// 수신이 잘 되었으므로 
					// 명령따라 데이터 파싱. --> data
					myDebug("[read] CMD = "+recvCmd+", data length="+dataLen);
					switch(recvCmd) {
						default: {
							myDebug("[read] Undefined CMD : 0x" + Integer.toHexString(recvCmd));
							writeCmd = CMD_INCRESE_ID;
						}
						break;
						
						case CMD_SET_TIME: {
							//  next open
							if(data[0] == DATA_OK) {
								writeCmd = CMD_INCRESE_ID;
								myDebug("[read] > CMD_SET_TIME : OK --> CMD_INCRESE_ID");
							} else {
								myDebug("[read] > CMD_SET_TIME : FAULT --> Retry Set Time");
							}
							
						}
						break;
						
						case CMD_READ_COUNT: {
							
							if(dataLen >= 2) {

								int fI = (data[0] & 0xFF) << 8;
								int sI = (data[1] & 0xFF);					
								int readCount = fI | sI ;
								myDebug("[read] CMD_READ_COUNT = "+readCount);
								
								if(readCount > 0) {
									if(isSensorOpened == false) {
										writeCmd = CMD_OPEN;
									} else {
										writeCmd = CMD_READ;
									}
								} else {
									writeCmd = CMD_INCRESE_ID;									
								}
								
								if(dataLen == 16) {
									// 날짜 비교.
									// Get Receiver Time 
									byte[] r_date = Arrays.copyOfRange(data, 2, 16);
									for(int i = 0; i<r_date.length;i++) {
										r_date[i] += 0x30;
									}
									String recv_date = new String(r_date);
									myDebug("[read] CMD_READ_COUNT : recv_date="+recv_date);
									
									try {
										Date recvDate = new SimpleDateFormat("yyyyMMddHHmmss").parse(recv_date);
										Date nowDate = new Date();
										
										long recv = recvDate.getTime();
										long now = nowDate.getTime();
										
										if(Math.abs(now - recv) > 60000) {
											myDebug("[read] CMD_READ_COUNT : Delta time = " + Math.abs(now - recv)+"msec, Now Init Receiver Time");
											writeCmd = CMD_SET_TIME;									
											
										}
										
									} catch (ParseException e) {
	
										myDebug(e.toString());
										myDebug("[read] CMD_READ_COUNT : Time data is NULL ");
									}
								}
							} else {
								myDebug("[read] CMD_READ_COUNT : Data length is mismatch : " + dataLen+", ---> Next write cmd is CMD_READ_COUNT");
								writeCmd = CMD_INCRESE_ID;
							}
							
						}
						break;
						
						case CMD_OPEN: {
							// next read							
							// success 
							if(data[0] == DATA_OK) {
								myDebug("[read] > CMD_OPEN : OK" );
								writeCmd = CMD_READ;
								
								isSensorOpened = true;
								
							} else {
								myDebug("[read] > CMD_OPEN : FAULT");
								writeCmd = CMD_CLOSE;
								
								isSensorOpened = false;
							}
							
						}
						break;
						
						case CMD_READ: {
							// next close
							if(data.length <= 1) {
								
								if(data[0] == DATA_OK) {
									myDebug("[read] > CMD_READ : DATA_OK --> CMD_CLOSE" );
									writeCmd = CMD_CLOSE;
								} else {
									// 읽을 데이터가 없으므로 CLOSE
									myDebug("[read] > CMD_READ : DATA_FAULT --> CMD_CLOSE" );
									writeCmd = CMD_CLOSE;
								}
								
								
							} else {
								
								/* Ver 1 */
								String strData = new String(data);	// 넣을 데이터. 
								
								/* Ver 2 */
								JSONObject jsonObject = new JSONObject();
								jsonObject.put("readtime", getNow());
								jsonObject.put("data", strData);
								updateRawdata(jsonObject.toString());
								
								writeCmd = CMD_READ_COMPLETE;
							}
							
						}
						break;
						
						case CMD_CLOSE: {
							// next id up-count
							myDebug("[read] > CMD_CLOSE : " + (data[0] == DATA_OK ? "OK" : "FAULT"));
							
							isSensorOpened = false;
							
							writeCmd = CMD_INCRESE_ID;
						}
						break;
						
						
						
						case CMD_RECV_ALL: {
							// next close
							if(data.length <= 1) {
								myDebug("[read] > CMD_RECV_ALL : " + (data[0] == DATA_FAULT ? "FAULT" : ("undefined : "+ data[0])) );
							} else {
								
								serialTimer = System.currentTimeMillis(); 
								
								String strData = new String(data);	// 넣을 데이터. 
								updateRawdata(strData);
								
								writeCmd = CMD_READ_COMPLETE;
							}
						}
						break;
						
						case CMD_READ_COMPLETE: {
							myDebug("[read] > CMD_READ_COMPLETE : " + (data[0] == DATA_OK ? "OK" : "FAULT"));
							writeCmd = CMD_READ;					
						}
						break;
					
					}
					
					
					
					// 성공하면 해당   길이만큼 지우고 모드 변경
					sb.delete(0, pos*2);
					
					if(sb.length() > 0) {
						//파싱할 데이터가 남아 있으므로 와치독 초기화 
						serialTimer = System.currentTimeMillis(); 
						set_serialWatchdog(30000);	//	30 초 더 기다려 본다. 
						
						myDebug("[read] There is still data left. Init read Watchdog");
						myDebug("[read] buffer len = "+ sb.length()/2);
						myDebug("-----------------------------------");
						
					} else {
						// 와치독 초기화해서 MODE_WRITE 로 전환 
						set_serialWatchdog(getDefault_serialWatchdog());	//
					}
					
					
				}
				break;
			
			}
	
		} /* ----------- while */
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
	
	public int updateRawdata(String data) {
		int ret = -1;
		
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
		
		ret = sendREST(getServerUrl()+SUBURL_DATA, jsonObject.toString()); 
		
		if( ret != 200) {
			
			myDebug("[updateRawdataArray] Failed to connect to server !!!!!!!!!!!!!!!!!!!!!!!!!!!");
			
			dba.updateRecvData(jsonarray, false); 
			
		} else {
			System.out.println("updateRecvData="+dba.updateRecvData(jsonarray, true));
			
			// Update
			
			System.out.println("[updateSensorReadTime] : "+dba.updateSensorReadTime(Integer.toString(get_sensorNo()), json.get("readtime").toString()));
		}
		
		return ret;
	}
	
	
	public boolean reportHeartbit() {
		boolean ret = false;;
	
		
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
		
		
		int retCode = sendREST(getServerUrl()+SUBURL_HEARTBIT, jsonObject.toString()); 
		ret = (retCode == 200 ? true : false);

		myDebug("[reportHeartbit] "+jsonObject.toString());
		myDebug("[reportHeartbit] Send : "+(ret == true ? "Success" : "Fail"));
		
		return ret;
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
		
		
		

		/* Debug -------------------------------------------------------------------------------- */
		
//		try {
////			Gateway gw = new Gateway("COM3", "3000", "http://49.247.200.147", "60000", "true");
//			Gateway gw = new Gateway("COM3", "3000", "https://dev-feature.emoldino.com", "60000", "true"); 
////			gw.run();
//			StringBuffer sb = new StringBuffer();
//			sb.append("test1");
//			gw.saveLog(sb);
//			
//			
//			StringBuffer sb2 = new StringBuffer();
//			sb2.append("test2");
//			gw.saveLog(sb2);
//			
//		} catch (UnknownHostException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
		
		
		
		
//		Gateway gw = new Gateway();
//		gw.setTerminalId("Test1-JB-terminal");
//		gw.setServerUrl("https://dev-feature.emoldino.com");
////		gw.setServerUrl("http://49.247.200.147");
//		
//		gw.reportHeartbit();
		
		
		
//		JSONObject jsonObject = new JSONObject();
//		jsonObject.put("readtime", gw.getNow());
//		jsonObject.put("data", "CDATA/SC_0000004/20211203180545/20211203190545/00009/9/4/042204170388036503480336/0140/09560958/b6ab/ADATA/SC_0000004/00001/20211203181344/0.5,0.031,2.6,0.053,3.1,0.022,5.2,0.019,80.9,0.000,81.1,0.027,81.4,0.033/TEST/0.0/81.8/95.8/177.6/191.6/273.4/287.4/369.2/383.2/465.0/479.0/560.8/574.8/656.7/670.4/752.4/766.2/848.2");
//		gw.updateRawdata(jsonObject.toString());
		
	

	}

}

