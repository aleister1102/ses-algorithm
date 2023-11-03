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

    public ClientHandler(int port, Socket clientSocket) {
      try {
        this.port = port;
        this.clientSocket = clientSocket;
        this.bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
      } catch (IOException e) {
        LogUtil.log("An error occurred while creating a client handler: %s", e.getMessage());
      }
    }

    @Override
    public void run() {
      String messageFromClient;
      File logFile = FileUtil.setupLogFile(this.port);
      File centralLogFile = FileUtil.setupCentralLogFile();

      while (clientSocket.isConnected()) {
        try {
          messageFromClient = bufferedReader.readLine();
          if (messageFromClient != null) {
            try {
              // Parse json string to message object
              ObjectMapper objectMapper = new ObjectMapper();
              Message message = objectMapper.readValue(messageFromClient, Message.class);

              boolean canDeliver = canDeliver(message);
              if (canDeliver)
                deliver(message, logFile, centralLogFile);

            } catch (JsonProcessingException e) {
              LogUtil.log("An error occurred while parsing message from client.\nOriginal message: %s.\nError message: %s", messageFromClient, e.getMessage());
            }
          } else
            break;
        } catch (IOException e) {
          SocketUtil.closeEverything(clientSocket, bufferedReader, bufferedWriter);
        }
      }
    }

    private boolean canDeliver(Message message) {
      ArrayList<VectorClock> vectorClocks = message.getVectorClocks();
      List<Integer> timestampVectorInVectorClocks = vectorClocks.stream()
              .filter(clock -> clock.getPort() == port)
              .map(VectorClock::getTimestampVector)
              .findAny()
              .orElse(null);

      if (timestampVectorInVectorClocks != null) {
        LogUtil.log("Comparing timestamp vector in vector clocks: %s with timestamp vector of current process: %s",
                timestampVectorInVectorClocks,
                Process.timestampVector);
        // Check whether the timestamp vector in the vector clock
        // is less than or equal the timestamp vector of the current process
        if (VectorClock.isLessThanOrEqual(timestampVectorInVectorClocks, Process.timestampVector)) {
          return true; // then deliver it
        } else {
          Process.buffer.add(message); // else buffer it
          LogUtil.logWithCurrentTimestamp("Message %s is buffered\n", message.toLog());
          LogUtil.logWithCurrentTimestamp("Current buffer:\n%s", Process.buffer.stream().map(Message::toLog).reduce("", (acc, cur) -> acc + cur + "\n"));
          return false;
        }
      } else {
        return true;
      }
    }

    private void deliver(Message message, File logFile, File centralLogFile) {
      int receiverPort = message.getReceiverPort();
      List<Integer> timestampVector = message.getTimestampVector();
      List<VectorClock> vectorClocks = message.getVectorClocks();

      // Increment and update the timestamp vector
      int indexInTimestampVector = Configuration.getIndexInTimestampVector(receiverPort);
      VectorClock.incrementAt(Process.timestampVector, indexInTimestampVector);
      VectorClock.mergeTimestampVector(timestampVector, Process.timestampVector);
      VectorClock.mergeVectorClocks(vectorClocks, Process.vectorClocks);

      // Log and write message content
      String logMessage = LogUtil.toStringWithTimestampVector(message.toLog(), Process.timestampVector);
      LogUtil.log(logMessage);
      LogUtil.writeLogToFile(logMessage, logFile);
      LogUtil.writeLogToFile(logMessage, centralLogFile);

      // Deliver message(s) in the buffer that can be delivered
      if (!Process.buffer.isEmpty())
        deliverMessageFromBuffer(logFile, centralLogFile);
    }

    private void deliverMessageFromBuffer(File logFile, File centralLogFile) {
      LogUtil.logWithCurrentTimestamp("Current buffer:\n%s", Process.buffer.stream().map(Message::toLog).reduce("", (acc, cur) -> acc + cur + "\n"));
      for (Message message : Process.buffer) {
        if (canDeliver(message)) {
          LogUtil.logWithCurrentTimestamp("Delivering message %s from buffer", message.toLog());
          Process.buffer.remove(message);
          deliver(message, logFile, centralLogFile);
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
