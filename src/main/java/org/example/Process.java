package org.example;

import lombok.Data;
import org.example.constants.Configuration;
import org.example.models.Message;
import org.example.models.VectorClock;
import org.example.utils.FileUtil;
import org.example.utils.LogUtil;
import org.example.utils.SocketUtil;
import org.example.utils.ThreadUtil;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

@Data
public class Process {
  private int port;
  private ServerSocket serverSocket;
  private Map<Integer, Client> clients;

  // Shared between threads
  public static ArrayList<Integer> timestampVector;
  public static ArrayList<VectorClock> vectorClocks;
  public static ConcurrentLinkedQueue<Message> buffer;
  private static List<Thread> threads = new LinkedList<>();

  // Lock for shared variables
  public static final Object lock = new Object();

  public Process(int port) {
    this.port = port;
    Optional.ofNullable(SocketUtil.createServerSocket(port)).ifPresent(createdServerSocket -> this.serverSocket = createdServerSocket);
    this.clients = new HashMap<>();

    timestampVector = new ArrayList<>(Collections.nCopies(Configuration.NUMBER_OF_PROCESSES, 0));
    vectorClocks = new ArrayList<>();
    buffer = new ConcurrentLinkedQueue<>();
    FileUtil.clearFile(FileUtil.setupLogFile(port));
  }

  private void addClient(int port, Client client) {
    clients.putIfAbsent(port, client);
  }

  public static String convertBufferToString() {
    return buffer.isEmpty()
            ? "Empty"
            : Process.buffer.stream()
            .map(message -> String.format("[P%s -> P%s]: %s", message.getSenderPort(), message.getReceiverPort(), message.getContent()))
            .reduce("\n", (acc, cur) -> acc + cur + ", ");
  }

  public static void main(String[] args) throws InterruptedException {
    Configuration.loadConfigsFromYaml();

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
      Semaphore semaphore = new Semaphore(0);
      createClients(process, semaphore);

      // Run demo or example
      if (Configuration.RUNNING_MODE.equals(Configuration.DEMO_MODE))
        runDemo(process);
      else if (Configuration.RUNNING_MODE.equals(Configuration.EXAMPLE_MODE))
        runScenarioOne(process);
    }
  }

  private static void createClients(Process process, Semaphore semaphore) {
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

  private static void runDemo(Process process) throws InterruptedException {
    for (var clientEntry : process.getClients().entrySet()) {
      Client client = clientEntry.getValue();

      int numberOfMessagesPerMinute = Configuration.randomNumberOfMessagesPerMinute();
      int sleepTime = Configuration.calculateSleepTime(numberOfMessagesPerMinute);
      LogUtil.log("Prepare to send message to port %s", client.getReceiverPort());
      LogUtil.log("Number of messages per minute: %s", numberOfMessagesPerMinute);
      LogUtil.log("Sleep time between messages: %s ms", sleepTime);

      int[] sleepTimes = new int[Configuration.NUMBER_OF_MESSAGES];
      Arrays.fill(sleepTimes, sleepTime);
      Thread sendingThread = ThreadUtil.start(() -> client.send(Configuration.NUMBER_OF_MESSAGES, sleepTimes));
      threads.add(sendingThread);
    }
  }

  private static void runScenarioOne(Process process) {
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
