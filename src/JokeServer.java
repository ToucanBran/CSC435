import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/*--------------------------------------------------------

 1. Name / Date: Brandon Gomez / 9/19/2017

 2. Java version used, if not the official version for the class:

 build 1.8.0_144-b01

 3. Precise command-line compilation examples / instructions:

 > javac JokeServer.java


 4. Precise examples / instructions to run this program:

 In shell:
 > java JokeServer [secondary]

 User can start the one server with the above command and multiple servers by adding the secondary keyword.

 5. List of files needed for running the program.

 a. JokeServer.java


 5. Notes:

 ----------------------------------------------------------*/
/*
    Name: JokeWorker Class

    Parameters: Socket, hashmap containing list of sessions, the current mode of the server, and a boolean to
    state whether it's working off of the secondary or main server.

    Purpose: The jokeworker thread is responsible for looking up user sessions and outputting the correct joke/proverb
 */
class JokeWorker extends Thread
{
    private Socket sock;
    private HashMap<String, Session> sessions;
    private Mode currentMode;
    private boolean isSecondary;
    public String[] jokesAndProverbs = new String[8];


    // Class constructor which will receive the Socket passed by the server
    JokeWorker(Socket s, HashMap<String, Session> sessions, Mode currentMode, boolean isSecondary)
    {
        sock = s;
        this.sessions = sessions;
        this.currentMode = currentMode;
        this.isSecondary = isSecondary;
    }

