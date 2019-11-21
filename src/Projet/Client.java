package Projet;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.logging.Level;
import java.io.OutputStream;



public class Client extends Thread{

    private static int port = 11111;
    private static String serverIP = "172.20.10.2";
    private static Loggs  log;
    private String path;
    private String list;
    private String clientIp;
    private String serverName = "172.20.10.2";
    private String clientName = "127.0.0.1";
    private String message_distant ="";
    private PrintWriter pout;
    private String fileName;
    private File directory;
    private File file;
    private BufferedReader br;
    private Socket mySocket;
    private Socket  svrSocket;     //client sending files
    private Socket mySocketClient; //client receiving files
    private ObjectOutputStream clientOutputStream = null;
    private OutputStream outputstream = null;
    private InputStream inputStream;
    private FileOutputStream fileOutputStream;
    private FileInputStream fileInputStream;
    private BufferedInputStream bufferedInputStream;
    private BufferedOutputStream bufferedOutputStream;
    private InetAddress serverAddress;
    private InetAddress client2Address;
    private InetAddress local=null;
    private InetAddress clientServerAddress;
    private Scanner scan;
    private Scanner scan1;


    /** CONSTRUCTORS **/

    public Client(String ip, int port){
        this.serverIP = ip;
        this.port = port;
    }

    public static void main(String[] args) {

        System.out.println("\n\t*** START CLIENT ***");
        Client client = new Client(serverIP,port);
        client.run();
    }


    public void run() {

        //Initialize logging
        log = Loggs.getInstance();
        log.startLog();

        try {
            //Communication/Informations
            serverAddress = InetAddress.getByName(serverName);
            client2Address = InetAddress.getByName(clientName);

            //try to connect to the server
            mySocket = new Socket(serverAddress,45000);
            System.out.println("Client got the connection to  "+ serverAddress);
            pout = new PrintWriter(mySocket.getOutputStream());
            clientOutputStream = new ObjectOutputStream(mySocket.getOutputStream());

            //write in ServerLog when a client is connecting
            log.loggerCliSer.info("\n"+ "Client connected to server: "+serverAddress);

            askQuestions();

            //end of the program
            System.out.println("\n*** END CLIENT ***");

        }catch (IOException e) {
            System.out.println("Server connection error.");
            log.loggerCliSer.setLevel(Level.WARNING);
            log.loggerCliSer.warning(e.getMessage());
        }
    }

    private void askQuestions() throws IOException{
        scan = new Scanner(System.in);

        //give choice to the client
        System.out.println("\n\tWhat do you want to do?");
        System.out.println("\t1 : create a file and add it to the list");
        System.out.println("\t2 : ask the list of files to the server");
        System.out.println("\t3 : ask a file to a client");
        System.out.println("\t4 : become server");
        System.out.println("\t5 : end the program");
        System.out.print("Answer : ");

        //message_distant for communicate with server
        message_distant = scan.next();

        switch(message_distant)
        {
            case "1": //create a file and add it to the list on server

                pout.println("1"); //send to server
                askFileName(); //ask name of the file to the client
                pout.println(fileName); //send fileName to server
                pout.flush();
                createFile(); 	//create a file
                printPath();
                System.out.println("File added to the list.");
                //write in ServerLog when a client add a file to tje list
                log.loggerCliSer.info("Client added file "+ fileName +"to the list.");
                break;

            case "2": //ask the list on the server
                System.out.println("File list asked to server");
                pout.println("2"); //send to server
                pout.flush();
                readList(); //read the list
                break;

            case "3": //ask a file to a client
                System.out.println("Connect and ask file to another client...");
                askFileName(); //ask name of the file to the client
                connectToClient(); //connect to the client
                receiveFile(fileName); //receive the file
                log.loggerCliSer.info("Client asked "+ fileName +"to the client.");

                break;

            case "4": //the client become a server
                System.out.println("Client switch like server...");
                acceptClientConnection(); //wait connection from other clients
                System.out.println("Client is server");
                sendFile();
                return;

            case "5": //Quit
                pout.println("5"); //send to the server
                pout.flush();
                System.out.println("Quit");
                log.loggerCliSer.info("Client disconnected");
                return;
        }
    }

    private void connectToClient() throws IOException {

        /** Procedure
         1) Open ubuntu with Client 2 (192.168.108.6)
         2) enter 5: switch in server
         3) Open ubuntu with Client 1 (192.168.108.5)
         4) enter 3: connect to client
         **/

        String client2IP = "192.168.108.6";
        clientServerAddress = InetAddress.getByName(client2IP);

        //create an other socket for connecting with client
        mySocketClient = new Socket(clientServerAddress,46000);

        System.out.println("Client is connected to another client...\n");
        log.loggerCliSer.info("Client "+client2IP+" is connected to another client...\n");
    }


