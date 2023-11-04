package org.example.constants;

import org.example.utils.LogUtil;
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
  public static String RUNNING_MODE;
  public static String DEMO_MODE = "demo";
  public static String EXAMPLE_MODE = "example";
  public static final String LOCALHOST = "localhost";
  public static final String CONFIG_FILE = "src/main/resources/config.yml";

  public static void loadConfigsFromYaml() {
    Yaml yaml = new Yaml();
    try (InputStream inputStream = new FileInputStream(CONFIG_FILE)) {
      Map<String, Object> yamlData = yaml.load(inputStream);
      LogUtil.log("yamlData: %s", yamlData);

      if (yamlData != null) {
        RUNNING_MODE = (String) yamlData.get("mode");

        if (RUNNING_MODE.equals(DEMO_MODE)) {
          Map<String, Object> demo = (Map<String, Object>) yamlData.get("demo");
          NUMBER_OF_PROCESSES = (int) demo.get("numberOfProcesses");
          LogUtil.log("NUMBER_OF_PROCESSES: %s", NUMBER_OF_PROCESSES);

          NUMBER_OF_MESSAGES = (int) demo.get("numberOfMessages");
          LogUtil.log("NUMBER_OF_MESSAGES: %s", NUMBER_OF_MESSAGES);
        } else if (RUNNING_MODE.equals(EXAMPLE_MODE)) {
          Map<String, Object> example = (Map<String, Object>) yamlData.get("example");
          NUMBER_OF_PROCESSES = (int) example.get("numberOfProcesses");
          LogUtil.log("NUMBER_OF_PROCESSES: %s", NUMBER_OF_PROCESSES);
        }

        for (int i = 0; i < NUMBER_OF_PROCESSES; i++) {
          PORTS.add(i + 1);
        }
        LogUtil.log("PORTS: %s", PORTS);
      }
    } catch (Exception e) {
      LogUtil.log("Error(s) occurred while loading configs from %s: %s", CONFIG_FILE, e.getMessage());
      e.printStackTrace();
    }
  }

  public static int getIndexInTimestampVector(int senderPort) {
    for (int i = 0; i < PORTS.size(); i++) {
      if (PORTS.get(i) == senderPort)
        return i;
    }
    return -1;
  }

  public static int randomNumberOfMessagesPerMinute() {
    return (int) (Math.random() * Configuration.NUMBER_OF_MESSAGES + Configuration.NUMBER_OF_MESSAGES / 5); // from NUMBER_OF_MESSAGES/5 to NUMBER_OF_MESSAGES
  }

  public static int calculateSleepTime(int numberOfMessagesPerMinute) {
    return 60000 / numberOfMessagesPerMinute; // 60000 ms = 1 minute
  }
}
