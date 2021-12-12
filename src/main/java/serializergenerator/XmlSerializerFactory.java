package serializergenerator;

import net.openhft.compiler.CompilerUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static serializergenerator.ReflectionUtils.*;

public class XmlSerializerFactory<T> implements SerializerFactory<T> {

    private final Class<T> clazz;
    private final Map<Class<?>, String> serializerMethodSources = new HashMap<>();

    private final String serializerClassTemplate;
    private final String serializerMethodTemplate;
    private final String serializerMethodLineTemplateImmediate;
    private final String serializerMethodLineTemplateRecursive;

    public XmlSerializerFactory(Class<T> clazz) {
        this.clazz = clazz;
        this.serializerClassTemplate = loadTemplate("/templates/serializer_class");
        this.serializerMethodTemplate = loadTemplate("/templates/serializer_method");
        this.serializerMethodLineTemplateImmediate = loadTemplate("/templates/serializer_line_immediate");
        this.serializerMethodLineTemplateRecursive = loadTemplate("/templates/serializer_line_recursive");
    }

    @Override
    public Serializer<T> createSerializer() {
        String source = buildSerializerSource();
        return (Serializer<T>) compileAndInstantiate(source);
    }

    private String loadTemplate(String filename) {
        try (InputStream inputStream = getClass().getResourceAsStream(filename);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            var lines = reader.lines().collect(Collectors.toList());
            return String.join("\n", lines);
        } catch (IOException e) {
            return null;
        }
    }

    private String buildSerializerSource() {
        String simpleClassName = clazz.getSimpleName();
        String canonicalClassName = clazz.getCanonicalName();

        generateSerializerMethods();

        StringBuilder methods = new StringBuilder();
        for (var methodSource : serializerMethodSources.values()) {
            methods.append(methodSource).append("\n\n");
        }

        return MessageFormat.format(serializerClassTemplate, simpleClassName, canonicalClassName, methods.toString());
    }

    private static <U> boolean isImmediatelySerializable(Class<U> clazz) {
        return isPrimitiveOrWrapperOrString(clazz);
    }

    private String buildSerializerMethodLine(Field field, String getterName) {
        Class<?> fieldType = field.getType();
        String fieldName = field.getName();
        String capitalizedFieldTypeName = capitalizeFirstLetter(fieldType.getSimpleName());

        if (isImmediatelySerializable(fieldType)) {
            return MessageFormat.format(serializerMethodLineTemplateImmediate, fieldName, getterName);
        } else {
            return MessageFormat.format(serializerMethodLineTemplateRecursive, fieldName, getterName,
                    capitalizedFieldTypeName);
        }
    }

    private <U> void generateSerializerMethodSourceForClass(Class<U> clazz) {
        if (serializerMethodSources.containsKey(clazz)) {
            return;
        }
        Set<Class<?>> serializerMethodsCalledRecursively = new HashSet<>();

        String simpleClassName = clazz.getSimpleName();
        String canonicalClassName = clazz.getCanonicalName();

        StringBuilder lines = new StringBuilder();
        for (var entry : ReflectionUtils.findValidGetterNames(clazz).entrySet()) {
            lines.append(buildSerializerMethodLine(entry.getKey(), entry.getValue())).append("\n");
            serializerMethodsCalledRecursively.add(entry.getKey().getType());
        }

        String methodBody = MessageFormat.format(serializerMethodTemplate, simpleClassName, canonicalClassName,
                lines.toString());
        serializerMethodSources.put(clazz, methodBody);

        for (var type : serializerMethodsCalledRecursively) {
            if (!isImmediatelySerializable(type)) {
                generateSerializerMethodSourceForClass(type);
            }
        }
    }

    private void generateSerializerMethods() {
        generateSerializerMethodSourceForClass(clazz);
    }

    private Object compileAndInstantiate(String source) {
        String serializerClassName = clazz.getSimpleName() + "ToXmlSerializer";
        try {
            return CompilerUtils.CACHED_COMPILER
                    .loadFromJava("serializergenerator." + serializerClassName, source)
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (InvocationTargetException | NoSuchMethodException | ClassNotFoundException | InstantiationException
                | IllegalAccessException e) {
            return null;
        }
    }
}
