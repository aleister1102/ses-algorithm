package org.example;

import java.net.*;
import java.util.*;

import org.example.constants.Configuration;
import org.example.models.VectorClock;
import org.example.utils.LogUtil;
import org.example.utils.SocketUtil;


import lombok.Data;

@Data
public class Process {
  private int port;
  private ServerSocket serverSocket;
  private List<Client> clients;

  public static final ArrayList<Integer> timestampVector = new ArrayList<>(Collections.nCopies(Configuration.NUMBER_OF_PROCESSES, 0));
  public static final ArrayList<VectorClock> vectorClocks = new ArrayList<>();

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

    // Create a server if the server socket is created successfully
    if (process.getServerSocket() != null) {
      Server server = new Server(process.getServerSocket());

      // Open for connections
      new Thread(server::open).start();

      // Wait for all processes to be connected
      LogUtil.log("Press any key to send messages");
      scanner.nextLine();

      // Create a client for each existing port (except the current port)
      for (int existingPort : Configuration.PORTS) {
        if (existingPort != port) {
          LogUtil.log("Creating a client for port %s", existingPort);
          Socket socket = SocketUtil.createClientSocket(existingPort);
          if (socket != null) {
            Client client = new Client(port, existingPort, socket);
            process.addClient(client);
          }
        }
      }

      // All processes are connected, send messages
      for (Client client : process.getClients()) {
        new Thread(client::send).start();
      }
      LogUtil.logWithCurrentTimeStamp("Finished sending messages");
    }
  }
}
