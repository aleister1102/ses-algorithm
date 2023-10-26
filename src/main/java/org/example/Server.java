package org.example;

import org.example.utils.Logger;
import org.example.utils.SocketUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class Server {
  private final ServerSocket serverSocket;

  public Server(ServerSocket serverSocket) {
    this.serverSocket = serverSocket;
  }

  public void open() {
    try {
      while (isOpened()) {
        Socket clientSocket = serverSocket.accept();
        ClientHandler clientHandler = new ClientHandler(clientSocket);

        Logger.log("A client is connected to the server! Client port: %s", clientSocket.getPort());

        // Create a new thread for handling the client
        new Thread(clientHandler).start();
      }
    } catch (IOException exception) {
      close(serverSocket);
    }
  }

  private boolean isOpened() {
    return !serverSocket.isClosed();
  }

  private class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private static final List<ClientHandler> clientHandlers = new LinkedList<>();

    public ClientHandler(Socket clientSocket) {
      try {
        this.clientSocket = clientSocket;
        this.bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

        clientHandlers.add(this);
      } catch (IOException e) {
        clientHandlers.remove(this);
        Logger.log("An error occurred while creating a client handler: %s", e.getMessage());
      }
    }

    @Override
    public void run() {
      String messageFromClient;

      while (clientSocket.isConnected()) {
        try {
          messageFromClient = bufferedReader.readLine();
          Logger.log("Message received from client with port %s: %s",
              clientSocket.getPort(),
              messageFromClient);
        } catch (IOException e) {
          clientHandlers.remove(this);
          SocketUtil.closeEverything(clientSocket, bufferedReader, bufferedWriter);
        }
      }
    }
  }

  private void close(Closeable socket) {
    try {
      if (socket != null)
        socket.close();
    } catch (IOException e) {
      Logger.log("Failed to close server socket!");
      e.printStackTrace();
    }
  }
}
