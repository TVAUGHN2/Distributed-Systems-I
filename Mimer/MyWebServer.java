

/**Travis Vaughn | 5/8/2016 |
 * File is: MyWebServer.java, Version 1.8
 * >javac MyWebServer
 * 
 * In separate shell windows:
 * >java MyWebServer
 *
 * If operating across multiple machines, be sure to enter
 * machine's IP address where server is hosted as a command
 * when running browser. 
 *
 * All files needed for running program
 * checklist-mimer.html
 * MyWebServer.java
 * http-streams.txt
 * serverlog.txt
 * BCHandler.java
 * mimer-discussion.html
 *
 * A simple web server specifically designed for browsers. Based on InetServer with 
 * help from Kishori, dhanyu for allowing me to modify their ReadFiles.java program
 * This will not run unless TCP/IP is loaded on your machine.
 *
 * Notes: Tested and confirmed functionality on IE, Firefox, and Chrome.
 * Handles .txt, .html, and .xyz files (xyz = xml for all intents and purposes) 
 ---------------------------------------------------------------*/
import java.io.*; //Get the Input Output libraries
import java.net.*; //Get the Java networking libraries
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/*Allows me to carry script values through multiple methods*/
class ScriptVal{
	String name;
	int val;
	int val2;
}

class GET{
	String fileType = "";
	String http = "";
	String path = "";
	String scriptStr = "";
	int isScript = 0;
}

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
            //creating the input stream to the server from the client (where it takes get request and host)
            in = new BufferedReader
                    (new InputStreamReader(sock.getInputStream()));
            //creating the output stream where we print out on the server screen what we're looking up
            out = new PrintStream(sock.getOutputStream());
            // Note that this branch might not execute when expected:
            try {
                String get, host;
				get = in.readLine();
				host = in.readLine();
				
				//ignore passing null values
				if (get != null && host != null){
					sendToLog("", "serverlog.txt"); //adding spacing
					sendToLog("\nGET Method from client: " + get, "serverlog.txt"); //logging
					sendToLog("Host from client: " + host, "serverlog.txt"); //logging
					sendToClient(get, host, out);
				}
			//if an IO error when reading in line from client, will catch exception and print below error
            } catch(IOException x) { 
                System.out.println("Server read error");
                x.printStackTrace();
            }
			
			sock.close();
		//if error when opening IO streams, will catch exception and print it to screen
        } catch(IOException ioe)    {System.out.println(ioe);} 
    }

	/* The core subroutine within worker class
	 * Responsible for sending headers to client,
	 * And sending dynamic content back to client (txt/html)
	 */
	static void sendToClient (String get, String host, PrintStream out){
		GET g = new GET();
		String conType = "Content-Type: ";
		String conLen = "Content-Length: ";
		BufferedReader in = null;
		
		/* Parse GET string and return values as seen in GET class*/
		g = parseGET(get, g);
		
		/*Parsing and setting final script values*/
		ScriptVal script = new ScriptVal();
		script = parseScript(g.scriptStr);

		//creating file and getting dir files if any
		if (g.path.length() < 1)	{g.path = "./";} //root
		File filename = new File(g.path);
		File[] strFilesDirs = filename.listFiles();

		/*final conLen and conType*/
		if (g.path.length() < 1 || g.path.substring(g.path.length()-1).equals("/")){ 
			//if directory, get content type & length
			//Determine length of all dir/files within directory
			//<1 takes care of favicon out-of-bounds errors
			conLen += getConLen('d', script, filename, strFilesDirs, g.path);
			conType = "Content-Type: text/html";
		}
		else if (g.isScript == 1){ //if script, get content type & length 
			conLen += getConLen('s', script, filename, strFilesDirs, g.path);
			conType += "text/html";
		}
		else { //if client enters file, get content type & length 
			conLen += getConLen('f', script, filename, strFilesDirs, g.path);
			if (g.fileType.equals("txt"))			{conType += "text/plain";}
			else if (g.fileType.equals("html"))	{conType += "text/html";}
			else if (g.fileType.equals("xyz"))	{conType += "application/xyz";}
		}
		
		/*Print headers*/
		out.println(g.http);
		out.println(conLen);
		out.println(conType);
		out.println("Connection: close");
		out.println("\r\n\r\n");

		//logging
		sendToLog("HTTP header sent to client: " + g.http, "serverlog.txt");
		sendToLog("Content Length header sent to client: " + conLen, "serverlog.txt");
		sendToLog("Content Type header sent to client: " + conType, "serverlog.txt");
		
		/*Open and print file/dir contents to client*/
		try{
			String line;
			if (g.path.substring(g.path.length()-1).equals("/")){ //user entered path to directory
				sendToLog("path sent to returnRules: " + g.path, "serverlog.txt"); //logging
				returnFiles(strFilesDirs, g.path, out);
			}
			else if (g.isScript == 1){ //user submitted user form
				sendToLog("Script String that was parsed: " + g.scriptStr, "serverlog.txt"); //logging
				addnums(script, out);
			}
			else{ //user entered path to file
				InputStream f = new FileInputStream(g.path);
				in = new BufferedReader(new InputStreamReader(f));
				while ((line = in.readLine()) != null){
					out.println(line);
				}
			}
		} catch (IOException ioe) {System.out.println(ioe);}

		//flush stream
		out.flush();
	}
	
	/*This determines the content length*/
	static int getConLen(Character c, ScriptVal script, File filename, File[] strFilesDirs, String path){
		int conLen;
		if (c == 's'){ //script
			conLen = (39 + script.name.length() +  String.valueOf(script.val).length() + 
					String.valueOf(script.val2).length() + String.valueOf(script.val + script.val2).length()); 
		}
		else if (c == 'd'){ //directory
			if (path.length() == 1)		{conLen = 47;} //if person is trying to access parent directory, send denied conlen
			else{	
				int dirSize = 0;
				int subDirCnt = 0; //count sub directories since they require additional text for /
				for ( int i = 0 ; i < strFilesDirs.length ; i ++ ) {
					dirSize += (strFilesDirs[i].getName().length() * 2); //*2 because using it twice in out
					if (strFilesDirs[i].isDirectory ( ) ){ subDirCnt++;}
				}
				dirSize += 4+(strFilesDirs.length * 2); 
				dirSize += (strFilesDirs.length * 20); //html files/dir hardcoded text length
				dirSize += path.length() + 23; //path length + html hardcoded text
				dirSize += (subDirCnt * 2); //# of / for each subdir is 2
				conLen = dirSize;
			}
		}
		else{ //file
			if (path.substring(path.length()-4, path.length()).equals(".xyz")){
				conLen = ((int)filename.length()+8); //xml file
			}else{conLen = ((int)filename.length()+4);} //text/html file
		} 
		
		return (conLen);
	}
	
	/*Parses GET request*/
	static GET parseGET (String get, GET g){
		/**Side Note: 
		  * I didn't think about using substring until I already parsed this way.
		  * I'm not changing as I think technically this is faster
		  * Because it would require multiple substring calls.
		  * Pick-up is likely minimal, likely leading preference to 
		  * be readability, but laziness wins out!
		  */
		int whitespace = 0, httpCnt = 0, isFileExtension = 0;
		
		/*iterates though get string to parse out necessary text*/

		for (int i = 0; i < get.length(); i++){
			//builds http string
			if (whitespace == 2 && httpCnt < 9) { //weary of hidden chars
				httpCnt++;
				g.http += get.charAt(i);
			}
			//build script string
			if(g.isScript == 1 && whitespace == 1)	{g.scriptStr+=get.charAt(i);}

			//builds path; starts after first /
			//and does not include whitespace or any text after
			if (i > 4 && get.charAt(i) != ' ' && whitespace == 1 && g.isScript == 0) 
				{g.path += get.charAt(i);}

			//building file extension string
			if (isFileExtension == 1 && g.isScript == 0){
				if (get.charAt(i) == ' ')	{isFileExtension = 2;}
				else						{g.fileType += get.charAt(i);}
			}
			if (get.charAt(i) == '.') {isFileExtension += 1;} //determines when to start fileextension string build
			if (get.charAt(i) == ' ') {whitespace++;} //increment whitespace
			if (get.charAt(i) == '?')	{g.isScript = 1;} //sets isScript flag
		}
		
		g.http += " 200 OK";
		return (g);
	}
	
	/*Parses script and sets script values*/
	static ScriptVal parseScript (String s){
		int equalCnt = 0, ampCnt = 0;
		String name = "", val = "", val2 = "";
		ScriptVal scr = new ScriptVal();
		for (int i = 0; i < s.length(); i++){
			if(s.charAt(i) == '&'){ampCnt++;}//iterate ampCnt
			//build name string
			if(equalCnt == 1 && ampCnt == 0){
				if(s.charAt(i) == '+'){name+= ' ';} //if user enters multiple names
				else{name+=s.charAt(i);}
			} 
			else if(equalCnt== 2 && ampCnt == 1) //store first number
				{val += Character.getNumericValue(s.charAt(i));} 
			else if (equalCnt == 3 && ampCnt == 2 && s.charAt(i) != ' ') //store second number
				{val2 += Character.getNumericValue(s.charAt(i));} 
			if (s.charAt(i) == '='){equalCnt++;}//iterate equal count
		}
		//setting scrip values
		scr.name = name; 
		try{ //parseInt throws exceptions
			scr.val = (int)Integer.parseInt(val); 	
		} catch (NumberFormatException e){}
		try{//parseInt throws exceptions
		scr.val2 = (int)Integer.parseInt(val2); 
		} catch (NumberFormatException e){}
		
		//logging
		sendToLog("Script name: " + scr.name, "serverlog.txt"); 
		sendToLog("Script val: " + scr.val, "serverlog.txt"); 
		sendToLog("Script val2: " + scr.val2, "serverlog.txt"); 
		return (scr);
	}
	
	/*This returns contents of directory client provided.  This is where I modified the ReadFiles mentioned in header*/ 	
	static void returnFiles(File[] strFilesDirs, String path, PrintStream out){
		//security to prevent from going to parent folder
		if (path.substring(0,1).equals("/")){out.println ("<pre><h1>Forbidden: You do not have access.</h1>");}
		
		//printing html header
		else{
			out.println("<pre><h1>Index of /" + path.substring(0, path.length()-1) + "</h1>");
			for ( int i = 0 ; i < strFilesDirs.length ; i ++ ) {
			  sendToLog("returnFiles strFilesDir[i]: " + strFilesDirs[i].getName(), "serverlog.txt"); //logging	
			  //grab directories; they require extra / chars
			  if ( strFilesDirs[i].isDirectory ( ) ){ 
				out.println ("<a href=" + "\"" + strFilesDirs[i].getName() + "/\">" + 
								strFilesDirs[i].getName() + "/</a> <br>");
			  }
			  //grab files
			  else if ( strFilesDirs[i].isFile ( ) ){
				out.println ("<a href=" + "\"" + strFilesDirs[i].getName() + "\">" + 
								strFilesDirs[i].getName() + "</a> <br>");
			  }
			}
		}
	}
	
	/*Adds script numbers and sends values back to client*/
	static void addnums(ScriptVal s, PrintStream out){	
		out.println("<pre><h1>Yo "+ s.name + ", the sum of " + s.val + 
					" and " + s.val2 + " is " + (s.val + s.val2) + ".</h1>");
	}
	
	/*Send request to log file*/
	static void sendToLog (String request, String filename){
		try(FileWriter fw = new FileWriter(filename, true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter pw= new PrintWriter(bw))
		{
			pw.println(request);

		} catch (IOException ioe) {System.out.println(ioe);}
	}
}

