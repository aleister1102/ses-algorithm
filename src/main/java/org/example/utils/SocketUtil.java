package org.example.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;

public class SocketUtil {
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
