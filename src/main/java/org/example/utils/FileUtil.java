package org.example.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtil {
  private static boolean alreadyCleared = false;

  public static void createFile(File file) {
    try {
      if (file.createNewFile()) {
        System.out.println("File created: " + file.getName());
      } else {
        System.out.println("File already exists.");
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

  public static synchronized void clearFile(File file) {
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

  public static void writeLog(String log, File logFile) {
    try {
      FileWriter writer = new FileWriter(logFile, true); // append to file (not overwrite)
      writer.write(log);
      writer.close();
    } catch (IOException e) {
      System.out.println("An error have been occurred while writing log to file: " + e.getMessage());
    }
  }
}
