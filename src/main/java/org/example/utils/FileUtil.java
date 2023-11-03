package org.example.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtil {
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
      FileWriter writer = new FileWriter(file);
      writer.write("");
      writer.close();
    } catch (IOException e) {
      System.out.println("An error have been occurred while clearing file: " + e.getMessage());
    }
  }

  public static File setupLogFile(int port) {
    String logFileName = String.format("logs/process-%s.txt", port);
    return FileUtil.createFile(logFileName);
  }

  public static File setupCentralLogFile() {
    return FileUtil.createFile(CENTRAL_LOG_FILE);
  }
}
