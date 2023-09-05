import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class SocketClient implements Runnable {
    int port;
    String filename;
    private Socket socket = null;
    private ServerSocket server = null;
    private DataOutputStream outputStream = null;
    private Scanner sc;

    Map<String, Socket> clients;
    public SocketClient(int portNum, String fileNameWithPath) {
        this.port = portNum;
        this.filename = fileNameWithPath;
        try {
            server = new ServerSocket(port);
            sc = new Scanner(new File(filename));
            clients = new ConcurrentHashMap<>();
            System.out.println("Server started");
            System.out.println("Waiting for a client ...");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendData() {
        try {
            while(true) {
                try {
                    if(sc.hasNextLine() && !clients.isEmpty()) {
                        String data = sc.nextLine();
                        System.out.println(data);
                        for(String client : clients.keySet()) {
                            Socket socket = clients.get(client);
                            if(!socket.isClosed()) {
//                                System.out.println("Sending data to client : " + socket.getInetAddress().getHostAddress());
                                try {
                                    DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                                    outputStream.writeUTF(data + "\n");
                                    outputStream.flush();
                                    Thread.sleep(10);
                                } catch (Exception e) {
                                    try {
                                        System.out.println("Connection terminated. Removing client from map : " + client);
                                        clients.remove(client);
                                        socket.close();
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            } else {
                                System.out.println("Connection terminated. Removing client from map : " + client);
                                clients.remove(client);
                                socket.close();
                            }
                        }
                    } else {
                        if(clients.isEmpty()) {
                            System.out.println("No client connected to server.");
                        } else {
                            System.out.println("No Data to broadcast.");
                            System.out.println("Total connected clients : " + clients.size());
                        }
                        Thread.sleep(5000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        Socket socket = null;
        while(true) {
            try {
                socket = server.accept();
                System.out.println("Request accepted for client " + socket.getInetAddress().getHostAddress());
                clients.put(socket.getInetAddress().getHostAddress(), socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}