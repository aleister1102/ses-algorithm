package org.example.constants;

import org.example.utils.LogUtil;
import java.util.Random;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Configuration {
  public static int NUMBER_OF_PROCESSES;
  public static List<Integer> PORTS = new LinkedList<>();
  public static int NUMBER_OF_MESSAGES;
  public static int MIN_MSG_PER_MINUTE;
  public static int MAX_MSG_PER_MINUTE;
  public static String DEMO_MODE = "demo";
  public static String EXAMPLE_MODE = "example";
  public static int CONTROLLER_PORT = 0;
  public static final String LOCALHOST = "localhost";
  public static final String CONFIG_FILE = "src/main/resources/config.yml";

  public static void loadConfigsFromYaml(String runningMode) {
    Yaml yaml = new Yaml();
    try (InputStream inputStream = new FileInputStream(CONFIG_FILE)) {
      Map<String, Object> yamlData = yaml.load(inputStream);

      if (yamlData != null) {
        if (runningMode.equals(DEMO_MODE)) {
          loadDemoConfigs(yamlData);

          LogUtil.log("Number of proceses: %s", NUMBER_OF_PROCESSES);
          LogUtil.log("Number of messages: %s", NUMBER_OF_MESSAGES);
          LogUtil.log("Minimum messages per minute: %s", MIN_MSG_PER_MINUTE);
          LogUtil.log("Maximum messages per minute: %s", MAX_MSG_PER_MINUTE);
        } else if (runningMode.equals(EXAMPLE_MODE)) {
          loadExampleConfigs(yamlData);
          LogUtil.log("Number of proceses: %s", NUMBER_OF_PROCESSES);
        }

        for (int i = 0; i <= NUMBER_OF_PROCESSES; i++) {
          PORTS.add(i); // port 0 is for controller process
        }

        LogUtil.log("Ports: %s", PORTS);
      }
    } catch (Exception e) {
      LogUtil.log("Error(s) occurred while loading configs from %s: %s", CONFIG_FILE, e.getMessage());
      e.printStackTrace();
    }
  }

  private static void loadDemoConfigs(Map<String, Object> yamlData) {
    Map<String, Object> demo = (Map<String, Object>) yamlData.get("demo");
    NUMBER_OF_PROCESSES = (int) demo.get("numberOfProcesses");
    NUMBER_OF_MESSAGES = (int) demo.get("numberOfMessages");
    MIN_MSG_PER_MINUTE = (int) demo.get("minimumMessagesPerMinute");
    MAX_MSG_PER_MINUTE = (int) demo.get("maximumMessagesPerMinute");
  }

  private static void loadExampleConfigs(Map<String, Object> yamlData) {
    Map<String, Object> example = (Map<String, Object>) yamlData.get("example");
    NUMBER_OF_PROCESSES = (int) example.get("numberOfProcesses");
  }

  public static int getIndexInTimestampVector(int senderPort) {
    for (int i = 0; i < PORTS.size(); i++) {
      if (PORTS.get(i) == senderPort)
        return i - 1; // port 0 is for controller process
    }
    return -1;
  }

  public static int randomNumberOfMessagesPerMinute() {
    Random random = new Random();
    return random.nextInt(MAX_MSG_PER_MINUTE - MIN_MSG_PER_MINUTE + 1) + MIN_MSG_PER_MINUTE;
  }

  public static int calculateSleepTime(int numberOfMessagesPerMinute) {
    return 60000 / numberOfMessagesPerMinute; // 60000 ms = 1 minute
  }
}
