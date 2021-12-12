package serializergenerator;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ReflectionUtils {
    private ReflectionUtils() {}

    private static final Set<Class<?>> PRIMITIVE_WRAPPER_TYPES = Set.of(Byte.class, Short.class, Integer.class,
            Long.class, Float.class, Double.class, Character.class, Boolean.class);

    public static <T> boolean isPrimitiveOrWrapperOrString(Class<T> clazz) {
        return clazz.equals(String.class) || clazz.isPrimitive() || PRIMITIVE_WRAPPER_TYPES.contains(clazz);
    }

    public static String capitalizeFirstLetter(String s) {
        if (s == null) {
            return null;
        }
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static String buildGetterNameForField(Field field) {
        Class<?> type = field.getType();
        String prefix = type.equals(Boolean.class) || type.equals(boolean.class) ? "is" : "get";
        return prefix + capitalizeFirstLetter(field.getName());
    }

    public static <U> Map<Field, String> findValidGetterNames(Class<U> clazz) {
        if (clazz == null) {
            return new HashMap<>();
        }
        Map<Field, String> fieldToGetterName = findValidGetterNames(clazz.getSuperclass());
        for (Field field : clazz.getDeclaredFields()) {
            String getterName = buildGetterNameForField(field);
            try {
                clazz.getMethod(getterName);
                fieldToGetterName.put(field, getterName);
            } catch (NoSuchMethodException ignored) {}
        }
        return fieldToGetterName;
    }
}
