package util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setPrettyPrinting()
                .create();
    }

    public static class DurationTypeAdapter extends TypeAdapter<Duration> {
        @Override
        public void write(JsonWriter writer, Duration duration) throws IOException {
            if (duration == null) {
                writer.nullValue();
            } else {
                writer.value(duration.toMinutes());
            }
        }

        @Override
        public Duration read(JsonReader reader) throws IOException {
            if (reader.peek() == JsonToken.NULL) {
                reader.nextNull();
                return null;
            }
            return Duration.ofMinutes(reader.nextLong());
        }
    }

    public static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public void write(JsonWriter writer, LocalDateTime value) throws IOException {
            if (value == null) {
                writer.nullValue();
            } else {
                writer.value(formatter.format(value.withNano(0)));
            }
        }

        @Override
        public LocalDateTime read(JsonReader reader) throws IOException {
            if (reader.peek() == JsonToken.NULL) {
                reader.nextNull();
                return null;
            }
            return LocalDateTime.parse(reader.nextString(), formatter).withNano(0);
        }
    }
}
