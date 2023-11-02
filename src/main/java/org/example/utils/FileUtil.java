package org.example.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtil {
  private static boolean alreadyCleared = false;

  public static final String CENTRAL_LOG_FILE = "logs/central-logs.txt";


  public static void createFile(File file) {
    try {
      if (file.createNewFile()) {
        System.out.println("File created: " + file.getName());
      }
    } catch (IOException e) {
      System.out.println("An error occurred while creating file: " + e.getMessage());
    }
  }

  public static File createFile(String fileName) {
    File file = new File(fileName);
    createFile(file);
    return file;
  }

  public static void clearFile(File file) {
    try {
      if (!file.exists()) return;
      if (alreadyCleared) return;

      FileWriter writer = new FileWriter(file);
      writer.write("");
      writer.close();
      alreadyCleared = true;
    } catch (IOException e) {
      System.out.println("An error have been occurred while clearing file: " + e.getMessage());
    }
  }

  public static File setupLogFile(int port) {
    String logFileName = String.format("logs/process-%s.txt", port);
    File logFile = FileUtil.createFile(logFileName);
    FileUtil.clearFile(logFile);
    return logFile;
  }

  public static File setupCentralLogFile() {
    File logFile = FileUtil.createFile(CENTRAL_LOG_FILE);
    FileUtil.clearFile(logFile);
    return logFile;
  }
}
