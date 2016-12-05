

/**Travis Vaughn | 4/3/2016
 * File is: JokeClient.java, Version 1.8
 * >javac JokeClient
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
 * A client for JokeServer. Elliott, after Hughes, Shoffner, Winslow
 * This will not run unless TCP/IP is loaded on your machine.
 *
 ---------------------------------------------------------------*/
import java.io.*; //Get Input Output libraries
import java.net.*; //Get the Java networking libraries

public class JokeClient {
    public static void main(String args[]) {
        String serverName;

        //if there are no arguments after program name, assume it's localhost
        //otherwise set server to IP/name given
        if(args.length < 1) serverName = "localhost";
        else serverName = args[0];

        System.out.println("Travis Vaughn's Joke Client, 1.8.\n");
        System.out.println("Using server: " + serverName + ", Port: 4444\n"); //not worth it to soft-code.
	
		
        //according to Java documentation, this is a wrapper around Reader which allows
        //for most efficient handling of reading lines from inputs
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        try {
            String request, username;
			int requestNum = 0;

			//getting user name
			System.out.print("What is your name? ");
			username = in.readLine(); //gets user name
			System.out.print("Hello " + username + "! Ready for some fun?!\n");


            do {
                //asking user to request a joke
                System.out.print
                        ("Press Enter for a joke or proverb :), (\\quit) to end: "); //included ability to quit
                System.out.flush();

                //storing user entry into variable
                request = in.readLine();
				
                //this is how we allow user to quit
                //i included '\' in front of quit as I believe it's not a valid URL char
                //even if it is, users are at least less likely to enter \quit for a website
                //as they are quit
                if (request.indexOf("\\quit") < 0) {
                    getJoke(username, Integer.toString(requestNum), serverName); //this sends the website we wish to visit to the server
					requestNum++; //knows to pull next joke
					if (requestNum == 5){
						requestNum = 0; //allows roll-over
					}
                }
            } while (request.indexOf("\\quit") < 0); //exits client server if user enters "\quit"
            System.out.println("Cancelled by user request.");
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

    /* sends a hostname/ip to a server where the server then returns back results to client
     * See server side code for what it sends back to client
     */
    static void getJoke(String name, String requestNum, String serverName){
        Socket sock;
        BufferedReader fromServer;
        PrintStream toServer;
        String textFromServer;
		//String strLines;
		int linesOfText;

        try{
            //Open our connection to server port, choose your own port number..
            sock = new Socket(serverName, 4444); //this is client setting up ability to communicate with server

            //Create filter I/O streams for the socket:
            fromServer =
                    //opening input stream from server; sock has our server name and port number
                    new BufferedReader(new InputStreamReader(sock.getInputStream()));
            toServer = new PrintStream(sock.getOutputStream()); //opening output stream to our server
                                                                //sock has our server name and port number

            //Send request to server:
            toServer.println(name);
            toServer.println(requestNum);
            toServer.flush();

			//read in number of lines to know when to stop
			//linesOfText= Integer.parseInt(fromServer.readLine()) + 2; //add two for the constant string looking for joke... 
			//System.out.println("Lines of text: " + linesOfText);
			
			//88 is an arbitrary sequence at the end of each mode array in 
			//JokeServer.java
			while(!(textFromServer = fromServer.readLine()).equals("88")){
				System.out.println(textFromServer);
			}
			

            */
            sock.close(); //close connection with server
        } catch (IOException x){ //throws IO exception if IO error occurs either when open i/o streams, closing streams, or passing requests
            System.out.println("Socket error.");
            x.printStackTrace();
        }
    }
}
