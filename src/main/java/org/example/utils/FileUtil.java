package org.example.utils;


import org.example.constants.Configuration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtil {

  public static void createFile(File file) {
    try {
      if (file.createNewFile()) {
        LogUtil.log("File created: %s", file.getName());
      }
    } catch (IOException e) {
      LogUtil.log("Error(s) occurred while creating file: ", e.getMessage());
    }
  }

  public static File createFile(String filePath) {
    File file = new File(filePath);
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
      LogUtil.log("Error(s) have been occurred while clearing file: ", e.getMessage());
    }
  }

  public static File setupLogFile(int port) {
    String logFileName = String.format("logs/process-%s.txt", port);
    return FileUtil.createFile(logFileName);
  }
}
