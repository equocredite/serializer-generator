package serializergenerator;

import java.io.FileWriter;
import java.io.IOException;

public interface Serializer<T> {
    // serialize as xml string
    String serialize(T o);

    // serialize and store to a file
    default void serialize(T o, String filePath) throws IOException {
        FileWriter writer = new FileWriter(filePath);
        writer.write(serialize(o));
        writer.close();
    }
}
