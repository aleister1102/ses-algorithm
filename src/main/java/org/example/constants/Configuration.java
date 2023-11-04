package org.example.constants;

import java.util.Arrays;

public class Configuration {
  public static final String LOCALHOST = "localhost";
  public static final Integer[] PORTS = {1, 2, 3};
  public static final int NUMBER_OF_PROCESSES = PORTS.length;
  public static final int NUMBER_OF_MESSAGES = 20;
  public static final String CENTRAL_LOG_FILE = "logs/central-logs.txt";

  public static int getIndexInTimestampVector(int senderPort) {
    return Arrays.asList(PORTS).indexOf(senderPort);
  }

  public static int randomNumberOfMessagesPerMinute() {
    return (int) (Math.random() * (Configuration.NUMBER_OF_MESSAGES - 10 + 1) + 10); // from 10 to NUMBER_OF_MESSAGES
  }

  public static int calculateSleepTime(int numberOfMessagesPerMinute) {
    return 60000 / numberOfMessagesPerMinute; // 60000 ms = 1 minute
  }
}