/*BC LOOPER CODE SNIPPET*/
class myDataArray {
  int num_lines = 0;
  String[] lines = new String[10];
}

/* Main class responsible for receiving serialized data, sending ack back to client
 * and deserializing data on server side to print to server console
 */
class BCWorker extends Thread {
    private Socket sock;
    private int i;
    BCWorker (Socket s){sock = s;}
    PrintStream out = null; BufferedReader in = null;

    String[] xmlLines = new String[15];
    String[] testLines = new String[10];
    String xml;
    String temp;
    XStream xstream = new XStream();
    final String newLine = System.getProperty("line.separator");
    myDataArray da = new myDataArray();
    
    public void run(){
      System.out.println("Called BC worker.");
      try{
		//opening i/o streams with client
		in =  new BufferedReader(new InputStreamReader(sock.getInputStream()));
		out = new PrintStream(sock.getOutputStream()); // to send ack back to client
		i = 0; xml = "";
		
		//reading in data from client and building xml message
		while(true){
		  temp = in.readLine();
		  if (temp.indexOf("end_of_xml") > -1) break;
		  else xml = xml + temp + newLine; // Should use StringBuilder in 1.5
		}
		
		//printing serialized data to server
		System.out.println("The XML marshaled data:");
		System.out.println(xml);
		
		//logging serialized data
		sendToLog("The XML marshaled data:", "serverlog.txt"); //adding spacing
		sendToLog(xml, "serverlog.txt"); //logging
		sendToLog("", "serverlog.txt"); //logging
		
		//sending ack back to client
		out.println("Acknowledging Back Channel Data Receipt"); // send the ack
		out.flush(); sock.close(); //closing stream
		
		da = (myDataArray) xstream.fromXML(xml); // deserialize / unmarshal data
		
		//printing/logging deserialized data to server
		System.out.println("Here is the restored data: ");
		sendToLog("Here is the restored data:", "serverlog.txt"); //logging
		for(i = 0; i < da.num_lines; i++){
		  System.out.println(da.lines[i]);
		  sendToLog(da.lines[i], "serverlog.txt"); //logging
		}
      }catch (IOException ioe){} // end run
    }
	
