package org.example.utils;

import org.apache.commons.lang3.StringUtils;

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
}
