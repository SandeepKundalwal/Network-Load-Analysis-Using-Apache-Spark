public class Main {
    public static void main(String[] args) {
        System.out.println("\t\t\t\t\t\t\t\t\t==============================");
        System.out.println("\t\t\t\t\t\t\t\t\t||Starting broadcast service||");
        System.out.println("\t\t\t\t\t\t\t\t\t==============================");
        SocketClient socketClient = new SocketClient(Integer.parseInt(args[0]), args[1]);
        new Thread(socketClient).start();
        socketClient.sendData();
    }
}