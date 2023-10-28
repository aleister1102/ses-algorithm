package org.example;

import java.net.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import org.example.utils.Logger;
import org.example.utils.SocketUtil;

import lombok.Data;

@Data
public class Process {
  private int port;
  private ServerSocket serverSocket;
  private List<Client> clients;

  private static final int[] ports = { 1, 2, 3 };

  public Process(int port) {
    this.port = port;

    Optional.ofNullable(SocketUtil.createServerSocket(port)).ifPresent(createdServerSocket -> {
      this.serverSocket = createdServerSocket;
    });

    clients = new LinkedList<>();
  }

  public void addClient(Client client) {
    clients.add(client);
  }

  public static void main(String[] args) {
    int port = Integer.parseInt(args[0]);
    Process process = new Process(port);
    Scanner scanner = new Scanner(System.in);

    // Create a server if server socket is created successfully
    if (process.getServerSocket() != null) {
      Server server = new Server(process.getServerSocket());

      // Open for connections
      new Thread(() -> {
        server.open();
      }).start();

      Logger.log("Press any key to connect to other processes");
      scanner.nextLine();

      // Create a client for each existing port (except the current port)
      for (int existingPort : ports) {
        if (existingPort != port) {
          Logger.log("Creating a client for port %s", existingPort);
          Socket socket = SocketUtil.createClientSocket(existingPort);
          Client client = new Client(socket);
          process.addClient(client);
        }
      }

      Logger.log("Sending messages to other processes in 5 seconds...");
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      // Loop over clients and send a message to each one
      for (Client client : process.getClients()) {
        client.send();
      }

    }
  }
}
