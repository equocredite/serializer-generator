package serializergenerator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Person {
    private final String name;
    private final int age;
    private final Person parent;
    private final Car car;

    public Person(String name, int age, Person parent, Car car) {
        this.name = name;
        this.age = age;
        this.parent = parent;
        this.car = car;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public Person getParent() {
        return parent;
    }

    public Car getCar() {
        return car;
    }
}

class Car {
    private final Long id;
    private final int power;
    private final String brand;

    public Car(Long id, int power, String brand) {
        this.id = id;
        this.power = power;
        this.brand = brand;
    }

    public Long getId() {
        return id;
    }

    public int getPower() {
        return power;
    }

    public String getBrand() {
        return brand;
    }
}

public class XmlSerializerFactoryTest {
    private Serializer<Person> serializer;

    @BeforeEach
    void setup() {
        serializer = new XmlSerializerFactory<>(Person.class).createSerializer();
    }

    @Test
    public void simpleTest() {
        Person john = new Person("John", 64, null, null);
        String expected = "<car>null</car>" +
                          "<parent>null</parent>" +
                          "<age>64</age>" +
                          "<name>John</name>";
        assertEquals(expected, serializer.serialize(john));
    }

    @Test
    public void nestedObjectTest() {
        Car car = new Car(42L, 450, "Volvo");
        Person john = new Person("John", 64, null, car);
        String expected = "<car>" +
                              "<brand>Volvo</brand>" +
                              "<id>42</id>" +
                              "<power>450</power>" +
                          "</car>" +
                          "<parent>null</parent>" +
                          "<age>64</age>" +
                          "<name>John</name>";
        assertEquals(expected, serializer.serialize(john));
    }

    @Test
    public void referencetoSameClassTest() {
        Car johnsCar = new Car(42L, 450, "Volvo");
        Person john = new Person("John", 64, null, johnsCar);

        Car mattsCar = new Car(322L, 300, "Honda");
        Person matt = new Person("Matt", 35, john, mattsCar);

        Person luke = new Person("Luke", 4, matt, null);

        String expected = "<car>null</car>" +
                          "<parent>" +
                              "<car>" +
                                  "<brand>Honda</brand>" +
                                  "<id>322</id>" +
                                  "<power>300</power>" +
                              "</car>" +
                              "<parent>" +
                                  "<car>" +
                                      "<brand>Volvo</brand>" +
                                      "<id>42</id>" +
                                      "<power>450</power>" +
                                  "</car>" +
                                  "<parent>null</parent>" +
                                  "<age>64</age>" +
                                  "<name>John</name>" +
                              "</parent>" +
                              "<age>35</age>" +
                              "<name>Matt</name>" +
                          "</parent>" +
                          "<age>4</age>" +
                          "<name>Luke</name>";
        assertEquals(expected, serializer.serialize(luke));
    }
}
