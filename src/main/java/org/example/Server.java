package org.example;

import org.example.utils.LogUtil;
import org.example.utils.ThreadUtil;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
  private final ServerSocket serverSocket;

  public Server(ServerSocket serverSocket) {
    this.serverSocket = serverSocket;
  }

  public void open() {
    try {
      while (isOpened()) {
        Socket clientSocket = serverSocket.accept();
        ClientHandler clientHandler = new ClientHandler(serverSocket.getLocalPort(), clientSocket);

        LogUtil.logWithCurrentTimestamp("A client is connected to the server! Client port: %s", clientSocket.getPort());

        // Create a new thread for handling the client
        ThreadUtil.start(clientHandler);
      }
    } catch (IOException exception) {
      close(serverSocket);
    }
  }

  private boolean isOpened() {
    return !serverSocket.isClosed();
  }

  private void close(Closeable socket) {
    try {
      if (socket != null)
        socket.close();
    } catch (IOException e) {
      LogUtil.log("Failed to close server socket: ", e.getMessage());
    }
  }
}
