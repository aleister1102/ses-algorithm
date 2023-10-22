package org.example;

import org.example.utils.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
  private final ServerSocket serverSocket;

  public Server(ServerSocket serverSocket) {
    this.serverSocket = serverSocket;
  }

  public void start() {
    try {
      while (!serverSocket.isClosed()) {
        Socket socket = serverSocket.accept();
        Logger.logWithCurrentTimeStamp("A new client has connected!");
        ClientHandler clientHandler = new ClientHandler(socket);

        Thread thread = new Thread(clientHandler);
        thread.start();
      }
    } catch (IOException exception) {
      close();
    }
  }

  public void close() {
    try {
      if (serverSocket != null)
        serverSocket.close();
    } catch (IOException exception) {
      exception.printStackTrace();
    }
  }

  public static void main(String[] args) throws IOException {
    int PORT = 1234;
    ServerSocket socket = new ServerSocket(PORT);
    Server server = new Server(socket);
    Logger.logWithCurrentTimeStamp("Server is running at localhost:" + PORT);
    server.start();
  }
}
