package org.example;

import java.net.*;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Scanner;

import org.example.utils.Logger;
import org.example.utils.SocketUtil;

import lombok.Data;

@Data
public class Process {
  private int port;
  private ServerSocket serverSocket;
  private final LinkedList<Socket> clients;

  private static final int[] ports = { 1, 2, 3 };

  public Process(int port) {
    this.port = port;

    Optional.ofNullable(SocketUtil.createServerSocket(port)).ifPresent(createdServerSocket -> {
      this.serverSocket = createdServerSocket;
    });

    clients = new LinkedList<>();
  }

  public static void main(String[] args) {
    int port = Integer.parseInt(args[0]);
    Process process = new Process(port);

    // Create a server if server socket is created successfully
    if (process.getServerSocket() != null) {
      Server server = new Server(process.getServerSocket());

      // Open for connections
      new Thread(() -> {
        server.open();
      }).start();

      Logger.log("Press any key to send a message to other processes");
      Scanner scanner = new Scanner(System.in);
      scanner.nextLine();
      scanner.close();

      for (int existingPort : ports) {
        if (existingPort != port) {
          // Create a client for each existing port (except the current port)
          Logger.log("Creating a client for port %s", existingPort);
          Socket socket = SocketUtil.createClientSocket(existingPort);
          Client client = new Client(socket);

          // Send a message to each client
          Optional.ofNullable(client).ifPresent(createdClient -> {
            Logger.log("Sending a message to port %s", existingPort);
            createdClient.send();
          });
        }
      }
    }
  }
}
