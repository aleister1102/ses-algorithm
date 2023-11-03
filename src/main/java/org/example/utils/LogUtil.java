package org.example.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

public class LogUtil {
  public static String toStringWithTimestampVector(String message, List<Integer> timeStampVector) {
    return timeStampVector + " - " + message + "\n";
  }

  public static void writeLogToFile(String log, File logFile) {
    try (FileWriter writer = new FileWriter(logFile, true)) {
      writer.write(log);
    } catch (IOException e) {
      log("An error have been occurred while writing log to file: " + e.getMessage());
    }
  }

  public static void log(String message) {
    System.out.println(message);
  }

  public static void log(String format, Object... args) {
    log(String.format(format, args));
  }

  public static void logWithCurrentTimestamp(String format, Object... args) {
    format = new Timestamp(System.currentTimeMillis()).toString() + " - " + format;
    log(String.format(format, args));
  }
}
