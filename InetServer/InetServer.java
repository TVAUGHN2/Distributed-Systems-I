

/**Travis Vaughn | 4/3/2016
 * File is: InetClient.java, Version 1.8
 * >javac InetServer
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
 * A server for InetClient. Elliott, after Hughes, Shoffner, Winslow
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
            //creating the input stream to the server from the client (where it takes in the host name/ip address
            in = new BufferedReader
                    (new InputStreamReader(sock.getInputStream()));
            //creating the output stream where we print out on the server screen what we're looking up
            out = new PrintStream(sock.getOutputStream());
            // Note that this branch might not execute when expected:
            try {
                String name;
                name = in.readLine(); //stores the value input on the client side
                System.out.println("Looking up " + name); //prints out that we're looking up host name/ ip
                printRemoteAddress(name, out); //prints details back to client
            } catch(IOException x) { //if an IO error when reading in line from client, will catch exception and print below error
                System.out.println("Server read error");
                x.printStackTrace();
            }
        } catch(IOException ioe)    {System.out.println(ioe);} //if error when opening IO streams, will catch exception and print it to screen
    }

    /* Prints to client: input name given, the host name of the input, and the host IP
     * Also handles if unknown exception occurs
     */
    static void printRemoteAddress (String name, PrintStream out) {
        try {
            out.println("Looking up " + name + "...");
            InetAddress machine = InetAddress.getByName(name);
            out.println("Host name : " + machine.getHostName()); //To client...
            out.println("Host IP : " + toText(machine.getAddress()));
        } catch(UnknownHostException ex){
            out.println("Failed in attempt to lookup " + name);
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

public class InetServer {

    /* This is the code that executes the server
     * Generates infinite loop to keep handling client requests
     * With each new request, a new thread is created to handle request
     */
    public static void main(String a[]) throws IOException{
        int q_len = 6; //Not interesting. Number of requests for Opsys to queue
        int port = 4444; //this is how the server and client know where to send/receive requests on the server.
                        // it specifically identifies the process, since a server can handle multiple different requests
        Socket sock;

        ServerSocket servsock = new ServerSocket(port, q_len); //initializes new server socket

        System.out.println
                ("Travis Vaughn's Inet server 1.8 starting up, listening at port 4444.\n");
        //this is the main server routine.  It listens for client requests and then sends their requests
        //off to new threads for the Worker class to handle
        while (true){
            sock = servsock.accept(); //wait for the next client connection
            new Worker(sock).start(); //Spawn worker to handle it; this is the multi-threading
                                      //creating a new thread each time a new client request comes in
        }
    }
}
