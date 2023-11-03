package org.example;

import org.example.constants.Configuration;
import org.example.models.Message;
import org.example.models.VectorClock;
import org.example.utils.FileUtil;
import org.example.utils.LogUtil;
import org.example.utils.SocketUtil;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClientHandler implements Runnable {
  private int port;
  private Socket clientSocket;
  private BufferedReader bufferedReader;
  private BufferedWriter bufferedWriter;
  private File logFile;

  public ClientHandler(int port, Socket clientSocket) {
    try {
      this.port = port;
      this.clientSocket = clientSocket;
      this.bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
      this.logFile = FileUtil.setupLogFile(port);
    } catch (IOException e) {
      LogUtil.log("An error occurred while creating a client handler: %s", e.getMessage());
    }
  }

  @Override
  public void run() {
    String messageFromClient;
    while (clientSocket.isConnected()) {
      try {
        messageFromClient = bufferedReader.readLine();
        if (messageFromClient != null) {
          Optional.ofNullable(Message.parse(messageFromClient)).ifPresent((message) -> {
            if (check(message))
              deliver(message);
          });
        }
      } catch (IOException e) {
        SocketUtil.closeEverything(clientSocket, bufferedReader, bufferedWriter);
      }
    }
  }

  private boolean check(Message message) {
    ArrayList<VectorClock> vectorClocks = message.getVectorClocks();
    VectorClock vectorClock = VectorClock.findByReceiverPort(vectorClocks, this.port);

    if (vectorClock != null) {
      // Check whether the timestamp vector in the vector clock
      // is less than or equal the timestamp vector of the current process
      if (isVectorClockSatisfied(vectorClock)) {
        return true;
      } else {
        // Buffer the message if the timestamp vector in the vector clock
        // is greater than the timestamp vector of the current process
        bufferMessage(message);
        return false;
      }
    } else {
      // Deliver the message if the vector clock does not contain the timestamp vector
      return true;
    }
  }

  private boolean isVectorClockSatisfied(VectorClock vectorClock) {
    return VectorClock.isLessThanOrEqual(vectorClock.getTimestampVector(), Process.timestampVector);
  }

  private void bufferMessage(Message message) {
    message.setStatus(Message.BUFFERED);
    Process.buffer.add(message);
    LogUtil.logWithCurrentTimestamp("Message %s is buffered\n", message.toLog());
    LogUtil.logWithCurrentTimestamp("Current buffer:\n%s", Process.buffer.stream().map(Message::toLog).reduce("", (acc, cur) -> acc + cur + "\n"));
  }

  private void deliver(Message message) {
    message.setStatus(Message.DELIVERED);
    List<Integer> timestampVector = message.getTimestampVector();
    List<VectorClock> vectorClocks = message.getVectorClocks();

    // Increment and update the timestamp vector
    int indexInTimestampVector = Configuration.getIndexInTimestampVector(port);
    VectorClock.increment(indexInTimestampVector, Process.timestampVector);
    VectorClock.mergeTimestampVector(timestampVector, Process.timestampVector);
    VectorClock.mergeVectorClocks(vectorClocks, Process.vectorClocks);

    // Log and write message content
    String logMessage = LogUtil.toStringWithTimestampVector(message.toLog(), Process.timestampVector);
    LogUtil.log(logMessage);
    LogUtil.writeLogToFile(logMessage, logFile);
    LogUtil.writeLogToFile(logMessage, Process.centralLogFile);

    // Deliver message(s) in the buffer that can be delivered
    if (!Process.buffer.isEmpty())
      deliverMessageFromBuffer();
  }

  private void deliverMessageFromBuffer() {
    LogUtil.logWithCurrentTimestamp("Current buffer:\n%s", Process.buffer.stream().map(Message::toLog).reduce("", (acc, cur) -> acc + cur + "\n"));
    for (Message message : Process.buffer) {
      if (check(message)) {
        LogUtil.logWithCurrentTimestamp("Delivering message %s from buffer", message.toLog());
        Process.buffer.remove(message);
        deliver(message);
      }
    }
  }
}
