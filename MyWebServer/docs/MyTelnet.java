

/**Travis Vaughn | 4/3/2016
 * File is: MyTelnet.java, Version 1.8
 * >javac MyTelnet
 * 
 * In separate shell windows:
 * >java MyTelnet
 * >java InetServer
 *
 * If operating across multiple machines, be sure to enter
 * machine's IP address where server is hosted as a command
 * when running MyTelnet. 
 * >java MyTelnet 127.1.0.0
 *
 * All files needed for running program
 * Travis Vaughn's Joke Server Checklist.html
 * MyTelnet.java
 * InetServer.java
 *
 * A client for InetServer. Elliott, after Hughes, Shoffner, Winslow
 * This will not run unless TCP/IP is loaded on your machine.
 *
 ---------------------------------------------------------------*/
import java.io.*; //Get Input Output libraries
import java.net.*; //Get the Java networking libraries

public class MyTelnet {
    public static void main(String args[]) {
        String serverName;

        //if there are no arguments after program name, assume it's localhost
        //otherwise set server to IP/name given
        if(args.length < 1) serverName = "localhost";
        else serverName = args[0];

        System.out.println("Travis Vaughn's Inet Client, 1.8.\n");
        System.out.println("Using server: " + serverName + ", Port: 4444"); //not worth it to soft-code.

        //according to Java documentation, this is a wrapper around Reader which allows
        //for most efficient handling of reading lines from inputs
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        try {
            String getRequest, hostRequest;
                //asking user for website they wish to visit
                //storing user entry into variable
                getRequest = in.readLine();
				hostRequest = in.readLine();

                //this is how we allow user to quit
                //i included '\' in front of quit as I believe it's not a valid URL char
                //even if it is, users are at least less likely to enter \quit for a website
                //as they are quit
                if (getRequest.indexOf("\\quit") < 0) {
                    getServerResponse(getRequest, hostRequest, serverName); //this sends the website we wish to visit to the server
                }
        } catch(IOException x)  {x.printStackTrace();} //throws IO exception if issue with print out or, storing names, or getting remote address
    }

    static String toText (byte ip[]) {//make portable for 128 bit format
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < ip.length; ++ i){
            if (i > 0) result.append(".");
            result.append(0xff & ip[i]);
        }
        return result.toString();
    }

	static void sendToLog (String request, String filename){
		try(FileWriter fw = new FileWriter(filename, true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter pw= new PrintWriter(bw))
		{
			pw.println(request);

		} catch (IOException ioe) {System.out.println(ioe);}
	}
	
    /* sends a hostname/ip to a server where the server then returns back results to client
     * See server side code for what it sends back to client
     */
	 
	 
    static void getServerResponse(String getRequest, String hostRequest, String serverName){
        Socket sock;
        BufferedReader fromServer;
        PrintStream toServer;
        String textFromServer;

        try{
            //Open our connection to server port, choose your own port number..
            sock = new Socket(serverName, 80); //this is client setting up ability to communicate with server

            //Create filter I/O streams for the socket:
            fromServer =
                    //opening input stream from server; sock has our server name and port number
                    new BufferedReader(new InputStreamReader(sock.getInputStream()));
            toServer = new PrintStream(sock.getOutputStream()); //opening output stream to our server
                                                                //sock has our server name and port number

            //Send machine name or IP address to server:
            toServer.println(getRequest);
			toServer.println(hostRequest);
			toServer.println("\r\n\r\n");
            toServer.flush();

            //Read two or three lines of response from the server,
            //and block while synchronously waiting;
			while((textFromServer = fromServer.readLine()) != null){
				System.out.println(textFromServer);
				sendToLog(textFromServer, "http-streams.txt");
			}
            sock.close(); //close connection with server
        } catch (IOException x){ //throws IO exception if IO error occurs either when open i/o streams, closing streams, or passing requests
            System.out.println("Socket error.");
            x.printStackTrace();
        }
    }
}
