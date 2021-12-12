package serializergenerator;

public interface Serializer<T> {
    // serialize as xml string
    String serialize(T o);
}
