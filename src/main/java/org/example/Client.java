package org.example;

import lombok.Data;
import org.example.constants.Configuration;
import org.example.models.Message;
import org.example.models.VectorClock;
import org.example.utils.FileUtil;
import org.example.utils.LogUtil;
import org.example.utils.SocketUtil;

import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;

@Data
public class Client {
  private int senderPort;
  private int receiverPort;
  private Socket socket;
  private BufferedReader bufferedReader; // for reading message from server
  private BufferedWriter bufferedWriter; // for sending message to server

  public Client(int senderPort, int receiverPort, Socket socket) {
    try {
      this.senderPort = senderPort;
      this.receiverPort = receiverPort;
      this.socket = socket;
      this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    } catch (IOException exception) {
      SocketUtil.closeEverything(socket, bufferedReader, bufferedWriter);
    }
  }

  public void send(int numberOfMessages, int... delays) {
    File logFile = FileUtil.setupLogFile(senderPort);

    try {
      if (socket.isConnected()) {
        LogUtil.log("Sending %s message(s) to port %s", numberOfMessages, receiverPort);

        Message message;
        for (int messageIndex = 1; messageIndex <= numberOfMessages; messageIndex++) {
          synchronized (Process.timestampVector) {
            // Increment and update the timestamp vector
            int indexInTimestampVector = Configuration.getIndexInTimestampVector(senderPort);
            VectorClock.incrementAt(Process.timestampVector, indexInTimestampVector);

            // Build the message
            message = buildMessageByIndex(messageIndex);

            // Log and write the sending message
            LogUtil.logAndWriteToFileWithTimestampVector(message, Process.timestampVector, logFile);

            // Save the vector clock of the previous message
            VectorClock.updateTimestampVectorInList(Process.vectorClocks, receiverPort, Process.timestampVector);
          }

          Thread.sleep(delays[messageIndex - 1]);

          // Send the message
          bufferedWriter.write(message.toString());
          bufferedWriter.newLine();
          bufferedWriter.flush();
        }
      }
    } catch (IOException | InterruptedException exception) {
      SocketUtil.closeEverything(socket, bufferedReader, bufferedWriter);
    }
  }

  private Message buildMessageByIndex(int messageIndex) {
    String content = String.format("[message %s]", messageIndex);
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    return Message.builder()
            .senderPort(senderPort)
            .receiverPort(receiverPort)
            .content(content)
            .timestamp(timestamp)
            .vectorClocks(Process.vectorClocks)
            .build();
  }
}
