package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.constants.Configuration;
import org.example.models.Message;
import org.example.models.VectorClock;
import org.example.utils.FileUtil;
import org.example.utils.LogUtil;
import org.example.utils.SocketUtil;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
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
        ClientHandler clientHandler = new ClientHandler(serverSocket.getLocalPort(), clientSocket);

        LogUtil.log("A client is connected to the server! Client port: %s", clientSocket.getPort());

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

  public static class ClientHandler implements Runnable {
    private int port;
    private Socket clientSocket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private final List<Message> messages = new LinkedList<>();
    private static final List<ClientHandler> clientHandlers = new LinkedList<>();

    public ClientHandler(int port, Socket clientSocket) {
      try {
        this.port = port;
        this.clientSocket = clientSocket;
        this.bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

        clientHandlers.add(this);
      } catch (IOException e) {
        LogUtil.log("An error occurred while creating a client handler: %s", e.getMessage());
        clientHandlers.remove(this);
      }
    }

    @Override
    public void run() {
      String messageFromClient;
      File logFile = FileUtil.setupLogFile(this.port);

      while (clientSocket.isConnected()) {
        try {
          messageFromClient = bufferedReader.readLine();
          if (messageFromClient != null) {
            try {
              // Parse json string to message object
              ObjectMapper objectMapper = new ObjectMapper();
              Message message = objectMapper.readValue(messageFromClient, Message.class);
              int receiverPort = message.getReceiverPort();
              ArrayList<Integer> timestampVector = message.getTimestampVector();

              synchronized (Process.timestampVector) {
                // Increment and update the timestamp vector
                int indexInTimestampVector = Configuration.getIndexInTimestampVector(receiverPort);
                VectorClock.incrementAt(Process.timestampVector, indexInTimestampVector);
                VectorClock.merge(timestampVector, Process.timestampVector);

                // Add message to list
                messages.add(message);

                // Log and write message content
                LogUtil.logAndWriteToFileWithTimestampVector(message, Process.timestampVector, logFile);
              }
            } catch (JsonProcessingException e) {
              LogUtil.log("An error occurred while parsing message from client.\nOriginal message: %s.\nError message: %s", messageFromClient, e.getMessage());
            }
          } else
            break;
        } catch (IOException e) {
          SocketUtil.closeEverything(clientSocket, bufferedReader, bufferedWriter);
          clientHandlers.remove(this);
        }
      }
    }
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
