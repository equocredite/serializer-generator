package serializergenerator;

public interface SerializerFactory<T> {
    Serializer<T> createSerializer();
}
