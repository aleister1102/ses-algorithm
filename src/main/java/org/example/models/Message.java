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
  private Timestamp timestamp;
  private ArrayList<Integer> timestampVector;
  private ArrayList<VectorClock> vectorClocks;

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
    return String.format("[P%s -> P%s]: %s (timestamp: %s, timestampVector: %s, vector clocks: %s)",
            senderPort,
            receiverPort,
            content,
            StringUtils.rightPad(timestamp.toString(), 23, '0'),
            timestampVector.toString(),
            vectorClocks.toString());
  }
}
