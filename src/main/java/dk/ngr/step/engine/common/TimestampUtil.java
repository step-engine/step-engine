package dk.ngr.step.engine.common;

import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
public class TimestampUtil {

	private final String pattern; // Eg "yyyy-MM-dd'T'HH:mm:ss.SSS"

  public String getNow() {
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
  }

  public String parse(LocalDateTime dateTime) {
    return dateTime.format(DateTimeFormatter.ofPattern(pattern));
  }

  public LocalDateTime parse(String dateTime) {
    return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(pattern));
  }

  public LocalDateTime now() {
    return LocalDateTime.now();
  }
}