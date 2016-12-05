

/**Travis Vaughn | 4/3/2016
 * File is: JokeClientAdmin.java, Version 1.8
 * >javac JokeClientAdmin
 * 
 * In separate shell windows:
 * >java JokeClientAdmin
 * >java JokeServer
 *
 * If operating across multiple machines, be sure to enter
 * machine's IP address where server is hosted as a command
 * when running JokeClientAdmin. 
 * >java JokeClientAdmin 127.1.0.0
 *
 * All files needed for running program
 * Travis Vaughn's Joke Server Checklist.html
 * JokeClientAdmin.java
 * JokeServer.java
 *
 * A client for JokeServer. Elliott, after Hughes, Shoffner, Winslow
 * This will not run unless TCP/IP is loaded on your machine.
 *
 ---------------------------------------------------------------*/
import java.io.*; //Get Input Output libraries
import java.net.*; //Get the Java networking libraries

public class JokeClientAdmin {
    public static void main(String args[]) {
        String serverName, mode;

        //if there are no arguments after program name, assume it's localhost
        //otherwise set server to IP/name given
        if(args.length < 1) {
            System.out.println("ERROR: This command requires user to input mode");
            System.exit(0);
            //here as dummy variables to get program to compile 
            //since javac doesn't recognize that program won't ever get past System.exit in this if statement
            serverName = "localhost"; 
            mode = args[0];
        }
        else if (args.length == 1){
            serverName = "localhost";
            mode = args[0];
        }
        else {
            serverName = args[0];
            mode = args[1];
        }
        System.out.println("Travis Vaughn's Joke Client Admin, 1.8.\n");
        System.out.println("Using server: " + serverName + ", Port: 4555\n"); //not worth it to soft-code.
    
        
        sendMode(mode, serverName); //this sends the mode we wish to change the server to

    }

    /* sends a hostname/ip to a server where the server then returns back results to client
     * See server side code for what it sends back to client
     */
    static void sendMode(String mode, String serverName){
        Socket sock;
        BufferedReader fromServer;
        PrintStream toServer;
        String textFromServer;


        try{
            //Open our connection to server port, choose your own port number..
            sock = new Socket(serverName, 4555); //this is client setting up ability to communicate with server

            //Create filter I/O streams for the socket:
            fromServer =
                    //opening input stream from server; sock has our server name and port number
                    new BufferedReader(new InputStreamReader(sock.getInputStream()));
            toServer = new PrintStream(sock.getOutputStream()); //opening output stream to our server
                                                                //sock has our server name and port number

            //Send request to server:
            toServer.println(mode);
            toServer.flush();


            //Read two or three lines of response from the server,
            //and block while synchronously waiting;
            for (int i = 1; i <=3; i++){
                textFromServer = fromServer.readLine();
                if(textFromServer != null) System.out.println(textFromServer);
            }

            sock.close(); //close connection with server
        } catch (IOException x){ //throws IO exception if IO error occurs either when open i/o streams, closing streams, or passing requests
            System.out.println("Socket error.");
            x.printStackTrace();
        }
    }
}
