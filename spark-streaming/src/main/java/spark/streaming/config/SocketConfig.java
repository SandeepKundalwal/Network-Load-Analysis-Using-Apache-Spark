package spark.streaming.config;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SocketConfig implements Runnable {
    Map<String, Socket> clients;
    private ServerSocket server = null;

    PrintWriter out = null;


    public SocketConfig(int port) {
        clients = new ConcurrentHashMap<>();
        try {
            server = new ServerSocket(port);
            System.out.println("Server started on host : [" + server.getInetAddress().getHostAddress() + "], port : " + port);
            System.out.println("Waiting for connections...");
        } catch(IOException i) {
            i.printStackTrace();
        }
    }

    public synchronized void sendData(String data) {
        PrintWriter out = null;
        System.out.println("Current Thread : " + Thread.currentThread().getName());
//        DataOutputStream outputStream = null;
        for(String client : clients.keySet()) {
            Socket socket = clients.get(client);
            System.out.println("Sending data to client : " + socket.getInetAddress().getHostAddress());
            try {
                out = new PrintWriter(socket.getOutputStream());
                out.println(data.replaceAll("\n", "").replaceAll(" ", "") + "|");
                out.flush();
            } catch (Exception e) {
                try {
                    System.out.println("Removing client : " + client);
                    clients.remove(client);
                    socket.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    public void closeConnection() {
        try {
            for(String client : clients.keySet()) {
                Socket socket = clients.get(client);
                System.out.println("Closing connection with client : " + socket.getInetAddress().getHostAddress());
                socket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
