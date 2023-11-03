package org.example.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.example.utils.LogUtil;

import java.sql.Timestamp;
import java.util.ArrayList;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Message {
  private int senderPort;
  private int receiverPort;
  private String content;
  private ArrayList<Integer> timestampVector;
  private ArrayList<VectorClock> vectorClocks;
  private String status;

  public static final String DELIVERED = "delivered";
  public static final String BUFFERED = "buffered";

  public String toString() {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      LogUtil.log("An error occurred while converting message to string: ", e.getMessage());
      return "{}";
    }
  }

  public String toLog() {
    return String.format("[P%s -> P%s]: %s (timestamp vector: %s, vector clocks: %s, status: %s)",
            senderPort,
            receiverPort,
            content,
            timestampVector.toString(),
            vectorClocks.toString(),
            status);
  }

  public static Message parse(String messageString) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.readValue(messageString, Message.class);
    } catch (JsonProcessingException e) {
      LogUtil.log("An error occurred while parsing message from client.\nOriginal message: %s.\nError message: %s", messageString, e.getMessage());
      return null;
    }
  }
}
