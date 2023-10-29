package org.example.utils;

import org.apache.commons.lang3.StringUtils;
import org.example.models.Message;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;

public class LogUtil {

  public static String toStringWithTimestamp(String message, Timestamp timestamp) {
    return "[" +
            StringUtils.rightPad(timestamp.toLocalDateTime().toString(), 23, "0") +
            "] - " +
            message +
            "\n";
  }

  public static String toStringWithCurrentTimeStamp(String message) {
    Timestamp now = new Timestamp(System.currentTimeMillis());
    return toStringWithTimestamp(message, now);
  }

  public static void logWithCurrentTimeStamp(String message) {
    Timestamp now = new Timestamp(System.currentTimeMillis());
    System.out.println(toStringWithTimestamp(message, now));
  }

  public static void log(String message) {
    System.out.println(message);
  }

  public static void log(String format, Object... args) {
    log(String.format(format, args));
  }


  public static void logAndWriteToFile(Message message, File logFile) {
    String log = LogUtil.toStringWithCurrentTimeStamp(message.toLog());
    LogUtil.log(log);
    writeLogToFile(log, logFile);
  }

  private static void writeLogToFile(String log, File logFile) {
    try {
      FileWriter writer = new FileWriter(logFile, true); // append to file (not overwrite)
      writer.write(log);
      writer.close();
    } catch (IOException e) {
      System.out.println("An error have been occurred while writing log to file: " + e.getMessage());
    }
  }
}
