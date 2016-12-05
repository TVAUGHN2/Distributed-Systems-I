

/**Travis Vaughn | 4/3/2016
 * File is: JokeClient.java, Version 1.8
 * >javac JokeServer
 * 
 * In separate shell windows:
 * >java JokeClient
 * >java JokeServer
 *
 * If operating across multiple machines, be sure to enter
 * machine's IP address where server is hosted as a command
 * when running JokeClient. 
 * >java JokeClient 127.1.0.0
 *
 * All files needed for running program
 * Travis Vaughn's Joke Server Checklist.html
 * JokeClient.java
 * JokeServer.java
 *
 * A server for JokeClient. Elliott, after Hughes, Shoffner, Winslow
 * This will not run unless TCP/IP is loaded on your machine.
 *
 ---------------------------------------------------------------*/
import java.io.*; //Get the Input Output libraries
import java.net.*; //Get the Java networking libraries
import java.lang.*; //Get the Java langauge libraries

/* This creates a new thread each time it's called */
class Worker extends Thread {       //Class definition
    Socket sock;                    //Class member, socket, local to Worker
    Worker (Socket s)  {sock = s;}  //Constructor, assign arg s to local sock
	
		
    /*This function creates the I/O streams and passes arguments from client to server,
     * and prints back status to both client and server
     */
    public void run(){
        //Get I/O streams in/out from the socket:
        PrintStream out = null;
        BufferedReader in = null;
        try {
            //creating the input stream to the server from the client (where it takes in the host joke/ip address
            in = new BufferedReader
                    (new InputStreamReader(sock.getInputStream()));
            //creating the output stream where we print out on the server screen what we're looking up
            out = new PrintStream(sock.getOutputStream());
            // Note that this branch might not execute when expected:
            try {
                String strRequestNum;
                String name;
                name = in.readLine(); //stores user name
                strRequestNum = in.readLine(); //stores the value input on the client side
                System.out.println("Looking up joke for request: " + strRequestNum); //prints out that we're looking up host joke/ ip
                printJoke(name, strRequestNum, out); //prints details back to client
            } catch(IOException x) { //if an IO error when reading in line from client, will catch exception and print below error
                System.out.println("Server read error");
                x.printStackTrace();
            }
        } catch(IOException ioe)    {System.out.println(ioe);} //if error when opening IO streams, will catch exception and print it to screen
    }

    /* Prints to client: input joke given, the host joke of the input, and the host IP
     * Also handles if unknown exception occurs
     */
    static void printJoke (String name, String strRequestNum, PrintStream out) {
		int jokeNum = Integer.parseInt(strRequestNum);

		//all jokes and proverbs are not my own original work.
		//they have been taken from the Internet.
		
		//joke variables
		String[] jokeA = new String[]{name + ", how many computer programmers does it take to change a light bulb?", 
									  "None, that's a hardware problem.","", "88"};
		String[] jokeB = new String[]{name +", listen to this:", "", "Programmer A says: How much money do I owe you?",
									  "Programmer B says: 500 dollars.", 
									  "Programmer A says: Shall we round it off to 512?", "", "88"};
		String[] jokeC = new String[]{name + ", to understand recursion, one must first understand recursion.", "", "88"};
		String[] jokeD = new String[]{name +", a feature is nothing more than a bug with seniority.", "", "88"};
		String[] jokeE = new String[]{name +", did I ever sing you my new joke song?", 
									  "I've got 99 little bugs in the code,",
									  "99 little bugs,",
									  "Take one down, patch it around,",
									  "I've got 127 little bugs in the code.", "", "88"};
		
		String[][] jokeArrays = new String[][]{jokeA, jokeB, jokeC, jokeD, jokeE};
		
		//proverb variables
		String[] proverbA = new String[]{name +"...if you aren't, at any given time, scandalized by code you wrote five" +
										" or even three years ago, you're not learning anywhere near enough.","","88"};
		String[] proverbB = new String[]{name +", the most important property of a program is whether it" +
										 " accomplishes the intention of its user.", "","88"};
		String[] proverbC = new String[]{name +", computer science is no more about computers " +
										 "than astronomy is about telescopes.", "","88"};
		String[] proverbD = new String[]{name +", being an expert can be an excuse for staying in your comfort zone.", "", "88"};
		String[] proverbE = new String[]{name +", the first symptom of stagnation is preference.", "","88"};
		
		String[][] proverbArrays = new String[][]{proverbA, proverbB, proverbC, proverbD, proverbE};

		//maintenance variable
		String[] maintenance = new String[]{"The server is temporarily unavailable--check-back shortly.", "", "88"};
		String[][] maintenanceArrays = new String[][]{maintenance};
		
        //try {
			//out.println(jokeArrays[jokeNum].length); //returns length of joke being requested
           
			
			//server output if in joke mode
			if (Mode.currentMode.equals("joke-mode")){
				out.println("Looking up joke...\n");
				for (int i = 0; i < jokeArrays[jokeNum].length; i++) {
					out.println(jokeArrays[jokeNum][i]); //To client
				}
			}

			//server output if in proverb mode
			else if (Mode.currentMode.equals("proverb-mode")){
				out.println("Looking up proverb...\n");
				for (int j = 0; j < proverbArrays[jokeNum].length; j++) {
					out.println(proverbArrays[jokeNum][j]); //To client
				}
			}

			//server out put if in maintenance mode
			else{
				for (int k = 0; k < maintenanceArrays[0].length; k++) {
					out.println(maintenanceArrays[0][k]); //To client
				}
			}
    }

