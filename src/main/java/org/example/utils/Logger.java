package org.example.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;

public class Logger {

  public static String toStringWithTimestamp(String message, Timestamp timestamp) {
    return "[" +
        timestamp.toLocalDateTime() +
        "] - " +
        message +
        "\n";
  }

  public static void logWithCurrentTimeStamp(String message) {
    Timestamp now = new Timestamp(System.currentTimeMillis());
    System.out.println(toStringWithTimestamp(message, now));
  }

  public static void logWithTimeStamp(String message, Timestamp timestamp) {
    System.out.println(toStringWithTimestamp(message, timestamp));
  }

  public static void log(String message) {
    System.out.println(message);
  }

  public static void log(String format, Object... args) {
    log(String.format(format, args));
  }

  public static void logAndWriteWithTimeStamp(String message, Timestamp timestamp) {
    if (!message.isEmpty()) {
      String logMessage = toStringWithTimestamp(message, timestamp);
      log(logMessage);
      writeLog(logMessage);
    }
  }

  public static void writeLog(String log) {
    try {
      String logFileName = "chatroom.log";
      FileWriter writer = new FileWriter(logFileName, true);
      writer.write(log);
      writer.close();
    } catch (IOException e) {
      System.out.println("An error have been occurred while writing log to file: " + e.getMessage());
    }
  }
}
