import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

/*--------------------------------------------------------

 1. Name / Date: Brandon Gomez / 9/19/2017

 2. Java version used, if not the official version for the class:

 build 1.8.0_144-b01

 3. Precise command-line compilation examples / instructions:

 > javac JokeClient.java

 4. Precise examples / instructions to run this program:

 In shell:
 > java JokeClient <IP addr> <IP addr>

 User can start the one server with the above command or have multiple server options by adding another IP. With two
 servers up, user can toggle which one to talk to.

 5. List of files needed for running the program.

 a. JokeClient.java


 5. Notes:
 Purpose of this class is receive jokes and proverbs from the server.

 ----------------------------------------------------------*/

public class JokeClient
{
    public static void main(String args[])
    {
        String username, serverName = "127.0.0.1", secondaryServername = "", targetServer = serverName;
        int targetPort = 4545;

        StringBuffer serverString = new StringBuffer();
        if (args.length > 0)
            targetServer = serverName = args[0];

        serverString.append(String.format("Server one: %s, port 4545.", serverName));

        if (args.length > 1)
        {
            secondaryServername = args[1];
            serverString.append(String.format("\nServer two: %s, port 4546.", secondaryServername));
        }

        System.out.println(serverString);
        System.out.println("Welcome to the joke and proverb client. Please enter your name: ");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        try
        {
            username = in.readLine();
            //Makes sure that user enters in a username
            while (username == null || username.isEmpty())
            {
                System.out.println("Hmmm... doesn't look like you gave me a name. Try again:\n ");
                username = in.readLine();
            }

            //Attach UUID to username to make sure we can have unique sessions
            username += "uuid:" + UUID.randomUUID();
            String input = "";
            // Continually read user input until he or she quits program
            do
            {
                System.out.print("Press enter for a joke or proverb, \"s\" to toggle secondary server, and \"quit\" to end: ");

                input = in.readLine();
                if (input.equalsIgnoreCase("s") && !secondaryServername.isEmpty())
                {
                    // To switch to the other server, the logic here checks to see if the current
                    // targetserver is equal to the main server. If it is, switch to secondary, otherwise switch to main
                    targetServer = targetServer.equals(serverName) ? secondaryServername : serverName;

                    // similar logic for switching target ports
                    targetPort = targetPort == 4545 ? 4546 : 4545;
                    System.out.printf("Now communicating with: %s, port %d\n", targetServer, targetPort);
                }
                else if (input.equalsIgnoreCase("s"))
                    System.out.println("No secondary server available");
                else if (!input.equalsIgnoreCase("quit"))
                    getJokeOrProverb(username, targetServer, targetPort);
            }
            while (!input.equalsIgnoreCase("quit")); // loop continues until user types in "quit"
            System.out.println("Cancelled by user request.");
        }
        catch (IOException x)
        {
            x.printStackTrace();
        }
    }
    //not interesting
    private static String toText(byte ip[])
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
     * Method Name: getJokeOrProverb
     * Parameters: 
     *      String: unique user id
     *      String: server name
     *      int: target port
     * Purpose: This will request that some joke or proverb is sent back to JokeClient from the server
     */
    private static void getJokeOrProverb(String uuid, String serverName, int targetPort)
    {
        Socket sock;
        BufferedReader fromServer;

        PrintStream toServer;
        String textFromServer;

        try
        {
            // creates a connection to server at the target port
            sock = new Socket(serverName, targetPort);

            // Reads any data received from the server
            fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            // OutputStream object is used to send data back to the client. PrintStream
            // is a wrapper object that adds more functionality to that object so we
            // can send data differently/have more convenient methods
            toServer = new PrintStream(sock.getOutputStream());

            // Sends uuid to server so it can find our session and return the jokes or proverbs available to it.
            toServer.println(uuid);
            toServer.flush();

            textFromServer = fromServer.readLine();
            if (textFromServer != null)
                System.out.println(textFromServer);
            // disconnect from server
            sock.close();
        }
        catch (IOException x)
        {
            System.out.println("Looks like there's a connection error. Is the server up? Try again (Y/N)?");
            Scanner in = new Scanner(System.in);

            if (in.next().equalsIgnoreCase("y"))
                getJokeOrProverb(uuid, serverName, targetPort);
            else
                return;
        }
    }
}