    // This is always run for Thread classes. In a way its the class's "main" method
    public void run()
    {

        PrintStream out = null;
        String user = null;
        BufferedReader br;
        try
        {
            out = new PrintStream(sock.getOutputStream());
            out.flush();

            // Read data sent by client and received on the socket
            br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            user = br.readLine();

            // Checks if the user has a session already. If not, create a new one
            if (!sessions.containsKey(user))
                sessions.put(user, new Session(user));

            // I create the jokes here so if one of these jokes are incorrect, you only need to change it once in this spot.
            // Since they're not stored in the session, if you update these jokes, the session will also update with the
            // new ones.
            jokesAndProverbs[0] = String.format("PA %s : The pen is mightier than the sword.", sessions.get(user).getUsername());
            jokesAndProverbs[1] = String.format("PB %s : The squeaky wheel gets the grease.", sessions.get(user).getUsername());
            jokesAndProverbs[2] = String.format("PC %s : You miss 100 percent of the shots you don't take.", sessions.get(user).getUsername());
            jokesAndProverbs[3] = String.format("PD %s : Don't put all your eggs in one basket.", sessions.get(user).getUsername());
            jokesAndProverbs[4] = String.format("JA %s : Why is 6 afraid of 7? It's not. Numbers are non-sentient and " +
                    "therefore cannot feel fear.", sessions.get(user).getUsername());
            jokesAndProverbs[5] = String.format("JB %s : A dad is washing the car with his son. After a few moments the son asks, 'Dad, could you" +
                    " use a sponge instead?", sessions.get(user).getUsername());
            jokesAndProverbs[6] = String.format("JC %s :  To this day, the boy who used to bully me still takes my lunch money. On the plus side," +
                    " he makes a great Subway sandwich", sessions.get(user).getUsername());
            jokesAndProverbs[7] = String.format("JD %s :  Why is Peter Pan always flying? He neverlands...", sessions.get(user).getUsername());

            StringBuffer message = new StringBuffer();

            // Builds for joke/proverb
            if (isSecondary)
                message.append("<S2> ");

            Integer jokeProverbIndex = sessions.get(user).getJokeOrProverb(currentMode);
            message.append(jokesAndProverbs[jokeProverbIndex]);

            // Send output to client
            out.println(message);
            sock.close();
        } catch (Exception e)
        {
            e.printStackTrace();
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

/*
    Name: Session Class

    Parameters: String UUID

    Purpose: This is an object that will hold details of a specific user's session. It will contain a list of available
    jokes and proverbs. When the user runs out of jokes/proverbs, the list is randomized and reset.
 */
class Session
{
    //uuid always in form of [username]uuid:[uuid]
    private String uuid;
    private String username;
    // full list of joke indices. Used for restore purposes
    private ArrayList<Integer> jokes = new ArrayList<>();
    // full list of proverb indices. Used for restore purposes
    private ArrayList<Integer> proverbs = new ArrayList<>();
    //Hashmap which contains two keys: "jokes" and "proverbs". The value at each key is an Arraylist of indices which
    // will be used to pull from a joke/proverb array
    private HashMap<String, ArrayList<Integer>> jokesAndProverbs = new HashMap<>();

    public Session(String uuid)
    {
        this.uuid = uuid;
        this.username = uuid.split("uuid:")[0]; // extract username

        for (int i = 0; i < 4; i++)
        {
            // Joke and proverb array is of length 8. The bottom half = proverbs, top half = jokes. Here I'm assigning
            // indices to the proverbs and jokes arraylists.
            proverbs.add(i);
            jokes.add(i + 4);
        }

        Mode mode = new Mode();
        addJokesOrProverbs(mode);
        mode.toggleMode();
        addJokesOrProverbs(mode);
    }

    public String getUsername()
    {
        return username;
    }

    public Integer getJokeOrProverb(Mode mode)
    {
        if (!isAvailable(mode))
            addJokesOrProverbs(mode);

        return jokesAndProverbs.get(mode.getCurrentMode()).remove(0);
    }

    // Checks if jokes or proverbs are available
    private boolean isAvailable(Mode mode)
    {
        return !jokesAndProverbs.get(mode.getCurrentMode()).isEmpty();
    }

    // This method will reset and shuffle the joke/proverb array when the user runs out.
    private void addJokesOrProverbs(Mode mode)
    {
        ArrayList<Integer> newAdds = new ArrayList<>();
        ArrayList<Integer> source = mode.getCurrentMode().equals("joke") ? jokes : proverbs;
        Collections.shuffle(source);

        newAdds.addAll(source);
        jokesAndProverbs.put(mode.getCurrentMode(), newAdds);
    }
}

/*
    Name: Mode Class

    Parameters: None

    Purpose: This is an object that will hold the current mode of the server. It always defaults to joke and can only be
    changed by having the toggleMode method called
 */
class Mode
{
    private String currentMode = "joke";

    public String getCurrentMode()
    {
        return currentMode;
    }

    public void toggleMode()
    {
        this.currentMode = currentMode.equals("joke") ? "proverb" : "joke";
    }
}

/*
    Name: JokeServer Class

    Parameters: Optional parameter [secondary] which changes the server to list on port 4546

    Purpose: This class is responsible for handling communication with both the JokeClient and JokeClientAdmin classes.
    When communicating with the JokeClient class, it will keep track of a user's conversations inside of a "Session"
    object. Using the "Session", it will output jokes and proverbs based on the classes current mode. Interactions
    with the JokeClientAdmin class include receiving instructions to shutdown and toggle & return the class's current mode
 */
public class JokeServer
{
    public static void main(String args[]) throws IOException
    {
        Mode currentMode = new Mode();
        HashMap<String, Session> sessions = new HashMap<>();
        final boolean isSecondary = args.length > 0 && args[0].equals("secondary") ? true : false;
        final int q_len = 6; /* Number of requests for OpSys to queue */
        final int PORT_ADMIN = isSecondary ? 5051 : 5050;
        final int PORT_CLIENT = isSecondary ? 4546 : 4545;
        final AtomicBoolean jokeServerOn = new AtomicBoolean(true);
       // HashMap<Integer, String> jokesAndProverbs = new HashMap<>();

        // ServerSocket for client. This is defined up here so that the admin thread has access to close the socket when
        // it receives the shutdown command. This is the only way to shutdown the client right after the command is
        // given. Otherwise, the program is still blocked listening for client connections.
        ServerSocket servsock = new ServerSocket(PORT_CLIENT, q_len);

        // Listen for admin instructions
        new Thread(() ->
        {
            Socket sockAdmin;
            PrintStream out;

            try
            {
                // Listen for incoming connections from the admin client
                ServerSocket servSockAdmin = new ServerSocket(PORT_ADMIN, q_len);
                while (jokeServerOn.get())
                {
                    sockAdmin = servSockAdmin.accept();
                    System.out.printf("Incoming request from: %s, port %s\n", sockAdmin.getLocalAddress(), sockAdmin.getLocalPort());
                    // Read data sent by client and received on the socket
                    BufferedReader in = new BufferedReader(new InputStreamReader(sockAdmin.getInputStream()));
                    try
                    {
                        out = new PrintStream(sockAdmin.getOutputStream());
                        out.flush();

                        String textFromAdmin = in.readLine();
                        if (textFromAdmin.equals("mode"))
                            out.println(currentMode.getCurrentMode());
                        else if (textFromAdmin.equals("toggle"))
                            currentMode.toggleMode();
                        else if (textFromAdmin.equals("shutdown"))
                        {
                            jokeServerOn.set(false);
                            servsock.close();
                        }
                    } catch (IOException x)
                    {
                        System.out.println("Server read error");
                        x.printStackTrace();
                    }
                    sockAdmin.close();
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }).start();

        Socket sock;

        System.out
                .printf("Brandon Gomez's Joke server 1.8 starting up, listening at port %s.\n", PORT_CLIENT);
        // Infinite while loop to continually listen for client connections on the socket
        while (jokeServerOn.get())
        {
            try
            {
                sock = servsock.accept();
                System.out.printf("Incoming request from: %s, port %s\n", sock.getLocalAddress(), sock.getLocalPort());
                // Spawns new jokeworker thread which will handle communication between server and client
                new JokeWorker(sock, sessions, currentMode, isSecondary).start();
            } catch (SocketException s)
            {
                System.out.println("Received shutdown command. Closing down...");
                System.exit(0);
            }
        }
    }
}