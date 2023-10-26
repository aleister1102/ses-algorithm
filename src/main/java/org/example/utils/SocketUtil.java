package org.example.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketUtil {
  public static ServerSocket createServerSocket(int port) {
    try {
      ServerSocket serverSocket = new ServerSocket(port);
      Logger.log("Server socket is created on port %s", port);
      return serverSocket;
    } catch (IOException e) {
      Logger.log("Port %s is already in use", port);
      return null;
    }
  }

  public static Socket createClientSocket(int port) {
    try {
      Socket socket = new Socket("localhost", port);
      return socket;
    } catch (IOException e) {
      Logger.log("An error occured when connecting to port %s: %s", port, e.getMessage());
      return null;
    }
  }

  public static void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
    try {
      if (bufferedReader != null) {
        bufferedReader.close(); // also close the inside stream
      }
      if (bufferedWriter != null) {
        bufferedWriter.close();
      }
      if (socket != null) {
        socket.close(); // also close the input and output stream
      }
    } catch (IOException exception) {
      exception.printStackTrace();
    }
  }
}
