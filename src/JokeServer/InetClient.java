import java.io.*; 
import java.net.*; 
/*--------------------------------------------------------

1. Name / Date: Brandon Gomez / 9/12/2017

2. Java version used, if not the official version for the class:

build 1.8.0_144-b01

3. Precise command-line compilation examples / instructions:

> javac InetClient.java


4. Precise examples / instructions to run this program:

In separate shell windows:
> java InetServer
> java InetClient [server name]

User is prompted to enter a host name, ip or (quit). One user types in 
a host name or IP and presses enter, server will return host name and host IP address.

User optionally can enter a specific server to connect to who's also running
InetServer.

5. List of files needed for running the program.

A. Wodker.java
B. InetServer.java
C. InetClient.java

----------------------------------------------------------*/
public class InetClient
{
    public static void main(String args[])
    {
        String serverName;
        // Grabs server name from user if they've entered one
        if (args.length < 1)
            serverName = "localhost";
        else
            serverName = args[0];

        System.out.println("Brandon Gomez's Inet Client, 1.8.\n");
        System.out.println("Using server: " + serverName + ", Port: 2500");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        try
        {
            String name;
            // Continually read user input until he or she quits program
            do
            {
                System.out.print("Enter a hostname or an IP address, \"quit\" to end: ");
                System.out.flush();
                name = in.readLine();
                if (name.indexOf("quit") < 0) // Only gets remote address if user doesn't type in "quit"
                    getRemoteAddress(name, serverName);
            }
            while (name.indexOf("quit") < 0); // loop continues until user types in "quit"
            System.out.println("Cancelled by user request.");
        }
        catch (IOException x)
        {
            x.printStackTrace();
        }
    }

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

    /*
     * Method Name: getRemoteAddress
     * Parameters: 
     *      String: host name
     *      String: server name
     * Purpose: Sends host name to server. This then receives host name & host ip 
     * and prints to screen.
     */
    static void getRemoteAddress(String name, String serverName)
    {
        Socket sock;
        BufferedReader fromServer;
        PrintStream toServer;
        String textFromServer;

        try
        {    
            // creates a connection to server at port 2500
            sock = new Socket(serverName, 5050);
            
            // Reads any data received from the server
            fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            
            // OutputStream object is used to send data back to the client. PrintStream
            // is a wrapper object that adds more functionality to that object so we
            // can send data differently/have more convenient methods
            toServer = new PrintStream(sock.getOutputStream());
            
            // Sends host name to server for lookup
            toServer.println(name);
            toServer.flush();

            // Server sends back three lines: "looking up", the host name, and the host ip.
            // This reads that data and prints to System.out
            for (int i = 1; i <= 3; i++)
            {
                textFromServer = fromServer.readLine();
                if (textFromServer != null)
                    System.out.println(textFromServer);
            }
            // disconnect from server
            sock.close();
        }
        catch (IOException x)
        {
            System.out.println("Socket error.");
            x.printStackTrace();
        }
    }
}