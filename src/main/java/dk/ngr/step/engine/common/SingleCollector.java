package dk.ngr.step.engine.common;

import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class SingleCollector {
  public static <T> Collector<T, ?, Optional<T>> one() {
    return Collectors.collectingAndThen(
            Collectors.toList(),
            list -> {
              if (list.size() == 0) {
                return Optional.empty();
              } if (list.size() == 1) {
                return Optional.of(list.get(0));
              }

              throw new IllegalArgumentException(String.format("Size is not 1. size=%d", list.size()));
            }
    );
  }
}