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
            // If receive a notify message, set canSendMessages to true
            if (Message.isAllowConnecting(message)) {
              LogUtil.logWithSystemTimestamp(message.toLog());
              givePermissionToConnect();
            } else if (Message.isAllowSendingMessage(message)) {
              LogUtil.logWithSystemTimestamp(message.toLog());
              givePermissonToSendMessages();
            } else if (check(message, false))
              deliver(message, false);
          });
        }
      } catch (IOException e) {
        SocketUtil.closeEverything(clientSocket, bufferedReader, bufferedWriter);
      }
    }
  }

  private synchronized void givePermissionToConnect() {
    Process.canConnect = true;
    LogUtil.logWithSystemTimestamp("Port %s can connect to other processes now", port);
  }

  private synchronized void givePermissonToSendMessages() {
    Process.canSendMessages = true;
    LogUtil.logWithSystemTimestamp("Port %s can send messsages now", port);
  }

  private boolean check(Message message, boolean isInBuffer) {
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
        if (!isInBuffer) // only buffer the message if it is not in the buffer
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
    LogUtil.logAndWriteByPort(port, "Message %s is buffered", message.toLog());
    // LogUtil.logWithSystemTimestamp("Current buffer of port %s: %s\n", port,
    // Process.convertBufferToString());
  }

  private synchronized void deliver(Message message, boolean isFromBuffer) {
    message.setStatus(Message.DELIVERY);
    List<Integer> timestampVector = message.getTimestampVector();
    List<VectorClock> vectorClocks = message.getVectorClocks();

    // Increment the timestamp vector
    VectorClock.incrementByPort(port);

    // Merge the timestamp vector and the vector clocks
    mergeTimestampVectorAndVectorClocks(timestampVector, vectorClocks, isFromBuffer);

    // Log and write message
    LogUtil.logAndWriteWithTimestampVectorAndSystemTimestamp(
        message,
        Process.timestampVector,
        logFile,
        isFromBuffer
            ? "is delivered from buffer"
            : String.format("is delivered from port %s", message.getSenderPort()));

    // Deliver message(s) in the buffer that can be delivered
    if (!Process.buffer.isEmpty())
      deliverMessageFromBuffer();
  }

  private synchronized void mergeTimestampVectorAndVectorClocks(List<Integer> otherTimestampVector,
      List<VectorClock> otherVectorClocks, boolean needLogging) {
    // Merge the timestamp vector
    List<Integer> timestampVectorCopy = new ArrayList<>(Process.timestampVector);
    List<Integer> mergedTimestampVector = VectorClock.mergeTimestampVector(otherTimestampVector,
        Process.timestampVector);
    Process.timestampVector.clear();
    Process.timestampVector.addAll(mergedTimestampVector);

    // Merge the vector clocks
    List<VectorClock> vectorClocksCopy = VectorClock.copyVectorClocksOfProcess();
    VectorClock.mergeVectorClocks(otherVectorClocks, Process.vectorClocks);

    if (needLogging) {
      LogUtil.logAndWriteByPort(port,
          "Merged timestamp vector %s and %s. Current timestamp vector of the process: %s",
          otherTimestampVector, timestampVectorCopy, mergedTimestampVector);
      LogUtil.logAndWriteByPort(port, "Merged vector clocks %s into vector clocks %s. Current vector clocks: %s",
          otherVectorClocks, vectorClocksCopy, Process.vectorClocks);
    }
  }

  private void deliverMessageFromBuffer() {
    Message messageToBeDelivered = findMessageInBufferToBeDelivered();

    if (messageToBeDelivered != null) {
      // Remove the message from the buffer
      boolean removed = Process.buffer.remove(messageToBeDelivered);
      if (removed) {
        LogUtil.logAndWriteByPort(port, "Message %s is removed from buffer", messageToBeDelivered.toLog());
        // LogUtil.logWithSystemTimestamp("Current buffer of port %s: %s", port,
        // Process.convertBufferToString());

        // Deliver the message
        messageToBeDelivered.setStatus(Message.DELIVERY);
        deliver(messageToBeDelivered, true);
      }
    }
  }

  private Message findMessageInBufferToBeDelivered() {
    for (Message message : Process.buffer) {
      if (check(message, true)) {
        return message;
      }
    }
    return null;
  }
}
