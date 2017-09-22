import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Scanner;
import java.util.UUID;
/*--------------------------------------------------------

 1. Name / Date: Brandon Gomez / 9/19/2017

 2. Java version used, if not the official version for the class:

 build 1.8.0_144-b01

 3. Precise command-line compilation examples / instructions:

 > javac JokeClientAdmin.java

 4. Precise examples / instructions to run this program:

 In shell:
 > java JokeClientAdmin <IP addr> <IP addr>

 User can start the one server with the above command or have multiple server options by adding another IP.

 5. List of files needed for running the program.

 a. JokeClientAdmin.java


 5. Notes:
 Purpose of this class is to send commands to the JokeServer class. Specifically, this class will tell the JokeServer
 to change modes, get the current mode, and also to shutdown.

 ----------------------------------------------------------*/
public class JokeClientAdmin
{
    public static void main(String args[])
    {
        String username, serverName = "127.0.0.1", secondaryServername = "", targetServer = serverName;
        int targetPort = 5050;

        StringBuffer serverString = new StringBuffer();
        if (args.length > 0)
            targetServer = serverName = args[0];

        serverString.append(String.format("Server one: %s, port 5050.", serverName));

        if (args.length > 1)
        {
            secondaryServername = args[1];
            serverString.append(String.format("\nServer two: %s, port 5051.", secondaryServername));
        }

        System.out.println(serverString);
        System.out.println("Welcome to the joke and proverb administrative client.");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        try
        {
            String input = "";
            // Continually read user input until he or she quits program
            do
            {
                System.out.printf("Press enter to toggle joke and proverb mode, " +
                        "\"s\" to toggle secondary server, \"quit\" to end, and \"shutdown\" to shutdown server: \n");
                input = in.readLine();
                if (input.equalsIgnoreCase("shutdown"))
                    shutDownServer(serverName, targetPort);
                else if (input.equalsIgnoreCase("s") && !secondaryServername.isEmpty())
                {
                    // To switch to the other server, the logic here checks to see if the current
                    // targetserver is equal to the main server. If it is, switch to secondary, otherwise switch to main
                    targetServer = targetServer.equals(serverName) ? secondaryServername : serverName;

                    // similar logic for switching target ports
                    targetPort = targetPort == 5050 ? 5051 : 5050;
                    System.out.printf("Now communicating with: %s, port %d\n\n", targetServer, targetPort);
                }
                else if (input.equalsIgnoreCase("s"))
                    System.out.printf("No secondary server available\n\n");
                else if (!input.equalsIgnoreCase("quit")) // Only gets remote address if user doesn't type in "quit"
                {
                    // Toggle mode and return true if successful. If successful return current mode.
                    if (toggleMode(serverName, targetPort))
                        getCurrentMode(serverName, targetPort);
                }
            }
            while (!input.equalsIgnoreCase("quit")); // loop continues until user types in "quit"
            System.out.println("Cancelled by user request.");
        } catch (IOException x)
        {
            x.printStackTrace();
        }
    }

    // not interesting
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
     * Method Name: getCurrentMode
     * Parameters:
     *      String: server name
     *      String: target port
     * Purpose: Sends request to get the current mode of the server. Once received, output to client
     */
    private static void getCurrentMode(String serverName, int targetPort)
    {
        System.out.println(String.format("Current mode is: %s", talkToServer(serverName, targetPort, "mode")));
    }

    /*
    * Method Name: toggleMode
    * Parameters:
    *      String: server name
    *      String: target port
    * Purpose: Sends request to toggle mode of the server. If the talkToServer method returns back "unknown" then
    * there was an issue connecting to the server. Otherwise, this returns true.
    */
    private static boolean toggleMode(String serverName, int targetPort)
    {
       return !talkToServer(serverName, targetPort, "toggle").equals("unknown");
    }

    /*
    * Method Name: shutDownServer
    * Parameters:
    *      String: server name
    *      String: target port
    * Purpose: Sends request to shutdown server.
    */
    private static void shutDownServer(String serverName, int targetPort)
    {
        talkToServer(serverName, targetPort, "shutdown");
    }

    /*
    * Method Name: talkToServer
    * Parameters:
    *      String: server name
    *      String: target port
    *      String: command
    * Purpose: This method is responsible for sending commands and returning any messages from the server to the
    * caller. I created this method because the other three methods kept doing the same thing with only slight differences.
    */
    private static String talkToServer(String serverName, int targetPort, String command)
    {
        Socket sock;
        BufferedReader fromServer;
        PrintStream toServer;
        String textFromServer = "";

        try
        {
            // creates a connection to server at the target port
            sock = new Socket(serverName, targetPort);
            toServer = new PrintStream(sock.getOutputStream());
            toServer.flush();
            //sends command to server
            toServer.println(command);

            // Reads any data received from the server
            fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String serverResponse = fromServer.readLine();
            textFromServer = serverResponse == null ? textFromServer : serverResponse;
            // disconnect from server
            sock.close();
        } catch (IOException x)
        {
            System.out.println("Looks like there's a connection error. Is the server up? Try again (Y/N)?\n");
            Scanner in = new Scanner(System.in);
            if (in.next().equalsIgnoreCase("y"))
                return talkToServer(serverName, targetPort, command);
            else
                return "unknown";
        }
        return textFromServer;
    }
}
