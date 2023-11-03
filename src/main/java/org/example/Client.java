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

  public void send(int numberOfMessages, int... delays) {
    LogUtil.logWithCurrentTimestamp("%s of port %s is sending %s message(s) to port %s",
            Thread.currentThread().getName(),
            senderPort,
            numberOfMessages,
            receiverPort);

    try {
      if (socket.isConnected()) {
        for (int messageIndex = 1; messageIndex <= numberOfMessages; messageIndex++) {
          synchronized (Process.lock) {
            // Increment and update the timestamp vector
            int indexInTimestampVector = Configuration.getIndexInTimestampVector(senderPort);
            VectorClock.increment(indexInTimestampVector, Process.timestampVector);

            // Get the current timestamp vector and the current vector clocks - why need this?
            ArrayList<Integer> currentTimestampVector = new ArrayList<>(Process.timestampVector);
            ArrayList<VectorClock> currentVectorClocks = new ArrayList<>(Process.vectorClocks);

            // Build the message
            Message message = buildMessage(messageIndex, currentTimestampVector, currentVectorClocks);

            // Log and write the sending message
            String logMessage = LogUtil.toStringWithTimestampVector(message.toLog(), currentTimestampVector);
            LogUtil.log(logMessage);
            LogUtil.writeLogToFile(logMessage, logFile);
            LogUtil.writeLogToFile(logMessage, Process.centralLogFile);

            // Send the message to the buffer
            LogUtil.logWithCurrentTimestamp("Sending message %s to port %s", message.toLog(), receiverPort);
            bufferedWriter.write(message.toString());
            bufferedWriter.newLine();

            // Save the vector clock of the previous message
            VectorClock.updateTimestampVectorInList(currentTimestampVector, receiverPort, Process.vectorClocks);
          }

          Thread.sleep(delays[messageIndex - 1]);
          bufferedWriter.flush();
        }
      }
    } catch (IOException | InterruptedException exception) {
      SocketUtil.closeEverything(socket, bufferedReader, bufferedWriter);
    }
  }

  private Message buildMessage(int messageIndex, ArrayList<Integer> timestampVector, ArrayList<VectorClock> vectorClocks) {
    String content = String.format("[message %s]", messageIndex);
    return Message.builder()
            .senderPort(senderPort)
            .receiverPort(receiverPort)
            .content(content)
            .timestampVector(timestampVector)
            .vectorClocks(vectorClocks)
            .build();
  }
}
