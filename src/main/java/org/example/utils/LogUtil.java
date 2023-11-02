package org.example.utils;

import org.apache.commons.lang3.StringUtils;
import org.example.constants.Configuration;
import org.example.models.Message;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;

public class LogUtil {
  public static String toStringWithTimestampVector(String message, ArrayList<Integer> timeStampVector) {
    return timeStampVector + " - " + message + "\n";
  }

  public static void writeLogToFile(String log, File logFile) {
    try {
      FileWriter writer = new FileWriter(logFile, true); // append to file (not overwrite)
      writer.write(log);
      writer.close();
    } catch (IOException e) {
      System.out.println("An error have been occurred while writing log to file: " + e.getMessage());
    }
  }

  public static void log(String message) {
    System.out.println(message);
  }

  public static void log(String format, Object... args) {
    log(String.format(format, args));
  }
}
