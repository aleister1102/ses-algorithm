package org.example.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.constants.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VectorClock {
  private int port;
  private ArrayList<Integer> timestampVector;

  public static void incrementAt(ArrayList<Integer> timestampVector, int index) {
    timestampVector.set(index, timestampVector.get(index) + 1);
  }

  private static VectorClock findByReceiverPort(ArrayList<VectorClock> vectorClocks, int receiverPort) {
    return vectorClocks
            .stream()
            .filter(vectorClock -> vectorClock.getPort() == receiverPort)
            .findFirst()
            .orElse(null);
  }

  public static void updateTimestampVectorInList(ArrayList<VectorClock> vectorClocks, int receiverPort, ArrayList<Integer> timestampVector) {
    VectorClock vectorClock = findByReceiverPort(vectorClocks, receiverPort);
    Optional.ofNullable(vectorClock).ifPresentOrElse(
            vectorClock1 -> vectorClock1.setTimestampVector(timestampVector),
            () -> vectorClocks.add(VectorClock.builder().port(receiverPort).timestampVector(timestampVector).build())
    );
  }

  public boolean isLessThanOrEqual(ArrayList<Integer> otherTimestampVector) {
    for (int i = 0; i < Configuration.NUMBER_OF_PROCESSES; i++) {
      if (this.timestampVector.get(i) > otherTimestampVector.get(i)) return false;
    }
    return true;
  }

  public static ArrayList<Integer> max(ArrayList<Integer> timestampVector1, ArrayList<Integer> timestampVector2) {
    ArrayList<Integer> maxTimestampVector = new ArrayList<>(Collections.nCopies(10, 0));
    for (int i = 0; i < timestampVector1.size(); i++) {
      int maxTimestamp = Math.max(timestampVector1.get(i), timestampVector2.get(i));
      maxTimestampVector.set(i, maxTimestamp);
    }
    return maxTimestampVector;
  }

  public String toString() {
    return String.format("<%s, %s>", this.port, this.timestampVector.toString());
  }
}
