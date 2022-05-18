package com.emoldino.gateway;

/**
 * Hello world!
 *
 */
public class App 
{
	
	public App() {
		
	}
	
	public int sendREST(String sendUrl, String jsonString) {
		int ret = -1;
		
		
		
		return ret;
	}
	
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        
        
		Gateway gw = new Gateway();
		gw.setTerminalId("Test1-JB-terminal");
		gw.setServerUrl("https://dev-feature.emoldino.com");
		
//		gw.setServerUrl("http://49.247.200.147");
		
		gw.reportHeartbit();
        
    }
    
    
}
