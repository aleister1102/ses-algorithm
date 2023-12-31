package org.example;

import lombok.Data;

import org.example.models.Message;
import org.example.models.VectorClock;
import org.example.utils.FileUtil;
import org.example.utils.LogUtil;
import org.example.utils.SocketUtil;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

@Data
public class Client {
  private int senderPort;
  private int receiverPort;
  private Socket socket;
  private BufferedReader bufferedReader; // for reading message from server
  private BufferedWriter bufferedWriter; // for sending message to server
  private File logFile;

  public Client(int senderPort, int receiverPort, Socket socket) {
    try {
      this.senderPort = senderPort;
      this.receiverPort = receiverPort;
      this.socket = socket;
      this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
      this.logFile = FileUtil.setupLogFile(senderPort);
    } catch (IOException exception) {
      SocketUtil.closeEverything(socket, bufferedReader, bufferedWriter);
    }
  }

  public static synchronized void waitToConnect() {
    while (!Process.canConnect) {
      try {
        Thread.sleep(5000);
        LogUtil.log("Waiting until have enought permission to connect to other processes");
      } catch (InterruptedException exception) {
        LogUtil.log("Error(s) occurred while waiting to connect to other processes: %s", exception.getMessage());
      }
    }
  }

  public static synchronized void waitToSend() {
    while (!Process.canSendMessages) {
      try {
        Thread.sleep(5000);
        LogUtil.log("Waiting until have enought permission to send messages");
      } catch (InterruptedException exception) {
        LogUtil.log("Error(s) occurred while waiting to send messages: %s", exception.getMessage());
      }
    }
  }

  public void send(int numberOfMessages, int... delays) {
    String currentThreadName = Thread.currentThread().getName();
    LogUtil.logWithSystemTimestamp("%s of port %s is sending %s message(s) to port %s",
        currentThreadName, senderPort, numberOfMessages, receiverPort);

    try {
      if (socket.isConnected()) {
        for (int messageIndex = 1; messageIndex <= numberOfMessages; messageIndex++) {
          synchronized (Process.lock) {
            // Increment and update the timestamp vector
            VectorClock.incrementByPort(senderPort);

            // Get the current timestamp vector and the current vector clocks - why need
            // this?
            ArrayList<Integer> currentTimestampVector = new ArrayList<>(Process.timestampVector);
            ArrayList<VectorClock> currentVectorClocks = new ArrayList<>(Process.vectorClocks);

            // Build the message
            Message message = buildMessage(messageIndex, currentTimestampVector, currentVectorClocks);

            // Log and write the message
            LogUtil.logAndWriteWithTimestampVectorAndSystemTimestamp(
                message,
                currentTimestampVector,
                logFile, String.format("is sending to port %s", receiverPort));

            // Write the message to the buffer
            bufferedWriter.write(message.toString());
            bufferedWriter.newLine();

            // Save the timestamp vector of the previous message to vector clocks
            VectorClock.updateTimestampVectorInList(currentTimestampVector, Process.vectorClocks, senderPort,
                receiverPort);
          }

          Thread.sleep(delays[messageIndex - 1]);
          bufferedWriter.flush();
        }
      }
    } catch (IOException | InterruptedException exception) {
      SocketUtil.closeEverything(socket, bufferedReader, bufferedWriter);
    }
  }

  public void sendNotifyMessage(String content) {
    String currentThreadName = Thread.currentThread().getName();
    LogUtil.logWithSystemTimestamp("%s of port %s is sending notify message to port %s",
        currentThreadName, senderPort, receiverPort);

    try {
      if (socket.isConnected()) {
        Message notifyMessage = buildNotifyMessage(content);
        bufferedWriter.write(notifyMessage.toString());
        bufferedWriter.newLine();
        bufferedWriter.flush();
      }
    } catch (Exception e) {
      SocketUtil.closeEverything(socket, bufferedReader, bufferedWriter);
    }
  }

  private Message buildMessage(int messageIndex, ArrayList<Integer> timestampVector,
      ArrayList<VectorClock> vectorClocks) {
    String content = String.format("[message %s]", messageIndex);
    return Message.builder()
        .senderPort(senderPort)
        .receiverPort(receiverPort)
        .content(content)
        .timestampVector(timestampVector)
        .vectorClocks(vectorClocks)
        .build();
  }

  private Message buildNotifyMessage(String content) {
    return Message.builder()
        .senderPort(senderPort)
        .receiverPort(receiverPort)
        .content(content)
        .build();
  }
}
