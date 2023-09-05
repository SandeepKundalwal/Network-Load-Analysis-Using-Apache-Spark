package spark.streaming.service;

import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.receiver.Receiver;
import org.spark_project.guava.io.Closeables;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.net.Socket;

public class LogReceiver extends Receiver<String> {

    private String host = null;
    private DataInputStream input = null;
    private int port = -1;

    public LogReceiver(String host, int port) {
        super(StorageLevel.MEMORY_AND_DISK_2());
        this.host = host;
        this.port = port;
    }

    @Override
    public void onStart() {
        // Start the thread that receives data over a connection
        new Thread(this::receive).start();
    }

    @Override
    public void onStop() {
        // There is nothing much to do as the thread calling receive()
        // is designed to stop by itself isStopped() returns false
    }

    private void receive() {
        try {
            Socket socket = null;
            BufferedReader reader = null;
            try {
                // connect to the server
                socket = new Socket(host, port);
                input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                // Until stopped or connection broken continue reading
                String userInput;
                while (!isStopped()) {
                    userInput = input.readUTF();
//                    System.out.println("Received data '" + userInput + "'");
                    store(userInput);
                }
            } finally {
                Closeables.close(reader, /* swallowIOException = */ true);
                Closeables.close(socket,  /* swallowIOException = */ true);
            }
            // Restart in an attempt to connect again when server is active again
            restart("Trying to connect again");
        } catch(Exception ce) {
            // restart if could not connect to server
            restart("Could not connect", ce);
        }
    }
}
