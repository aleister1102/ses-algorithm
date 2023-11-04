package org.example;

import java.io.File;
import java.net.*;
import java.util.*;

import org.example.constants.Configuration;
import org.example.models.Message;
import org.example.models.VectorClock;
import org.example.utils.FileUtil;
import org.example.utils.LogUtil;
import org.example.utils.SocketUtil;

import lombok.Data;
import org.example.utils.ThreadUtil;

@Data
public class Process {
  private int port;
  private ServerSocket serverSocket;
  private Map<Integer, Client> clients;

  // Shared between threads
  public static final ArrayList<Integer> timestampVector = new ArrayList<>(Collections.nCopies(Configuration.NUMBER_OF_PROCESSES, 0));
  public static final ArrayList<VectorClock> vectorClocks = new ArrayList<>();
  public static final List<Message> buffer = new LinkedList<>();
  public static final File centralLogFile = FileUtil.setupCentralLogFile();

  // Lock for shared variables
  public static final Object lock = new Object();

  public Process(int port) {
    this.port = port;

    Optional.ofNullable(SocketUtil.createServerSocket(port)).ifPresent(createdServerSocket -> {
      this.serverSocket = createdServerSocket;
    });

    clients = new HashMap<>();
    FileUtil.clearFile(FileUtil.setupLogFile(port));
  }

  public void addClient(int port, Client client) {
    clients.putIfAbsent(port, client);
  }

  public static String convertBufferToString() {
    return buffer.isEmpty() ? "Empty" : Process.buffer.stream().map(Message::toLog).reduce("\n", (acc, cur) -> acc + cur + "\n");
  }

  public static void main(String[] args) {
    int port = Integer.parseInt(args[0]);
    Process process = new Process(port);
    Scanner scanner = new Scanner(System.in);

    // Create a server if the server socket is created successfully
    if (process.getServerSocket() != null) {
      Server server = new Server(process.getServerSocket());

      // Open for connections
      ThreadUtil.start(server::open);

      // Wait for all processes to be connected
      LogUtil.log("Press any key to send messages");
      scanner.nextLine();
      scanner.close();

      // Create a client for each existing port (except the current port)
      createClients(process);

      // All processes are connected, send messages
      // runDemo(process);

      // Simulate the example
      runScenarioOne(process);
    }
  }

  public static void createClients(Process process) {
    for (int existingPort : Configuration.PORTS) {
      if (existingPort != process.port) {
        LogUtil.log("Creating a client for port %s", existingPort);
        Socket socket = SocketUtil.createClientSocket(existingPort);
        if (socket != null) {
          Client client = new Client(process.port, existingPort, socket);
          process.addClient(existingPort, client);
        }
      }
    }
  }

  public static void runDemo(Process process) {
    for (var clientEntry : process.getClients().entrySet()) {
      Client client = clientEntry.getValue();

      int numberOfMessagesPerMinute = Configuration.randomNumberOfMessagesPerMinute();
      int sleepTime = Configuration.calculateSleepTime(numberOfMessagesPerMinute);
      LogUtil.log("Number of messages per minute: %s", numberOfMessagesPerMinute);
      LogUtil.log("Sleep time between messages: %s ms", sleepTime);

      int[] sleepTimes = new int[numberOfMessagesPerMinute];
      Arrays.fill(sleepTimes, sleepTime);
      ThreadUtil.start(() -> client.send(numberOfMessagesPerMinute, sleepTimes));
    }
  }

  public static void runScenarioOne(Process process) {
    Client clientWithPort1 = process.getClients().get(1);
    Client clientWithPort2 = process.getClients().get(2);
    Client clientWithPort3 = process.getClients().get(3);

    if (process.port == 2) {
      ThreadUtil.start(() -> clientWithPort1.send(2, 7000, 2000));
      ThreadUtil.sleep(1000);
      ThreadUtil.start(() -> clientWithPort3.send(1, 2000));
    } else if (process.port == 3) {
      ThreadUtil.sleep(4000);
      ThreadUtil.start(() -> clientWithPort1.send(1, 7000));
      ThreadUtil.sleep(1000);
      ThreadUtil.start(() -> clientWithPort2.send(1, 1000));
    }
  }
}
