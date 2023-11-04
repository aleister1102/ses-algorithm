package org.example.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.utils.LogUtil;

import java.util.ArrayList;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Message implements  Cloneable {
  private int senderPort;
  private int receiverPort;
  private String content;
  private ArrayList<Integer> timestampVector;
  private ArrayList<VectorClock> vectorClocks;

  @Builder.Default
  private String status = SENDING;

  public static final String DELIVERY = "delivery";
  public static final String BUFFER = "buffer";
  public static final String SENDING = "sending";

  public String toString() {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      LogUtil.log("Error(s) occurred while converting message to string: ", e.getMessage());
      return "{}";
    }
  }

  public String toLog() {
    return String.format("[P%s -> P%s]: %s (timestamp: %s, clocks: %s, status: %s)",
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
      LogUtil.log("Error(s) occurred while parsing message from client.\nOriginal message: %s.\nError message: %s", messageString, e.getMessage());
      return null;
    }
  }

  @Override
  public Message clone() {
    try {
      Message clone = (Message) super.clone();
      clone.setSenderPort(senderPort);
      clone.setReceiverPort(receiverPort);
      clone.setContent(content);
      clone.setTimestampVector(new ArrayList<>(timestampVector));
      clone.setVectorClocks(new ArrayList<>(vectorClocks));
      clone.setStatus(status);
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }
}
