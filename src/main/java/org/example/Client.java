package org.example;

import org.example.constants.Configuration;
import org.example.models.Message;
import org.example.models.VectorClock;
import org.example.utils.LogUtil;
import org.example.utils.SocketUtil;

import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;

public class Client {
  private int senderPort;
  private int receiverPort;
  private Socket socket;
  private BufferedReader bufferedReader; // for reading message from server
  private BufferedWriter bufferedWriter; // for sending message to server

  private static final ArrayList<Integer> timestampVector = new ArrayList<>(Collections.nCopies(Configuration.NUMBER_OF_PROCESSES, 0));
  private int indexInTimestampVector;
  private static final ArrayList<VectorClock> vectorClocks = new ArrayList<>();


  public Client(int senderPort, int receiverPort, Socket socket) {
    try {
      this.senderPort = senderPort;
      this.receiverPort = receiverPort;
      this.socket = socket;
      this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
      this.indexInTimestampVector = getIndexInTimestampVector();
    } catch (IOException exception) {
      SocketUtil.closeEverything(socket, bufferedReader, bufferedWriter);
    }
  }

  private int getIndexInTimestampVector() {
    for (int i = 0; i < Configuration.NUMBER_OF_PROCESSES; i++) {
      if (this.senderPort == Configuration.PORTS[i])
        return i;
    }
    return -1;
  }

  public void send() {
    try {
      if (socket.isConnected()) {
        int numberOfMessagesPerMinute = randomNumberOfMessagesPerMinute();
        int sleepTime = calculateSleepTime(numberOfMessagesPerMinute);
        LogUtil.log("Sending %s messages to port %s", Configuration.NUMBER_OF_MESSAGES, receiverPort);
        LogUtil.log("Number of messages per minute: %s", numberOfMessagesPerMinute);
        LogUtil.log("Sleep time between messages: %s ms", sleepTime);

        for (int i = 1; i <= Configuration.NUMBER_OF_MESSAGES; i++) {
          // Increment the timestamp vector
          VectorClock.incrementAt(timestampVector, indexInTimestampVector);
          VectorClock.updateTimestampVectorInList(vectorClocks, receiverPort, timestampVector);

          // Build the message
          Message message = buildMessageByIndex(i);

          // Send the message
          bufferedWriter.write(message.toString());
          bufferedWriter.newLine();
          bufferedWriter.flush();

          Thread.sleep(sleepTime);
        }
      }
    } catch (IOException | InterruptedException exception) {
      SocketUtil.closeEverything(socket, bufferedReader, bufferedWriter);
    }
  }

  private int randomNumberOfMessagesPerMinute() {
    return (int) (Math.random() * (Configuration.NUMBER_OF_MESSAGES - 10 + 1) + 10); // from 10 to NUMBER_OF_MESSAGES
  }

  private int calculateSleepTime(int numberOfMessagesPerMinute) {
    return 60000 / numberOfMessagesPerMinute; // 60000 ms = 1 minute
  }


  private Message buildMessageByIndex(int messageIndex) {
    String content = String.format("[message %s]", messageIndex);
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    return Message.builder()
            .senderPort(senderPort)
            .receiverPort(receiverPort)
            .content(content)
            .timestamp(timestamp)
            .vectorClocks(vectorClocks)
            .build();
  }
}