	/*Send request to log file*/
	static void sendToLog (String request, String filename){
		try(FileWriter fw = new FileWriter(filename, true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter pw= new PrintWriter(bw))
		{
			pw.println(request);

		} catch (IOException ioe) {System.out.println(ioe);}
	}
}

/* Opens connection to listen to client requests on back channel
 * Call BCworker to handle data/requests sent by client
 */
class BCLooper implements Runnable {
  public void run(){ // Running the Admin listen loop
    int q_len = 6; /* Number of requests for OpSys to queue */
    int port = 2570;  // Listen here for Back Channel Connections
    Socket sock;
	
    try{
      ServerSocket servsock = new ServerSocket(port, q_len);
      while (true) {
	// wait for the next ADMIN client connection:
		System.out.println("In BC Looper thread, waiting for 2570 connections");
		sock = servsock.accept();
		new BCWorker (sock).start(); 
      }
    }catch (IOException ioe) {System.out.println(ioe);}
  }
}


public class MyWebServer {

    /* This is the code that executes the server
     * Generates infinite loop to keep handling client requests
     * With each new request, a new thread is created to handle request
     */
    public static void main(String a[]) throws IOException{
        int q_len = 6; //Not interesting. Number of requests for Opsys to queue
        int port = 2540; //this is how the server and client know where to send/receive requests on the server.
                        // it specifically identifies the process, since a server can handle multiple different requests
        Socket sock;

		/*BC LOOPER CODE SNIPPET*/
		BCLooper AL = new BCLooper(); // separate thread for back door channel comm with client 
		//AL.run();
		Thread t = new Thread(AL); //open new thread
		t.start();  // listen for requests
		
        ServerSocket servsock = new ServerSocket(port, q_len); //initializes new server socket
		
        System.out.println
                ("Travis Vaughn's Web Server server 1.8 starting up, listening at port 2540.\n");
        //this is the main server routine.  It listens for client requests and then sends their requests
        //off to new threads for the Worker class to handle
        while (true){
            sock = servsock.accept(); //wait for the next client connection
            new Worker(sock).start(); //Spawn worker to handle it; this is the multi-threading
                                      //creating a new thread each time a new client request comes in
        }
    }
}
