package org.example;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import org.example.constants.Configuration;
import org.example.models.Message;
import org.example.models.VectorClock;
import org.example.utils.FileUtil;
import org.example.utils.LogUtil;
import org.example.utils.SocketUtil;
import org.example.utils.ThreadUtil;

import lombok.Data;

@Data
public class Process {
  private int port;
  private ServerSocket serverSocket;
  private Map<Integer, Client> clients;

  // Shared between threads
  public static ArrayList<Integer> timestampVector;
  public static ArrayList<VectorClock> vectorClocks;
  public static ConcurrentLinkedQueue<Message> buffer;
  public static ConcurrentLinkedQueue<Thread> sendingThreads;
  public static boolean canSendMessages = false;

  // Lock for shared variables
  public static final Object lock = new Object();

  public Process(int port) {
    this.port = port;
    Optional.ofNullable(SocketUtil.createServerSocket(port))
        .ifPresent(createdServerSocket -> this.serverSocket = createdServerSocket);
    this.clients = new HashMap<>();

    timestampVector = new ArrayList<>(Collections.nCopies(Configuration.NUMBER_OF_PROCESSES, 0));
    vectorClocks = new ArrayList<>();
    buffer = new ConcurrentLinkedQueue<>();
    sendingThreads = new ConcurrentLinkedQueue<>();
    FileUtil.clearFile(FileUtil.setupLogFile(port));
  }

  private void addClient(int port, Client client) {
    clients.putIfAbsent(port, client);
  }

  public static String convertBufferToString() {
    return buffer.isEmpty()
        ? "Empty"
        : Process.buffer.stream()
            .map(message -> String.format("[P%s -> P%s]: %s",
                message.getSenderPort(),
                message.getReceiverPort(),
                message.getContent()))
            .collect(Collectors.joining(", "));
  }

  public static void main(String[] args) throws InterruptedException {
    String runningMode = args[1];
    LogUtil.log("Running in %s mode", runningMode);
    Configuration.loadConfigsFromYaml(runningMode);

    int port = Integer.parseInt(args[0]);
    Process process = new Process(port);

    // Create a server if the server socket is created successfully
    if (process.getServerSocket() != null) {
      Server server = new Server(process.getServerSocket());
      Scanner scanner = new Scanner(System.in);

      // Open for connections
      ThreadUtil.start(server::open);

      // Wait until all processes are ready
      LogUtil.log("Press any key to connect to other processes");
      scanner.nextLine();

      // Create a client for each existing port (except the current port)
      createClients(process);

      if (port == Configuration.CONTROLLER_PORT) {
        LogUtil.log("Press any key to send notify messages");
        scanner.nextLine();
        scanner.close();
        runController(process);
      } else if (runningMode.equals(Configuration.DEMO_MODE)) {
        runDemo(process);
      } else if (runningMode.equals(Configuration.EXAMPLE_MODE)) {
        runExampleOne(process);
      }
    }
  }

  private static void createClients(Process process) {
    for (int availablePort : Configuration.PORTS) {
      if (availablePort != process.port && availablePort != Configuration.CONTROLLER_PORT) {
        LogUtil.log("Creating a client for port %s", availablePort);
        Socket socket = SocketUtil.createClientSocket(availablePort);
        if (socket != null) {
          Client client = new Client(process.port, availablePort, socket);
          process.addClient(availablePort, client);
        }
      }
    }
  }

  private static void runController(Process process) {
    for (var clientEntry : process.getClients().entrySet()) {
      Client client = clientEntry.getValue();

      // Send notification to other processes
      Thread notifyThread = ThreadUtil.start(() -> client.sendNotifyMessage());
      sendingThreads.add(notifyThread);
    }

    cleanupSendingThreads(process);
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
      sendingThreads.add(sendingThread);
    }

    cleanupSendingThreads(process);
  }

  private static void runExampleOne(Process process) {
    Client clientWithPort1 = process.getClients().get(1);
    Client clientWithPort2 = process.getClients().get(2);
    Client clientWithPort3 = process.getClients().get(3);

    if (process.port == 2) {
      sendingThreads.add(ThreadUtil.start(() -> clientWithPort1.send(2, 7000, 2000)));
      ThreadUtil.sleep(1000);
      sendingThreads.add(ThreadUtil.start(() -> clientWithPort3.send(1, 2000)));
    } else if (process.port == 3) {
      ThreadUtil.sleep(5000);
      sendingThreads.add(ThreadUtil.start(() -> clientWithPort1.send(1, 7000)));
      ThreadUtil.sleep(1000);
      sendingThreads.add(ThreadUtil.start(() -> clientWithPort2.send(1, 1000)));
    }

    cleanupSendingThreads(process);
  }

  private static void cleanupSendingThreads(Process process) {
    for (Thread thread : sendingThreads) {
      try {
        thread.join();
        thread.interrupt();
        LogUtil.log("Thread %s of port %s is terminated", thread.getName(), process.port);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
