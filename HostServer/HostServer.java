/*
 * Travis Vaughn 04/29/2016
 * CSC 435 java version 1.8
 * Notes: keeping below two sections of comments to reference original implementation
 */

/* ####Professor's Comments####
2012-05-20 Version 2.0

Thanks John Reagan for this well-running code which repairs the original
obsolete code for Elliott's HostServer program. Dr.Elliott made a few additional
changes to John's code, so blame Elliott if something is not running.

I ended up rewriting the code and entering my own comments. 

-----------------------------------------------------------------------
NOTE: This is NOT a suggested implementation for your agent platform,
but rather a running example of something that might serve some of
your needs, or provide a way to start thinking about what YOU would like to do.
You may freely use this code as long as you improve it and write your own comments.

-----------------------------------------------------------------------

/* ####Beginning of Travis's amended comments.#### 
Execution:
>java HostServer
*Begin web browser to local host 1565. (complete user form)
*Start additional browsers pointed to same host. (complete user form)
*Enter "migrate" in text box in order to migrate to different agent port. 
-----------------------------------------------------------------------------------

COMMENTS:
This is a basic setup to create agents that work on behalf of the main
Web Server to handle and migrate requests across different available ports
on the server. 

Could implement this across multiple servers, but the basic concept for this is one server. 

State is managed by a number that increments for every client request on the port. 

-----------------------------------------------------------------------------------
 
 This progam consists of:
 class agentHolder
	This is the basic object to manage agent ports and states.
 class AgentListener extends Thread
	This sets up the agent ports with the main server
	And initializes the client user form
	And kicks off the AgentWorker class
 class AgentWorker extends Thread
	This processes the client requests: regular and migration requests
 public class HostServer
	This is the main server thread that spawns the new agent ports by listening
	To for new server requests from client/browser on port 1565
 
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

 
/*
 * Object created to hold agent connection and manage agent state
 * and pass it between ports
 */
class agentHolder {
	//active serversocket object
	ServerSocket sock;
	//basic agentState var
	int agentState;
	
	//basic constructor
	agentHolder(ServerSocket s) { sock = s;}
}
 
 /*
  * Looks out for each of the "agent" ports. Manages the requests from client by calling AgentWorker.  
  * Initialized everytime new request is made to host server (which has different port from agents).
  */
class AgentListener extends Thread {
	Socket sock;
	int agentPort;
	
	AgentListener(Socket As, int prt) { //Constructor
		sock = As;
		agentPort = prt;
	}
	
	int agentState = 0; //initial agent state
	
	/* This is the routine that establishes the server connection with the browser 
	 * And calls agent worker when request is made.
	 */
	public void run() {
		//Get I/O streams in/out from the socket:
		BufferedReader in = null;
		PrintStream out = null;
		String NewHost = "localhost";
		System.out.println("In AgentListener Thread");		
		try {
			String buf;
			//creating the output stream where we print out on the server screen what we're looking up
			out = new PrintStream(sock.getOutputStream());
			//creating the input stream to the server from the client (where it takes get request and host)
			in =  new BufferedReader(new InputStreamReader(sock.getInputStream()));
			
			//this is the GET request from the browser to hostserver
			buf = in.readLine();
			
			//if buf has valid request and we have a state
			//go through request and parse out values to store
			if(buf != null && buf.indexOf("[State=") > -1) {
				//this gets the state from the request
				String tempbuf = buf.substring(buf.indexOf("[State=")+7, buf.indexOf("]", buf.indexOf("[State=")));
				agentState = Integer.parseInt(tempbuf);//this changes from string to int
				System.out.println("agentState is: " + agentState); //printing agent state to server 
					
			}
			
			System.out.println("Buf: " + buf); //logs buf to server 
			StringBuilder htmlResponse = new StringBuilder(); //initializes string build for html response
			
			//adds HTML header, port, & user form using local method 
			htmlResponse.append(sendHTMLheader(agentPort, NewHost, buf)); 
			htmlResponse.append("Now in Agent Looper starting Agent Listening Loop\n<br />\n");
			htmlResponse.append("[Port="+agentPort+"]<br/>\n"); //adds port again; seems redudant
			htmlResponse.append(sendHTMLsubmit()); //ends with input submit tags
			//sends information to client
			sendHTMLtoStream(htmlResponse.toString(), out);
			
			ServerSocket servsock = new ServerSocket(agentPort,2); //open connection @ agent port
			agentHolder agenthold = new agentHolder(servsock); //initializes agent hold, setting sock to servsock
			agenthold.agentState = agentState; //sets agent state
			
			//listen for connections to the agent port
			while(true) {
				sock = servsock.accept(); //opens connection to listen for requests from user form/browser
				System.out.println("Got a connection to agent at port " + agentPort); //print agent port ot server
				new AgentWorker(sock, agentPort, agenthold).start(); //process request from userform/browser
			}
		
		} catch(IOException ioe) { //standard IO exception except also notifies when port is killed
			System.out.println("Either connection failed, or just killed listener loop for agent at port " + agentPort);
			System.out.println(ioe);
		}
	}

