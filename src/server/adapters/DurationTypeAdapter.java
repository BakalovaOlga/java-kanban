package server.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;

public class DurationTypeAdapter extends TypeAdapter<Duration> {
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
