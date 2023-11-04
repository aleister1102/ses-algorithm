package org.example.utils;

public class ThreadUtil {
  public static void start(Runnable runnable) {
    Thread thread = new Thread(runnable);
    thread.start();
  }

  public static void sleep(int milliseconds) {
    try {
      Thread.sleep(milliseconds);
    } catch (InterruptedException exception) {
      LogUtil.log("Error(s) occurred while sleeping: %s", exception.getMessage());
    }
  }
}
