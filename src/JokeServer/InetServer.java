import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.*;

/*--------------------------------------------------------

 1. Name / Date: Brandon Gomez / 9/12/2017

 2. Java version used, if not the official version for the class:

 build 1.8.0_144-b01

 3. Precise command-line compilation examples / instructions:

 > javac InetServer.java


 4. Precise examples / instructions to run this program:

 In shell:
 > java InetServer

 Program accepts a hostname from a client and does an IP lookup based
 on that hostname. Both IP and Hostname are returned to the client.

 5. List of files needed for running the program.

 a. InetServer.java


 5. Notes:
 You could say that InetClient.java is also a file needed
 for this program but that's not completely true. This program
 will accept input from any client who sends data to port 2500

 ----------------------------------------------------------*/

class Wodker extends Thread
{ 
    Socket sock; 

    // Class constructor which will receive the Socket passed by the server
    Wodker(Socket s)
    {
        sock = s;
    } 

    // This is always run for Thread classes. In a way its the class's "main" method
    public void run()
    {
        
        PrintStream out = null;
        BufferedReader in = null;
        try
        {
            // Read data sent by client and received on the socket
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            
            // OutputStream object is used to send data back to the client. PrintStream
            // is a wrapper object that adds more functionality to that object so we
            // can send data differently/have more convenient methods
            out = new PrintStream(sock.getOutputStream());
            
            try
            {
                String name;
                name = in.readLine();
                System.out.println("Looking up " + name);
                printRemoteAddress(name, out);
            }
            catch (IOException x)
            {
                System.out.println("Server read error");
                x.printStackTrace();
            }
            sock.close(); 
        }
        catch (IOException ioe)
        {
            System.out.println(ioe);
        }
    }

    /*
     * Method Name: printRemoteAddress
     * Parameters: 
     *      String: name of host user is looking for
     *      PrintStream: PrintStream used to send data back to client
     * Purpose: Sends back the host name and host IP to the client
     */
    static void printRemoteAddress(String name, PrintStream out)
    {
        try
        {
            out.println("Looking up " + name + "..."); // sent to client
            InetAddress machine = InetAddress.getByName(name); // Gets information about a given hostname
            out.println("Host name : " + machine.getHostName()); // sent to client 
            out.println("Host IP : " + toText(machine.getAddress())); // sent to client
        }
        catch (UnknownHostException ex)
        {
            out.println("Failed in atempt to look up " + name); // sent to client if UnknownHostException 
        }
    }

    // Not interesting
    static String toText(byte ip[])
    { 
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < ip.length; ++i)
        {
            if (i > 0)
                result.append(".");
            result.append(0xff & ip[i]);
        }
        return result.toString();
    }
}

public class InetServer
{

    public static void main(String a[]) throws IOException
    {
        // max queue length
        int q_len = 6;
        int port = 2500;
        Socket sock;

        // server specific socket which will connect to clients
        ServerSocket servsock = new ServerSocket(port, q_len);

        System.out
                .println("Brandon Gomez's Inet server 1.8 starting up, listening at port 2500.\n");
        // Infinite while loop to continually listen for client connections on the socket
        while (true)
        {
            sock = servsock.accept();
            // Spawns new worker thread which will handle the looking up of hostnames,
            // returning information back to client, and closing the socket
            new Wodker(sock).start();
        }
    }
}