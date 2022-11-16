package application;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class gameServer {
    public static void main(String[] args){
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(8848);
            Socket wait_socket = null;
            System.out.println("Waiting player...");
            while (true){
                Socket come = serverSocket.accept();
                if (wait_socket == null){
                    wait_socket = come;
                    System.out.println("A player enter, wait to match another player...");
                }else {
                    System.out.println("Match successfully, game start");
                    new ticThread(wait_socket, come).start();
                }
            }
        } catch (IOException e) {
            System.out.println("Error!");
            throw new RuntimeException(e);
        }
    }
}
