package org.example.constants;

import java.util.Arrays;

public class Configuration {
  public static final int NUMBER_OF_MESSAGES = 20;
  public static final Integer[] PORTS = {1, 2, 3};
  public static final int NUMBER_OF_PROCESSES = PORTS.length;

  public static int getIndexInTimestampVector(int senderPort) {
    return Arrays.asList(PORTS).indexOf(senderPort);
  }
}
