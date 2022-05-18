package com.emoldino.demo.gateway;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

public class Dba {
	
	static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver"; // jdbc 드라이버 주소
	static final String DB_URL = "jdbc:mysql://localhost:3306/emoldino?allowPublicKeyRetrieval=true&useSSL=false"; // DB 접속
	
	static final String USERNAME = "emoldino"; // DB ID
	static final String PASSWORD = "!emoldino"; // DB Password
	
	
	public Dba() {
		
	}
	
	public JSONArray getLastdata() {
		
		JSONArray ret = new JSONArray();
		
		Connection dbConn = null;
		Statement dbState = null;
		try {
			Class.forName(JDBC_DRIVER);
			dbConn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
			dbState = dbConn.createStatement();

			String sql = "select READ_DATE, RAWDATA from TB_RAW where DELIVERED = 0";
			ResultSet rs = dbState.executeQuery(sql);
			
			while (rs.next()) {
				JSONObject json = new JSONObject();
				String READ_DATE = rs.getString("READ_DATE");
//				System.out.println(READ_DATE);
				json.put("readtime", READ_DATE);
				
				
				String RAWDATA = rs.getString("RAWDATA");
//				System.out.println(RAWDATA);
				json.put("data", RAWDATA);
				
				ret.put(json);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			if (dbConn != null) {
				try {
					dbConn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}		
		
		
//		System.out.println("ret="+ret);
		
		
		return ret;
	}
	
	/** String 은 JSON */
	public boolean insertRecvData(JSONObject jsondata) {
		boolean ret =false;
		
		Connection dbConn = null;
		PreparedStatement pstmt = null;
		
		try {
			Class.forName(JDBC_DRIVER);
			dbConn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
			
			
			// 데이터 추가.  
			String sql = "insert into TB_RAW( RAWDATA, READ_DATE) values (?,?)";

			pstmt = dbConn.prepareStatement(sql);
			
				
			pstmt.setString(1, jsondata.getString("data"));
			pstmt.setString(2, jsondata.getString("readtime"));
			
			try {
			
				ret = (pstmt.executeUpdate() == 0 ? false: true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			

			dbConn.close();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			if (dbConn != null) {
				try {
					dbConn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		return ret;
	}

	public int updateRecvData(JSONArray jsonarray, boolean isSended ) {
		int ret =0;
		
		Connection dbConn = null;
		PreparedStatement pstmt = null;
		
		try {
			Class.forName(JDBC_DRIVER);
			dbConn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
			
			
			// 데이터 추가.  
			// String sql = "update emoldino.TB_SENEOR set READ_TIME = ? where SENSOR_ID = ?";
			String sql = "update TB_RAW set DELIVERED=? where READ_DATE=?";

			pstmt = dbConn.prepareStatement(sql);
			
			for(int i = 0; i < jsonarray.length(); i++ ) {
				
				JSONObject json = jsonarray.getJSONObject(i);
				pstmt.setBoolean(1, isSended);
				pstmt.setString(2, json.getString("readtime"));
				
				ret += pstmt.executeUpdate();
			}

			dbConn.close();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			if (dbConn != null) {
				try {
					dbConn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		return ret;
	}
	

	
	
	
	
	/***
	 * 센서 목록을 READ_TIME 이 오래된 순서로 조회한다. 
	 * @return
	 */
	public JSONArray getSensorList() {
		
		JSONArray ret = new JSONArray();
		
		Connection dbConn = null;
		Statement dbState = null;
		try {
			Class.forName(JDBC_DRIVER);
			dbConn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
			dbState = dbConn.createStatement();

			String sql = "select SENSOR_ID, READ_TIME, REG_DATE from TB_SENSOR order by READ_TIME ASC";
			ResultSet rs = dbState.executeQuery(sql);
			
			while (rs.next()) {
				JSONObject json = new JSONObject();
				json.put("id", rs.getString("SENSOR_ID"));
				json.put("last_read", rs.getString("READ_TIME"));
//				json.put("REG_DATE", rs.getString("REG_DATE"));
				
				ret.put(json);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			if (dbConn != null) {
				try {
					dbConn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}		
		
		return ret;
	}
	
	boolean updateSensorReadTime(String sensor_id, String readTime) {
		boolean ret = false;
		
		Connection dbConn = null;
		PreparedStatement pstmt = null;
		
		try {
			Class.forName(JDBC_DRIVER);
			dbConn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
			
			String sql = "update TB_SENSOR set READ_TIME = ? where SENSOR_ID = ?";

			pstmt = dbConn.prepareStatement(sql);
			pstmt.setString(1, readTime);
			pstmt.setString(2, sensor_id);
			
			ret = (pstmt.executeUpdate() == 1 ? true : false);
							

			dbConn.close();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			if (dbConn != null) {
				try {
					dbConn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
		return ret;
	}
	
	boolean deleteSensorId(String sensor_id) {
		boolean ret = false;
		
		Connection dbConn = null;
		PreparedStatement pstmt = null;
		
		try {
			Class.forName(JDBC_DRIVER);
			dbConn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
			
			String sql = "delete from TB_SENEOR where SENSOR_ID = ?";
			
			pstmt = dbConn.prepareStatement(sql);
			pstmt.setString(1, sensor_id);
			
			ret = (pstmt.executeUpdate() == 1 ? true : false);
							

			dbConn.close();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			if (dbConn != null) {
				try {
					dbConn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		return ret;
	}
	
	public int insertSensorList(ArrayList<String> sensorList) {
		int ret = 0;
		
		ArrayList<String> old_sensorList = new ArrayList<String>();	//getSensorList();
		
		for(int i = 0; i < old_sensorList.size(); i++) {
			
			System.out.println("old_sensor id = "+old_sensorList.get(i)+", delete "+ (deleteSensorId(old_sensorList.get(i))==true?"OK":"Fail"));
			
		}
		
		// 
		Connection dbConn = null;
		PreparedStatement pstmt = null;
		
		try {
			Class.forName(JDBC_DRIVER);
			dbConn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
			
			String sql = "insert into emoldino.TB_SENEOR(SENSOR_ID) values (?)";
			pstmt = dbConn.prepareStatement(sql);

			for(int i = 0; i<sensorList.size();i++) {
				pstmt.setString(1, sensorList.get(i));
				
				ret += pstmt.executeUpdate();
			}

			dbConn.close();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			if (dbConn != null) {
				try {
					dbConn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		return ret;
	}
	
	
	public static void main(String[] args) {
		
		Gateway gw = new Gateway();
	
		Dba dba = new Dba();
		
		
		System.out.println(dba.getSensorList());
		
		
		System.out.println(dba.getLastdata());
		
//		
//		ArrayList<String> ar = new ArrayList<String>();
//		ar.add("GL2137WU0015");
//		ar.add("GL2137WU0014");
//		ar.add("GL2137WU0013");
//		ar.add("GL2137WU0012");
//		ar.add("GL2137WU0011");
//		
////		dba.insertSensotList(ar);
//		
//		System.out.println(dba.getSensorList());
//		
//		System.out.println(dba.updateSensorReadTime("GL2137WU0007", "2021-12-03 11:11:22.023"));
//		
//		System.out.println(dba.getSensorList());
		
	
		
		
		
	}
	
	public static int compareHour(LocalDateTime date1, LocalDateTime date2) {
		LocalDateTime dayDate1 = date1.truncatedTo(ChronoUnit.HOURS);
		LocalDateTime dayDate2 = date2.truncatedTo(ChronoUnit.HOURS);
		int compareResult = dayDate1.compareTo(dayDate2);
		System.out.println("=== 시간 단위 비교 ===");
		System.out.println("date1.truncatedTo(ChronoUnit.HOURS) : " + dayDate1);
		System.out.println("date2.truncatedTo(ChronoUnit.HOURS) : " + dayDate2);
		System.out.println("결과 : " + compareResult);
		return compareResult;
		}


}
