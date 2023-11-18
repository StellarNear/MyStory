package stellarnear.mystory.BooksLibs;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import stellarnear.mystory.Constants;

public class Note {
    private final Instant creation;
    private final String title;
    private final String note;

    public Note(String title, String note) {
        this.title = title;
        this.note = note;
        this.creation = Instant.now();
    }

    public Instant getCreation() {
        return creation;
    }

    public String getTitle() {
        return title;
    }

    public String getNote() {
        return note;
    }

    public String getCreationDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.TIME_PATTERN_FORMAT)
                .withZone(ZoneId.systemDefault());
        return formatter.format(creation);
    }
}
