package org.example.utils;

import java.sql.Timestamp;

public class LogUtil {

  public static String toStringWithTimestamp(String message, Timestamp timestamp) {
    return "[" +
            timestamp.toLocalDateTime() +
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
}
