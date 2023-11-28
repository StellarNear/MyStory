package stellarnear.mystory;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Constants {
    public static String TIME_PATTERN_FORMAT = "dd/MM/yyyy";
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(Constants.TIME_PATTERN_FORMAT)
            .withZone(ZoneId.systemDefault());
}
