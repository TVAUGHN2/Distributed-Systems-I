
/**Travis Vaughn | 5/8/2016
 * File is: BCHandler.java, Version 1.8
 *
 * Overview of program: 
 * Capture Environment Variables passed from .bat file through java.exe.
 * Assuming the first argument is a valid file name, read five lines
 * of data from the file, and display the data on the console.
 * Then send serialized xml data to MyWebServer.java
 *
 * For use with webserver back channel @ port 2570. Written for Windows.

----Example of how to compile:---- 

 * rem jcxbhandler.bat
 * rem java compile BCHandler.java with xml libraries.
 * rem change this path to point to your own .jar file locations:
 * javac -cp "c:\Program Files\Java\jdk1.8.0_77\lib\xstream-1.2.1.jar;c:\Program Files\Java\jdk1.8.0_77\lib\xpp3_min-1.1.3.4.O.jar" BCHandler.java

 * Requires the Xstream libraries contained in .jar files to compile, AND to run.

----To run:----
 * Will be automatically run from web browser assuming, file extension/association are setup correctly. 
 * To test via command line, see below:

 * Here is the DOS .bat file to run this Java program:
 * rem This is shim.bat
 * rem Change this to your development directory:
 * cd C:\Users\Travis\
 * rem Change this to point to your Handler directory:
 * cd C:\Users\Travis\
 * rem have to set classpath in batch, passing as arg does not work.
 * rem Change this to point to your own Xstream library files:
 * set classpath=%classpath%C:\Users\Travis\mime-xml\;c:\Program Files\Java\jdk1.8.0_77\lib\xstream-1.2.1.jar;c:\Program Files\Java\jdk1.8.0_77\lib\xpp3_min-1.1.3.4.O.jar;
 * rem pass the name of the first argument to java:
 * java -Dfirstarg=%1 BCHandler

 * > shim mimer-data.xyz

 * ...where mimer-data.xyz has five lines of ascii data in it.

----Additional Notes:----
 *** CREATING A FILE EXTENSION ***
 *    Open the Windows Registry (regedit) via Windows Explorer
 *    Go to HKEY_CLASSES_ROOT\MIME\Database\Content Type
 *    Add a new content type 
 *	     -> right-click and select New
 *	     -> Select Key
 *	     -> type in application/[extension] ...ex: application/xyz
 *	     -> right-click on Key and select New String Value
 *	     -> add "Extension"
 *	     -> right-click modify, then assign value = ".xyz"
 *
 *** ADDING FILE EXTENSION TO INDEXING OPTION ***
 *    Go to Control Panel > Indexing options > Advanced > File types tab.
 *    At the bottom, you should see : Add new extension to list
 *    Type the new extension & click add > OK
 *
 *** ASSOCIATING FILE TYPE WITH YOUR PROGRAM (.xyz with shim.bat in our case) ***
 *    Search for "Change the file type" in your windows start search.
 *    Select "Change the file type with a file extension"
 *    Find your extension
 *    Select Change program
 *    And either browse for your associated program or type it in (shim.bat)
 *
 ----------------------------------------------------------------------------------------------*/


import java.io.*;
import java.util.*;
import java.net.*; // Get the Java networking libraries
import com.thoughtworks.xstream.XStream; //Get XStream libraries
import com.thoughtworks.xstream.io.xml.DomDriver; //Get xml i/o libraries

//structure to hold input from mimer data
class myDataArray {
  int num_lines = 0;
  String[] lines = new String[8];
}

/* This class takes the file argument passed when running shim.bat and creates/saves an 
 * XML doc. to a temporary location and passing data back to server
 */
public class BCHandler {
  private static String XMLfileName = "C:\\temp\\mimer.output"; //location to save/retrieve temporary output
  private static PrintWriter      toXmlOutputFile;
  private static File             xmlFile;
  private static BufferedReader   fromMimeDataFile;

  public static void main(String args[]) {
    int i = 0;
    BufferedReader in =
      new BufferedReader(new InputStreamReader(System.in));
    myDataArray da = new myDataArray(); //storing text from file
	XStream xstream = new XStream(); //creating object to handle xml data
	String serverName = "localhost"; //hard-coding to local server
	
    try {
      System.out.println("Executing the java application.");System.out.flush();
	  //creates variable for all key/association values
      Properties p = new Properties(System.getProperties()); 
      
	  //passing first argument after shim.bat runs via environment variable 
	  //In this case this would return our file name and the association in order to open and run it
      String argOne = p.getProperty("firstarg");  
      System.out.println("First var is: " + argOne);
      
	  //opening stream to read from file
      fromMimeDataFile = new BufferedReader(new FileReader(argOne));
      // Only allows for five lines of data in input file plus safety:
      while(((da.lines[i++] = fromMimeDataFile.readLine())!= null) && i < 8){
			System.out.println("Data is: " + da.lines[i-1]); //printing out data
      }
      da.num_lines = i-1; //reduce by one as false clause of while will still cause i to increment
      System.out.println("i is: " + i); //printing num. of lines

	  
	  /*Begin client processing*/
	  String xml = xstream.toXML(da); //serializes the object myDataArray and all of it's contents
	  sendToBC(xml, serverName); //sending serialized data to server
	  
/*	  
	  //printing xml version of data to client
	  System.out.println("\n\nHere is the XML version:");
	  System.out.print(xml);
		  
	  //printing deserialized data, a.k.a. the normal text, back to client
	  daTest = (myDataArray) xstream.fromXML(xml); // deserialize data
	  System.out.println("\n\nHere is the deserialized data: ");
	  for(i=0; i < daTest.num_lines; i++){System.out.println(daTest.lines[i]);}
	  System.out.println("\n");
*/	  
	  
      xmlFile = new File(XMLfileName); //opens file at temporary location
      if (xmlFile.exists() == true && xmlFile.delete() == false){
		throw (IOException) new IOException("XML file delete failed.");
      } //throws exception if the file is there and cannot be deleted
      xmlFile = new File(XMLfileName);
      if (xmlFile.createNewFile() == false){
		throw (IOException) new IOException("XML file creation failed.");
      } //throws exception is unable to create a new file at that location
      else{ //otherwise continue with creating xml file in temp location and printing to console
		toXmlOutputFile = 
			new PrintWriter(new BufferedWriter(new FileWriter(XMLfileName)));
		toXmlOutputFile.println("First arg to Handler is: " + argOne + "\n");
		toXmlOutputFile.println(xml); //printing xml to temp file
		toXmlOutputFile.close();
      }
    }
    catch (Throwable e) { //handles error
      e.printStackTrace();
    }
  }
  
  /*Sends & receives serialized data to/from server in order to pass back to client*/
  static void sendToBC (String sendData, String serverName){
    Socket sock;
    BufferedReader fromServer;
    PrintStream toServer;
    String textFromServer;
    try{
      // Open our connection Back Channel on server:
      sock = new Socket(serverName, 2570);
      toServer   = new PrintStream(sock.getOutputStream());
      // Will be blocking until we get ACK from server that data sent
      fromServer = 
			new  BufferedReader(new InputStreamReader(sock.getInputStream()));
      
	  //sending serialized data to server
      toServer.println(sendData);
      toServer.println("end_of_xml");
      toServer.flush(); 
      // Read two or three lines of response from the server,
      // and block while synchronously waiting:
      System.out.println("Blocking on acknowledgment from Server... ");
      textFromServer = fromServer.readLine();
      if (textFromServer != null){System.out.println(textFromServer);} //prints our serialized data to client
      sock.close();
    } catch (IOException x) {
      System.out.println ("Socket error.");
      x.printStackTrace ();
    }
  }
  
}