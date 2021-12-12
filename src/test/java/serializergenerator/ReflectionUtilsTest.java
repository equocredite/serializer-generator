package serializergenerator;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static serializergenerator.ReflectionUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class Foo {
    private int bar;
    private boolean baz;

    public int getBar() {
        return bar;
    }
}

public class ReflectionUtilsTest {
    @Test
    public void testIsPrimitiveOrWrapperOrString() {
        assertTrue(isPrimitiveOrWrapperOrString(int.class));
        assertTrue(isPrimitiveOrWrapperOrString(Float.class));
        assertTrue(isPrimitiveOrWrapperOrString(String.class));
        assertFalse(isPrimitiveOrWrapperOrString(Person.class));
        assertFalse(isPrimitiveOrWrapperOrString(StringBuilder.class));
    }

    @Test
    public void testCapitalizeFirstLetter() {
        assertNull(capitalizeFirstLetter(null));
        assertEquals("Mda", capitalizeFirstLetter("mda"));
    }

    @Test
    public void testBuildGetterNameForField() throws NoSuchFieldException {
        assertEquals("getBar", buildGetterNameForField(Foo.class.getDeclaredField("bar")));
        assertEquals("isBaz", buildGetterNameForField(Foo.class.getDeclaredField("baz")));
    }

    @Test
    public void testFindValidGetterNames() {
        assertEquals(Collections.emptyMap(), findValidGetterNames(Object.class));
        assertEquals(1, findValidGetterNames(Foo.class).size());
    }
}
