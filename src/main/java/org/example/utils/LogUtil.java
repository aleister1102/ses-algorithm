package org.example.utils;

import org.apache.commons.lang3.StringUtils;
import org.example.Process;
import org.example.models.Message;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

public class LogUtil {

  private static String toLogWithTimestampVector(String message, List<Integer> timestampVector) {
    return timestampVector + " - " + message;
  }

  private static String toLogWithSystemTimestamp(String message) {
    Timestamp now = new Timestamp(System.currentTimeMillis());
    return StringUtils.rightPad(now.toString(), 23, '0') + " - " + message;
  }

  private static String formatWithSystemTimestamp(String format, Object... args) {
    String formattedMessage = String.format(format, args);
    return toLogWithSystemTimestamp(formattedMessage);
  }

  public static void logWithSystemTimestamp(String format, Object... args) {
    String messageWithCurrentTimestamp = formatWithSystemTimestamp(format, args);
    log(messageWithCurrentTimestamp);
  }

  public static void logAndWrite(String message, File logFile) {
    log(message);
    writeLogToFile(message, logFile);
    writeLogToFile(message, Process.centralLogFile);
  }

  public static void logAndWriteWithTimestampVectorAndSystemTimestamp(Message message, List<Integer> currentTimestampVector, File logFile, String postfix) {
    String logWithTimestampVector = LogUtil.toLogWithTimestampVector(message.toLog(), currentTimestampVector);
    String logWithCurrentTimestamp = LogUtil.toLogWithSystemTimestamp(logWithTimestampVector);
    String logWithPostfix = logWithCurrentTimestamp + " " + postfix;
    LogUtil.logAndWrite(logWithPostfix, logFile);
  }

  public static void log(String message) {
    System.out.println(message + "\n");
  }

  public static void log(String format, Object... args) {
    log(String.format(format, args));
  }

  private static void writeLogToFile(String log, File logFile) {
    try (FileWriter writer = new FileWriter(logFile, true)) {
      writer.write(log + "\n");
    } catch (IOException e) {
      log("Error(s) have been occurred while writing log to file: " + e.getMessage());
    }
  }
}
