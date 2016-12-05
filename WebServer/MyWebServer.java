

/**Travis Vaughn | 4/27/2016
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
 * checklist-mywebserver.html
 * MyWebServer.java
 * http-streams.txt
 * serverlog.txt
 *
 * A simple web server specificall designed for browsers. Based on InetServer with 
 * help from Kishori, dhanyu for allowing me to modify their ReadFiles.java program
 * This will not run unless TCP/IP is loaded on your machine.
 *
 ---------------------------------------------------------------*/
import java.io.*; //Get the Input Output libraries
import java.net.*; //Get the Java networking libraries

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
					sendToLog("\n", "serverlog.txt"); //adding spacing
					sendToLog("\n\nGET Method from client: " + get, "serverlog.txt"); //logging
					sendToLog("Host from client: " + host, "serverlog.txt"); //logging
					sendToClient(get, host, out);
				}
				
            } catch(IOException x) { //if an IO error when reading in line from client, will catch exception and print below error
                System.out.println("Server read error");
                x.printStackTrace();
            }
        } catch(IOException ioe)    {System.out.println(ioe);} //if error when opening IO streams, will catch exception and print it to screen
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
	
	/* The core subroutine within worker class
	 * Responsible for sending headers to client,
	 * And sending dynamic content back to client (txt/html)
	 */
	static void sendToClient (String get, String host, PrintStream out){
		String http="", fileType = "", pathname = "";
		String conType = "Content-Type: ";
		String conLen = "Content-Length: ";
		int whitespace = 0, httpCnt = 0, isFileExtension = 0;
		BufferedReader in = null;
		
		/*iterates though get string to parse out necessary text*/
		for (int i = 0; i < get.length(); i++){
			//builds http string
			if (whitespace == 2 && httpCnt < 9) { //weary of hidden chars
				httpCnt++;
				http += get.charAt(i);
			}
			//builds pathname; starts after first /, and does not include whitespace or any text after
			if (i > 4 && get.charAt(i) != ' ' && whitespace == 1) {pathname += get.charAt(i);}

			//building file extension string
			if (isFileExtension == 1){
				if (get.charAt(i) == ' '){isFileExtension = 2;}
				else{
					fileType += get.charAt(i);
				}
			}
			if (get.charAt(i) == '.') {isFileExtension += 1;} //determines when to start fileextension string build
			if (get.charAt(i) == ' ') {whitespace++;} //increment whitespace
		}
		
		/*final http string*/
		http += " 200 OK";
		
		/*final conLen and conType*/
		File filename = new File(pathname);
		File[] strFilesDirs = filename.listFiles();
		//if directory, then we need to determine length of all dir/files within directory
		if (pathname.length() < 1 || pathname.substring(pathname.length()-1).equals("/")){
			int dirSize = 0;
			int subDirCnt = 0; //count sub directories since they require additional text for /
			for ( int i = 0 ; i < strFilesDirs.length ; i ++ ) {
				dirSize += (strFilesDirs[i].getName().length() * 2); //*2 because using it twice in out
				if (strFilesDirs[i].isDirectory ( ) ){ subDirCnt++;}
			}
			dirSize += 4+(strFilesDirs.length * 2); 
			dirSize += (strFilesDirs.length * 20); //html files/dir hardcoded text length
			dirSize += pathname.length() + 23; //pathname length + html hardcoded text
			dirSize += (subDirCnt * 2); //# of / for each subdir is 2
			conLen += dirSize;
			conType = "Content-Type: text/html";
		}
		else { //if client enters file
			conLen += (filename.length()+4);
			if (fileType.equals("txt"))			{conType += "text/plain";}
			else if (fileType.equals("html"))	{conType += "text/html";}
		}
		/*Print headers*/
		out.println(http);
		out.println(conLen);
		out.println(conType);
		out.println("Connection: close");
		out.println("\r\n\r\n");

		/*Open and print file/dir contents to client*/
		try{
			String line;
			if (pathname.substring(pathname.length()-1).equals("/")){ //user entered path to directory
				sendToLog("Pathname sent to returnRules: " + pathname, "serverlog.txt"); //logging
				returnFiles(strFilesDirs, pathname, out);
			}
			else{ //user entered path to file
				InputStream f = new FileInputStream(pathname);
				in = new BufferedReader(new InputStreamReader(f));
				while ((line = in.readLine()) != null){
					out.println(line);
				}
			}
		} catch (IOException ioe) {System.out.println(ioe);}

		//logging
		sendToLog("HTTP header sent to client: " + http, "serverlog.txt");
		sendToLog("Content Length header sent to client: " + conLen, "serverlog.txt");
		sendToLog("Content Type header sent to client: " + conType, "serverlog.txt");
		
		//flush stream
		out.flush();
	}
	
	/*This returns contents of directory client provided.  This is where I modified the ReadFiles mentioned in header*/ 	
	static void returnFiles(File[] strFilesDirs, String pathname, PrintStream out){
		//printing html header
		out.println("<pre><h1>Index of /" + pathname.substring(0, pathname.length()-1) + "</h1>");
		for ( int i = 0 ; i < strFilesDirs.length ; i ++ ) {
		  sendToLog("returnFiles strFilesDir[i]: " + strFilesDirs[i].getName(), "serverlog.txt"); //logging	
		  //grab directories; they require extra / chars
		  if ( strFilesDirs[i].isDirectory ( ) ){ 
			out.println ("<a href=" + "\"" + strFilesDirs[i].getName() + "/\">" + strFilesDirs[i].getName() + "/</a> <br>");
		  }
		  //grab files
		  else if ( strFilesDirs[i].isFile ( ) ){
			out.println ("<a href=" + "\"" + strFilesDirs[i].getName() + "\">" + strFilesDirs[i].getName() + "</a> <br>");
		  }
		}
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
