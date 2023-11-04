package org.example.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.Process;
import org.example.constants.Configuration;
import org.example.utils.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
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

    LogUtil.logAndWriteByPort(senderPort, "Update timestamp vector of port %s in vector clocks to %s. Current vector clocks: %s",
            receiverPort, timestampVector, vectorClocks);
  }

  public boolean isTimestampVectorLessThanOrEqual(List<Integer> otherTimestampVector) {
    for (int i = 0; i < Configuration.NUMBER_OF_PROCESSES; i++) {
      if (timestampVector.get(i) > otherTimestampVector.get(i)) {
        LogUtil.logAndWriteByPort(port, "Timestamp vector %s is not less than or equal the timestamp vector %s",
                timestampVector, otherTimestampVector);
        return false;
      }
    }
    LogUtil.logAndWriteByPort(port, "Timestamp vector %s <= timestamp vector %s",
            timestampVector, otherTimestampVector);
    return true;
  }

  public static List<Integer> mergeTimestampVector(List<Integer> source, List<Integer> destination) {
    List<Integer> mergedTimestampVector = new ArrayList<>(Collections.nCopies(Configuration.NUMBER_OF_PROCESSES, 0));

    for (int i = 0; i < source.size(); i++) {
      int maxTimestamp = Math.max(source.get(i), destination.get(i));
      mergedTimestampVector.set(i, maxTimestamp);
    }

    return mergedTimestampVector;
  }

  public static void mergeVectorClocks(List<VectorClock> source, List<VectorClock> destination) {
    for (VectorClock sourceClock : source) {
      VectorClock destinationClock = findByPort(destination, sourceClock.port);
      if (destinationClock != null) {
        List<Integer> mergedTimestampVector = mergeTimestampVector(sourceClock.timestampVector, destinationClock.timestampVector);
        destinationClock.setTimestampVector(mergedTimestampVector);
      } else {
        destination.add(sourceClock);
      }
    }
  }

  public static List<VectorClock> copyVectorClocksOfProcess() {
    List<VectorClock> vectorClocksCopy = new ArrayList<>();
    for (VectorClock clock : Process.vectorClocks) {
      VectorClock copy = new VectorClock();
      copy.setPort(clock.getPort());
      copy.setTimestampVector(new ArrayList<>(clock.getTimestampVector()));
      vectorClocksCopy.add(copy);
    }
    return vectorClocksCopy;
  }

  public String toString() {
    return String.format("<%s, %s>", this.port, this.timestampVector.toString());
  }
}