	/* This sends HTML headers from server to client/browser for the userform*/
	static String sendHTMLheader(int agentPort, String NewHost, String inLine) {
		
		StringBuilder htmlString = new StringBuilder(); //instantiates new string builder

		htmlString.append("<html><head> </head><body>\n");
		htmlString.append("<h2>This is for submission to PORT " + agentPort + " on " + NewHost + "</h2>\n");
		htmlString.append("<h3>You sent: "+ inLine + "</h3>"); //header of text user sent
		htmlString.append("\n<form method=\"GET\" action=\"http://" + NewHost +":" + agentPort + "\">\n"); //GET request
		htmlString.append("Enter text or <i>migrate</i>:"); //text for user form entry box
		htmlString.append("\n<input type=\"text\" name=\"person\" size=\"20\" value=\"YourTextInput\" /> <p>\n"); //input box
		
		return htmlString.toString(); //converts builder to string
	}
	/*Sends final html header for submission input*/
	static String sendHTMLsubmit() {
		return "<input type=\"submit\" value=\"Submit\"" + "</p>\n</form></body></html>\n";
	}

	/*Sends response headers along with request back to browser*/
	static void sendHTMLtoStream(String html, PrintStream out) {
		
		out.println("HTTP/1.1 200 OK"); //http header
		out.println("Content-Length: " + html.length()); //content length of response
		out.println("Content-Type: text/html"); //content type of response
		out.println(""); //the return characters		
		out.println(html); //the actual response 
	}
	
}
 
/*
 * So agent ports are created in the previous class (AgentListener)
 * This class ends up processing the client/browser requests between the different 
 * Agents. It also migrates clients to available agents as necessary. 
 */
 
class AgentWorker extends Thread {
	
	Socket sock; //client connection
	agentHolder parentAgentHolder; //keeps agent state and socket
	int agentPort; //this agent port
	
	AgentWorker (Socket s, int prt, agentHolder ah) { //constructor
		sock = s;
		agentPort = prt;
		parentAgentHolder = ah;
	}
	