    //the client do the server
    private void acceptClientConnection() throws IOException{

        ServerSocket acceptClientSocket ;
        String interfaceName = "eth1";

        try {
            NetworkInterface ni2 = NetworkInterface.getByName(interfaceName);
            Enumeration<InetAddress> inetAddresses2 =  ni2.getInetAddresses();
            while(inetAddresses2.hasMoreElements()) {
                InetAddress ia = inetAddresses2.nextElement();
                if(!ia.isLinkLocalAddress()) {
                    if(!ia.isLoopbackAddress()) {
                        System.out.println(ni2.getName() + "->IP: " + ia.getHostAddress());
                        local = ia;
                    }
                }
            }
            System.out.println("Client is waiting client connection");
            acceptClientSocket = new ServerSocket(46000,5,local);

            clientIp=local.toString();
            svrSocket = acceptClientSocket.accept(); //accept the client

        }catch (SocketException e) {
            e.printStackTrace();
            log.loggerCliSer.setLevel(Level.SEVERE);
            log.loggerCliSer.severe(e.getMessage());
        }
    }


    private void askFileName(){
        System.out.println("\nWhat is the name of the file? (name + extension)");
        scan1 = new Scanner(System.in);
        fileName = scan1.nextLine();
    }


    private void createFile(){
        try {
            //create directory in ubuntu
            directory = new File("C:\\Users\\marcv\\Desktop\\HES-SO\\Semestre3\\Prog_distr\\test");
            directory.mkdir();

            file = new File(directory, fileName);//create file
            file.createNewFile();
            log.loggerCliSer.info("Client added a file: "+fileName);
            System.out.println("\nFile created");
        } catch (IOException e) {
            e.printStackTrace();
            log.loggerCliSer.setLevel(Level.SEVERE);
            log.loggerCliSer.severe(e.getMessage());
        }
    }


    private void printPath(){
        path = directory.getAbsolutePath();
        System.out.println("The path of the file is " + path + "\\" + fileName);
    }


    private void readList() throws IOException{
        br = new BufferedReader (new InputStreamReader (mySocket.getInputStream()));
        while((list = br.readLine()) != null) {
            System.out.println(list);
        }
        System.out.println("List received");
    }

    //only client 1 can receive the file
    private void receiveFile(String fileName) throws IOException{
        final int FILE_SIZE = 6022386; //give a big size for the file
        final String FILE_TO_RECEIVED = "C:\\Users\\marcv\\Desktop\\HES-SO\\Semestre3\\Prog_distr\\test"+fileName; //path
        byte [] mybytearray  = new byte [FILE_SIZE];
        int bytesRead;
        int current = 0;

        inputStream = mySocketClient.getInputStream();
        fileOutputStream = new FileOutputStream(FILE_TO_RECEIVED);
        bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        bytesRead = inputStream.read(mybytearray,0,mybytearray.length);
        current = bytesRead;

        //reveive file
        do {
            bytesRead =
                    inputStream.read(mybytearray, current, (mybytearray.length-current));
            if(bytesRead >= 0) current += bytesRead;
        } while(bytesRead > -1);

        bufferedOutputStream.write(mybytearray, 0 , current);
        bufferedOutputStream.flush();
        System.out.println("File " + FILE_TO_RECEIVED+ " downloaded (" + current + " bytes read)");

        //close streams
        if (fileOutputStream != null) fileOutputStream.close();
        if (bufferedOutputStream != null) bufferedOutputStream.close();
        if (mySocketClient != null) mySocketClient.close();
    }

    //only client 2 can send files
    private void sendFile() throws IOException {
        /**because we hadn't the time to do correctly, we put an existant fileName: TEST.txt **/
        final String FILE_TO_SEND = "\\Users\\marcv\\Desktop\\HES-SO\\Semestre3\\Prog_distr\\test\\test.txt";

        File myFile = new File (FILE_TO_SEND);
        byte [] mybytearray  = new byte [(int)myFile.length()]; //byte array

        fileInputStream = new FileInputStream(myFile);
        bufferedInputStream = new BufferedInputStream(fileInputStream);
        bufferedInputStream.read(mybytearray,0,mybytearray.length);
        outputstream = svrSocket.getOutputStream();
        System.out.println("Sending " + FILE_TO_SEND + "(" + mybytearray.length + " bytes)");
        outputstream.write(mybytearray,0,mybytearray.length); //send with outputstream
        outputstream.flush();
        System.out.println("File sent.");

        //close streams
        if (bufferedInputStream != null) bufferedInputStream.close();
        if (outputstream != null) outputstream.close();
        if (svrSocket!=null) svrSocket.close();
    }
}
