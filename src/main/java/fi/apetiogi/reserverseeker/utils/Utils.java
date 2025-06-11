package fi.apetiogi.reserverseeker.utils;

import java.time.Duration;
import java.time.Instant;

public class Utils {
    public static String get_days_hours_minutes(Long unix) {
            Duration time_between = Duration.between(Instant.ofEpochSecond(unix), Instant.now());

            long days = time_between.toDays();
            time_between = time_between.minusDays(days);

            long hours = time_between.toHours();
            time_between = time_between.minusHours(hours);

            long minutes = time_between.toMinutes();

            return String.format("%dd %dh %dm", days, hours, minutes);
    }
}