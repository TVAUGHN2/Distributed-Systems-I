

/**Travis Vaughn | 4/3/2016
 * File is: InetClient.java, Version 1.8
 * >javac InetClient
 * 
 * In separate shell windows:
 * >java InetClient
 * >java InetServer
 *
 * If operating across multiple machines, be sure to enter
 * machine's IP address where server is hosted as a command
 * when running InetClient. 
 * >java InetClient 127.1.0.0
 *
 * All files needed for running program
 * Travis Vaughn's Joke Server Checklist.html
 * InetClient.java
 * InetServer.java
 *
 * A client for InetServer. Elliott, after Hughes, Shoffner, Winslow
 * This will not run unless TCP/IP is loaded on your machine.
 *
 ---------------------------------------------------------------*/
import java.io.*; //Get Input Output libraries
import java.net.*; //Get the Java networking libraries

public class InetClient {
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
            String name;
            do {
                //asking user for website they wish to visit
                System.out.print
                        ("Enter a hostname or an IP address, (\\quit) to end: "); //note: if hostname has quit, program will potentially terminate
                System.out.flush();

                //storing user entry into variable
                name = in.readLine();

                //this is how we allow user to quit
                //i included '\' in front of quit as I believe it's not a valid URL char
                //even if it is, users are at least less likely to enter \quit for a website
                //as they are quit
                if (name.indexOf("\\quit") < 0) {
                    getRemoteAddress(name, serverName); //this sends the website we wish to visit to the server
                }
            } while (name.indexOf("\\quit") < 0); //exits client server if user enters "\quit"
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
    static void getRemoteAddress(String name, String serverName){
        Socket sock;
        BufferedReader fromServer;
        PrintStream toServer;
        String textFromServer;

        try{
            //Open our connection to server port, choose your own port number..
            sock = new Socket(serverName, 4444); //this is client setting up ability to communicate with server

            //Create filter I/O streams for the socket:
            fromServer =
                    //opening input stream from server; sock has our server name and port number
                    new BufferedReader(new InputStreamReader(sock.getInputStream()));
            toServer = new PrintStream(sock.getOutputStream()); //opening output stream to our server
                                                                //sock has our server name and port number

            //Send machine name or IP address to server:
            toServer.println(name);
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