    //Not interesting to us:
    static String toText(byte ip[]) { //Make portable for 128 bit format
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < ip.length; i++){
            if (i > 0) result.append(".");
            result.append(0xff & ip[i]);
        }
        return result.toString();
    }
}

/*This runs creates a new admin specific thread*/
class AdminRequest implements Runnable {
  public void run(){ // running admin request
    System.out.println("In the admin looper thread");
    
    int q_len = 6; /* Number of requests for OpSys to queue */
    int port = 4555;  // listening at different port as other port is blocked and should remain block
					  //just as this one should be.
    Socket sock;

    try{
      ServerSocket servsock = new ServerSocket(port, q_len);
      while (true) {
		// listening for admin connection
		sock = servsock.accept();
		new AdminWorker (sock).start(); 
      }
    }catch (IOException ioe) {System.out.println(ioe);}
  }
}
 
 class AdminWorker extends Thread {       //Class definition
    Socket sock;                    //Class member, socket, local to Worker
    AdminWorker (Socket s)  {sock = s;}  //Constructor, assign arg s to local sock

	    /*This function creates the I/O streams and passes arguments from client to server,
     * and prints back status to both client and server
     */
    public void run(){
        //Get I/O streams in/out from the socket:
        PrintStream out = null;
        BufferedReader in = null;
        try {
            //creating the input stream to the server from the admin client
            in = new BufferedReader
                    (new InputStreamReader(sock.getInputStream()));
            //creating the output stream where we print out on the server screen what we're looking up
            out = new PrintStream(sock.getOutputStream());
            // Note that this branch might not execute when expected:
            try {
                String mode;
                mode = in.readLine(); //stores the value input on the client side
                System.out.println("Processing mode request: " + mode); //prints out that we're looking up host joke/ ip
                changeMode(mode, out); //prints details back to client
            } catch(IOException x) { //if an IO error when reading in line from client, will catch exception and print below error
                System.out.println("Server read error");
                x.printStackTrace();
            }
        } catch(IOException ioe)    {System.out.println(ioe);} //if error when opening IO streams, will catch exception and print it to screen
    }
	
	/* Prints to client: input joke given, the host joke of the input, and the host IP
     * Also handles if unknown exception occurs
     */
    static void changeMode (String mode, PrintStream out) {
    	out.println("Processing mode request...\n");
        //try {
    	if (Mode.currentMode.equals(mode)){
    		out.println("Server is already in this mode, nothing changed.");
    	}
    	else if (mode.equals("joke-mode") || mode.equals("proverb-mode") || mode.equals("maintenance-mode")){
    		Mode.currentMode = mode;
    		out.println("Server mode changed to: " + mode);

    	}
    	else{
    		out.println("Incorrect mode entered.");
    	}
			//code to change mode @ server level
			//}
        //} catch(UnknownHostException ex){
         //   out.println("Failed in attempt to lookup joke");
        //}
    }
}

class Mode{
	public static String currentMode = "joke-mode";
}
 
public class JokeServer {

    /* This is the code that executes the server
     * Generates infinite loop to keep handling client requests
     * With each new request, a new thread is created to handle request
     */
    public static void main(String a[]) throws IOException{
        int q_len = 6; //Not interesting. Number of requests for Opsys to queue
        int port = 4444; //this is how the server and client know where to send/receive requests on the server.
                        // it specifically identifies the process, since a server can handle multiple different requests
		Socket sock;
	
		AdminRequest admin = new AdminRequest(); // create different thread for admin specific request
		Thread t = new Thread(admin);
		t.start();  // waits for admin input
		
        ServerSocket servsock = new ServerSocket(port, q_len); //initializes new server socket

        System.out.println
                ("Travis Vaughn's Joke Server 1.8 starting up.\n");
        //this is the main server routine.  It listens for client requests and then sends their requests
        //off to new threads for the Worker class to handle
		
        while (true){
            sock = servsock.accept(); //wait for the next client connection
            new Worker(sock).start(); //Spawn worker to handle it; this is the multi-threading
											   //creating a new thread each time a new client request comes in
		}
    }
}
