package org.example;

import org.example.utils.Logger;
import org.example.utils.SocketUtil;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
  private Socket socket;
  private BufferedReader bufferedReader; // for reading message from server
  private BufferedWriter bufferedWriter; // for sending message to server
  private String username;

  public Client(Socket socket) {
    try {
      this.socket = socket;
      this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    } catch (IOException exception) {
      SocketUtil.closeEverything(socket, bufferedReader, bufferedWriter);
    }
  }

  private void join() {
    Scanner scanner = new Scanner(System.in);
    Logger.log("Enter your username for the group chat: ");
    username = scanner.nextLine();
    try {
      bufferedWriter.write(username);
      bufferedWriter.newLine();
      bufferedWriter.flush();
    } catch (IOException exception) {
      SocketUtil.closeEverything(socket, bufferedReader, bufferedWriter);
    }
  }

  private void sendMessage() {
    try {
      Scanner scanner = new Scanner(System.in);
      while (socket.isConnected()) {
        String messageToSend = scanner.nextLine();
        bufferedWriter.write(username + ": " + messageToSend);
        bufferedWriter.newLine();
        bufferedWriter.flush();
      }
    } catch (IOException exception) {
      SocketUtil.closeEverything(socket, bufferedReader, bufferedWriter);
    }
  }

  private void listenForMessage() {
    new Thread(() -> {
      String messageFromGroupChat;

      while (socket.isConnected()) {
        try {
          messageFromGroupChat = bufferedReader.readLine();
          Logger.logWithCurrentTimeStamp(messageFromGroupChat);
        } catch (IOException exception) {
          SocketUtil.closeEverything(socket, bufferedReader, bufferedWriter);
        }
      }
    }).start();
  }

  public static void main(String[] args) throws IOException {
    Socket socket = new Socket("localhost", 1234);
    Client client = new Client(socket);
    client.join();
    client.listenForMessage();
    client.sendMessage();
  }
}
