package org.example;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Process {
  private int port;
  private ServerSocket serverSocket;
  private ArrayList<Socket> connections;
  private static ArrayList<Process> processes = new ArrayList<Process>();

  public Process(int port) {
    this.port = port;
    connections = new ArrayList<>();
  }

  public void startListening() {
    try {
      serverSocket = new ServerSocket(port);
      System.out.println("Process listening on port " + port);

      // Accept incoming connections in a separate thread
      Thread listenerThread = new Thread(() -> {
        while (!serverSocket.isClosed()) {
          try {
            Socket clientSocket = serverSocket.accept();
            connections.add(clientSocket);
            System.out.println("Process " + this.port + ": connected to a new process at " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      });

      listenerThread.start();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void connectToOtherProcesses() {
    for (Process targetProcess : processes) {
      if (targetProcess.getPort() != this.port) {
        try {
          Socket socket = new Socket("localhost", targetProcess.getPort());
          connections.add(socket);
          System.out.println("Process " + this.port + ": connected to process at port " + targetProcess.getPort());
        } catch (IOException e) {
          System.out.println("Process " + this.port + ": an error has occurred while connecting to process at port " + targetProcess.getPort() + ". Retrying...");
        }
      }
    }
  }

  public int getPort() {
    return port;
  }

  public static void main(String[] args) {
    int n = 3;
    int[] ports = {1234, 1235, 1236, 1237, 1238};
    for (int i = 0; i < n; i++) {
      processes.add(new Process(ports[i]));
    }

    for (Process process : processes) {
      process.startListening();
    }

    for (Process process : processes) {
      process.connectToOtherProcesses();
    }
  }
}
