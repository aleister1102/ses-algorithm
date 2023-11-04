package org.example.constants;

public class Configuration {
  public static final String LOCALHOST = "localhost";
  public static final int[] PORTS = new int[3];
  public static final int NUMBER_OF_PROCESSES = PORTS.length;
  public static final int NUMBER_OF_MESSAGES = 5;
  public static final String CENTRAL_LOG_FILE = "logs/central-logs.txt";

  static {
    for (int i = 0; i < PORTS.length; i++) {
      PORTS[i] = i + 1;
    }
  }

  public static int getIndexInTimestampVector(int senderPort) {
    for (int i = 0; i < PORTS.length; i++) {
      if (PORTS[i] == senderPort)
        return i;
    }
    return -1;
  }

  public static int randomNumberOfMessagesPerMinute() {
    return (int) (Math.random() * Configuration.NUMBER_OF_MESSAGES + 1); // from 1 to NUMBER_OF_MESSAGES
  }

  public static int calculateSleepTime(int numberOfMessagesPerMinute) {
    return 60000 / numberOfMessagesPerMinute; // 60000 ms = 1 minute
  }
}
