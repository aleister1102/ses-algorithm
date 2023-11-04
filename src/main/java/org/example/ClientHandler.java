package org.example;

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
      LogUtil.log("Error(s) occurred while creating a client handler: %s", e.getMessage());
    }
  }

  @Override
  public void run() {
    while (clientSocket.isConnected()) {
      try {
        String messageFromClient = bufferedReader.readLine();

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
    VectorClock vectorClock = VectorClock.findByPort(vectorClocks, port);

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
    return vectorClock.isTimestampVectorLessThanOrEqual(Process.timestampVector);
  }

  private void bufferMessage(Message message) {
    message.setStatus(Message.BUFFER);
    Process.buffer.add(message);
    LogUtil.logAndWriteWithTimestampVectorAndSystemTimestamp(message, Process.timestampVector, logFile, " is buffered");
    LogUtil.logWithSystemTimestamp("Current buffer: %s\n", Process.convertBufferToString());
  }

  private void deliver(Message message) {
    message.setStatus(Message.DELIVERY);
    List<Integer> timestampVector = message.getTimestampVector();
    List<VectorClock> vectorClocks = message.getVectorClocks();

    // Increment the timestamp vector
    VectorClock.incrementByPort(port);

    // Merge the timestamp vector and the vector clocks
    mergeTimestampVectorAndVectorClocks(timestampVector, vectorClocks);

    // Log and write message
    LogUtil.logAndWriteWithTimestampVectorAndSystemTimestamp(
            message,
            Process.timestampVector,
            logFile,
            String.format("is delivered from port %s", message.getSenderPort()));

    // Deliver message(s) in the buffer that can be delivered
    if (!Process.buffer.isEmpty())
      deliverMessageFromBuffer();
  }

  private void mergeTimestampVectorAndVectorClocks(List<Integer> timestampVector, List<VectorClock> vectorClocks) {
    VectorClock.mergeTimestampVector(timestampVector, Process.timestampVector, port);
    VectorClock.mergeVectorClocks(vectorClocks, Process.vectorClocks, port);
  }

  private void deliverMessageFromBuffer() {
    for (Message message : Process.buffer) {
      if (check(message)) {
        LogUtil.logAndWriteWithTimestampVectorAndSystemTimestamp(message, Process.timestampVector, logFile, "is delivered from buffer");
        Process.buffer.remove(message);
        deliver(message);
      }
    }
    LogUtil.logWithSystemTimestamp("Current buffer: %s", Process.convertBufferToString());
  }
}
