package org.example.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.Process;
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

  public static void incrementByPort(int port) {
    int indexInTimestampVector = Configuration.getIndexInTimestampVector(port);
    if (indexInTimestampVector != -1)
      VectorClock.incrementByIndex(indexInTimestampVector);
  }

  private static void incrementByIndex(int index) {
    Process.timestampVector.set(index, Process.timestampVector.get(index) + 1);
  }

  public static VectorClock findByPort(List<VectorClock> vectorClocks, int port) {
    return vectorClocks
            .stream()
            .filter(vectorClock -> vectorClock.getPort() == port)
            .findFirst()
            .orElse(null);
  }

  public static void updateTimestampVectorInList(List<Integer> timestampVector, List<VectorClock> vectorClocks, int senderPort, int receiverPort) {
    VectorClock vectorClock = findByPort(vectorClocks, receiverPort);
    Optional.ofNullable(vectorClock).ifPresentOrElse(
            vectorClock1 -> vectorClock1.setTimestampVector(timestampVector),
            () -> vectorClocks.add(VectorClock.builder().port(receiverPort).timestampVector(timestampVector).build()));

    LogUtil.logAndWriteByPort(senderPort, "Updates timestamp vector of port %s to %s. Current vector clocks: %s",
            receiverPort, timestampVector, vectorClocks);
  }

  public boolean isTimestampVectorLessThanOrEqual(List<Integer> otherTimestampVector) {
    LogUtil.logAndWriteByPort(port, "Check whether the timestamp vector %s is less than or equal the timestamp vector %s",
            timestampVector, otherTimestampVector);

    for (int i = 0; i < Configuration.NUMBER_OF_PROCESSES; i++) {
      if (timestampVector.get(i) > otherTimestampVector.get(i))
        return false;
    }
    return true;
  }

  public static void mergeTimestampVector(List<Integer> source, List<Integer> destination, int port) {
    List<Integer> destinationCopy = List.copyOf(destination);

    for (int i = 0; i < source.size(); i++) {
      int maxTimestamp = Math.max(source.get(i), destination.get(i));
      destination.set(i, maxTimestamp);
    }

    LogUtil.logAndWriteByPort(port, "Merged timestamp vector %s and %s. Current timestamp vector of the process: %s",
            source, destinationCopy, destination);
  }

  public static void mergeVectorClocks(List<VectorClock> source, List<VectorClock> destination, int port) {
    for (VectorClock vectorClock : source) {
      destination.stream().filter(clock -> clock.port == vectorClock.port).findFirst().ifPresent(clock -> {
        clock.setTimestampVector(vectorClock.timestampVector);

        LogUtil.logAndWriteByPort(port, "Merged timestamp vector %s into %s. Current vector clocks of the process: %s",
                vectorClock.timestampVector, clock, destination);
      });
    }
  }

  public String toString() {
    return String.format("<%s, %s>", this.port, this.timestampVector.toString());
  }
}
