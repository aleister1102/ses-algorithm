package org.example;

import org.example.utils.SocketUtil;

import java.io.*;
import java.net.Socket;

public class Client {
  private Socket socket;
  private BufferedReader bufferedReader; // for reading message from server
  private BufferedWriter bufferedWriter; // for sending message to server

  private static int NUMBER_OF_MESSAGES = 10;

  public Client(Socket socket) {
    try {
      this.socket = socket;
      this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    } catch (IOException exception) {
      SocketUtil.closeEverything(socket, bufferedReader, bufferedWriter);
    }
  }

  public void send() {
    try {
      if (socket.isConnected()) {
        for (int i = 0; i < NUMBER_OF_MESSAGES; i++) {
          bufferedWriter.write(String.format("[message %s]", i));
          bufferedWriter.newLine();
          bufferedWriter.flush();
        }
      }
    } catch (IOException exception) {
      SocketUtil.closeEverything(socket, bufferedReader, bufferedWriter);
    }
  }
}