	/* This sets up the connections with the agents, reading in the client text 
	 * from the user form and sending to across to next available port.
	 */
	public void run() {
		
		//initializing
		PrintStream out = null;
		BufferedReader in = null;
		String NewHost = "localhost"; //in practice we would need to allow different hosts
		int NewHostMainPort = 1565; //server port for main worker as new host is technically the ports >3000
		String buf = "";
		int newPort;
		Socket clientSock;
		BufferedReader fromHostServer;
		PrintStream toHostServer;
		
		try {
			out = new PrintStream(sock.getOutputStream());
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			
			//read request from client / user form 
			String inLine = in.readLine();

			//builds string in order to send accurate content length and request headers. 
			StringBuilder htmlString = new StringBuilder(); //not used until later on 
			
			System.out.println();//adding space for visualization 
			System.out.println("Request line: " + inLine); //prints client request to server
			
			//looks for "migrate" in the text box and if so, migrates proceeding text
			//switching to a new port
			if(inLine.indexOf("migrate") > -1) {
				clientSock = new Socket(NewHost, NewHostMainPort); //creates new agent connection wtih same local server
				fromHostServer = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
				toHostServer = new PrintStream(clientSock.getOutputStream());
				
				//sends request to server to receive next open agent port
				toHostServer.println("Please host me. Send my port! [State=" + parentAgentHolder.agentState + "]");
				toHostServer.flush();
				
				//listens until valid port is found
				for(;;) {
					buf = fromHostServer.readLine(); //reads in server response
					if(buf.indexOf("[Port=") > -1) { //checks if valid port response 
						break;
					}
				}
				
				//parse port number
				String tempbuf = buf.substring( buf.indexOf("[Port=")+6, buf.indexOf("]", buf.indexOf("[Port=")) );
				newPort = Integer.parseInt(tempbuf); //converts port number to actual integer
				System.out.println("newPort is: " + newPort); //prints new agent port number back to server
				
				/*Builds migration html response / user form for client/browser*/
				htmlString.append(AgentListener.sendHTMLheader(newPort, NewHost, inLine)); //html headers
				htmlString.append("<h3>We are migrating to host " + newPort + "</h3> \n"); //displays new port
				htmlString.append("<h3>View the source of this page to see how the client is informed of the new location.</h3> \n");
				htmlString.append(AgentListener.sendHTMLsubmit()); //final submission response

				System.out.println("Killing parent listening loop."); //print to server killing waiting agent port
				ServerSocket ss = parentAgentHolder.sock; //storing parent socket in variable.
				ss.close(); //close old agent port
				
				
			} else if(inLine.indexOf("person") > -1) { //if it's not a migration request
				parentAgentHolder.agentState++; //increment state on current agent port within this instance.
				//build html response from agent back to client/browser displaying updated 
				//agent state and user form
				htmlString.append(AgentListener.sendHTMLheader(agentPort, NewHost, inLine));
				htmlString.append("<h3>We are having a conversation with state   " + parentAgentHolder.agentState + "</h3>\n");
				htmlString.append(AgentListener.sendHTMLsubmit());

			} else {
				//invalid request from client
				//likely fav.ico request 
				htmlString.append(AgentListener.sendHTMLheader(agentPort, NewHost, inLine));
				htmlString.append("You have not entered a valid request!\n"); //tell client/browser invalid request
				htmlString.append(AgentListener.sendHTMLsubmit());		
				
		
			}
			//sends html response based on whether it was a migration, regular, or invalid request
			AgentListener.sendHTMLtoStream(htmlString.toString(), out);
			
			sock.close(); //close
			
			
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}
	
}



/*
 * This is the host server.  Currently it is set to listen @ port 1565.
 * This is not the same port that the clients/agents are assigned to.
 * Those are assigned above 3000 with the assumption that all of those ports
 * are free.  
 */
public class HostServer {
	//This is the initial port; however since when calling next port, 
	//we always add 1, we start with port 3001
	public static int NextPort = 3000;
	
	public static void main(String[] a) throws IOException {
		int q_len = 6;
		int port = 1565;
		Socket sock;
		
		ServerSocket servsock = new ServerSocket(port, q_len);
		System.out.println("Travis Vaughn's Host Server started at port 1565.");
		System.out.println("Connect from 1 to 3 browsers using \"localhost:1565\"\n");
		//main loop, listening for new/migration requests  
		while(true) {
			//increment nextport! could be more sophisticated, but this will work for now 
			NextPort = NextPort + 1;
			sock = servsock.accept(); //open socket for server to listen for requests
			//log startup
			System.out.println("Starting AgentListener at port " + NextPort);
			//this is where next port comes into play. 
			//we open up port to listen for requests for the agents. 
			new AgentListener(sock, NextPort).start(); //Spawn agent "server" aka listener to listen for requests. 
													  //creating a new thread each time a new request comes in to server
		}
		
	}
}