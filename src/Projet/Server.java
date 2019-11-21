package Projet;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.logging.Level;

public class Server implements Runnable {

    private static Loggs log;
    private static int cpt =1;
    private static String ipAddress;
    private int clientNo;
    private String list;
    private String fileName;
    private ObjectInputStream serverInputStream;
    private BufferedReader br;
    private FileWriter writer;
    private PrintWriter pout;
    private FileReader reader;
    private Socket threadSocket;

    /** CONSTRUCTOR **/
    public Server(Socket threadSocket, int clientNo){
        this.threadSocket = threadSocket;
        this.clientNo = clientNo;
    }

    public static void main(String[] args) {

        String interfaceName = "eth1";
        NetworkInterface netI;
        InetAddress localAddress = null;
        ServerSocket mySkServer;
        int clientNo = 55555;

        try {
            netI = NetworkInterface.getByName(interfaceName);
            Enumeration<InetAddress> inetAddresses = netI.getInetAddresses();
            while(inetAddresses.hasMoreElements()) {
                InetAddress ia = inetAddresses.nextElement();
                if(!ia.isLinkLocalAddress()) {
                    if(!ia.isLoopbackAddress()) {
                        System.out.println("\n\t*** START SERVER ***\nIP: " + ia.getHostAddress());
                        localAddress = ia;
                    }
                }
            }

            mySkServer = new ServerSocket(45000,10,localAddress);
            mySkServer.setSoTimeout(120000); //2 min
            System.out.println("Port: " +mySkServer.getLocalPort()+"\n\nWaiting connections...");

            while(true){
                // Accept severals clients with threads
                Socket threadSocket = mySkServer.accept();

                ipAddress = threadSocket.getRemoteSocketAddress().toString();

                System.out.println("\n----------\nClient "+cpt+" is connected "+ ipAddress);

                Thread t = new Thread(new Server(threadSocket, clientNo));
                cpt++;
                t.start();
            }
        } catch (SocketException e) {
            System.out.println("Connection Timed out");
            log.loggerCliSer.setLevel(Level.SEVERE);
            log.loggerCliSer.severe("Connection Timed out");
        } catch (IOException e) {
            e.printStackTrace();
            log.loggerCliSer.info("Error");
        }
    }


    public void run() {

        //init logs
        log = Loggs.getInstance();
        log.startLog();

        try {
            serverInputStream = new ObjectInputStream(threadSocket.getInputStream());
            pout = new PrintWriter(threadSocket.getOutputStream());
            BufferedReader buffin = new BufferedReader (new InputStreamReader (threadSocket.getInputStream()));

            //message_distant is a message send by the client
            String message_distant="";

            while(true){
                System.out.println("Waiting client request...");

                //reading the message from client
                message_distant = buffin.readLine().trim();

                switch(message_distant){
                    case "1":
                        System.out.println("\n**Client "+ipAddress+" is adding a file to the list...");
                        fileName = buffin.readLine();
                        //name of the file send by the client
                        writeInList(fileName);
                        break;

                    case "2": //the client ask the list
                        sendList();
                        System.out.println("List sent");
                        return;

                    case "5": //Ending program
                        closeConnections();
                        return;

                    default :
                        System.out.println("Error");
                        break;
                }
                closeConnections();
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.loggerCliSer.setLevel(Level.SEVERE);
            log.loggerCliSer.severe(e.getMessage());
        }
    }


    private void writeInList(String f) {
        try{

            writer = new FileWriter("FileList.txt", true); //adding files
            writer.write("\n Client "+ipAddress+" / File name: "+f); //write
            writer.close();
            System.out.println("File added correctly to the list.");

            //write in ServerLog when a client add a file to the list
            log.loggerCliSer.info("Client " + ipAddress + " add file: "+fileName+" in the list.");
        }catch(IOException e) {
            e.printStackTrace();
            log.loggerCliSer.setLevel(Level.SEVERE);
            log.loggerCliSer.severe(e.getMessage());
        }
    }


    private void sendList() {
        try{
            reader = new FileReader("FileList.txt");
            br = new BufferedReader(reader);
            System.out.println("\nFile list asked by the client.");
            pout = new PrintWriter(threadSocket.getOutputStream());

            while((list = br.readLine()) != null){
                pout.println(list);
            }

            //write in ServerLog when the server send the list to the client
            log.loggerCliSer.info("\nFilelist send to the client");

            closeReader(); //closind reader

        }catch(IOException e) {
            e.printStackTrace();
            log.loggerCliSer.setLevel(Level.SEVERE);
            log.loggerCliSer.severe(e.getMessage());
        }
    }


    private void closeReader() {
        System.out.println("Closing Reader...");
        try{
            reader.close();
            pout.flush();
            threadSocket.close();
            pout.close();
            System.out.println("File list sent.");
        }catch(IOException e) {
            e.printStackTrace();
            log.loggerCliSer.setLevel(Level.SEVERE);
            log.loggerCliSer.severe(e.getMessage());
        }
    }


    private void closeConnections(){
        System.out.println("\n*** END SERVER ***");
        System.exit(0);
    }
}
