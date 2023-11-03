package org.example.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.constants.Configuration;
import org.example.utils.LogUtil;

import java.util.List;
import java.util.Optional;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VectorClock {
  private int port;
  private List<Integer> timestampVector;

  public static void increment(int index, List<Integer> timestampVector) {
    timestampVector.set(index, timestampVector.get(index) + 1);
  }

  public static VectorClock findByReceiverPort(List<VectorClock> vectorClocks, int receiverPort) {
    return vectorClocks
            .stream()
            .filter(vectorClock -> vectorClock.getPort() == receiverPort)
            .findFirst()
            .orElse(null);
  }

  public static void updateTimestampVectorInList(
          List<Integer> timestampVector,
          int receiverPort,
          List<VectorClock> vectorClocks) {

    LogUtil.log("Thread %s is updating timestamp vector of port %s to %s", Thread.currentThread().getName(), receiverPort, timestampVector);

    VectorClock vectorClock = findByReceiverPort(vectorClocks, receiverPort);
    Optional.ofNullable(vectorClock).ifPresentOrElse(
            vectorClock1 -> vectorClock1.setTimestampVector(timestampVector),
            () -> vectorClocks.add(VectorClock.builder().port(receiverPort).timestampVector(timestampVector).build()));
  }

  public static boolean isLessThanOrEqual(List<Integer> timestampVector, List<Integer> otherTimestampVector) {
    LogUtil.log("Check whether the timestamp vector %s is less than or equal the timestamp vector %s", timestampVector, otherTimestampVector);
    for (int i = 0; i < Configuration.NUMBER_OF_PROCESSES; i++) {
      if (timestampVector.get(i) > otherTimestampVector.get(i))
        return false;
    }
    return true;
  }

  public static void mergeTimestampVector(List<Integer> source, List<Integer> destination) {
    for (int i = 0; i < source.size(); i++) {
      int maxTimestamp = Math.max(source.get(i), destination.get(i));
      destination.set(i, maxTimestamp);
    }
  }

  public static void mergeVectorClocks(List<VectorClock> source, List<VectorClock> destination) {
    for (VectorClock vectorClock : source) {
      destination.stream().filter(clock -> clock.port == vectorClock.port).findFirst().ifPresent(clock -> {
        clock.setTimestampVector(vectorClock.timestampVector);
      });
    }
  }

  public String toString() {
    return String.format("<%s, %s>", this.port, this.timestampVector.toString());
  }
}
