package dic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

public class ContainerTest {
    interface IFoo {
        
    }

    class Foo implements IFoo {
        public Foo() { }
    }

    class Bar implements IFoo {
        public Bar() { }
    }

    abstract class Foobar {
        
    }

    class Baz extends Foobar {
        public Baz() { }
    }

    class Qux extends Baz {
        public Qux() { }
    }

    @Test
    public void registerTypeTest1() throws
        InstantiationException,
        IllegalAccessException,
        IllegalArgumentException,
        InvocationTargetException,
        NoSuchMethodException,
        SecurityException
    {
        SimpleContainer c = new SimpleContainer();

        c.registerType(Foo.class, false);
        Foo f1 = (Foo) c.resolve(Foo.class);
        Foo f2 = (Foo) c.resolve(Foo.class);
        assertNotEquals(f1, f2);

        c.registerType(Bar.class, true);
        Bar b1 = (Bar) c.resolve(Bar.class);
        Bar b2 = (Bar) c.resolve(Bar.class);
        assertEquals(b1, b2);

        c.registerType(Bar.class, false);
        Bar b3 = (Bar) c.resolve(Bar.class);
        Bar b4 = (Bar) c.resolve(Bar.class);
        assertNotEquals(b2, b3);
        assertNotEquals(b2, b4);
        assertNotEquals(b3, b4);
    }

    @Test
    public void registerTypeTest2() throws
        InstantiationException,
        IllegalAccessException,
        IllegalArgumentException,
        InvocationTargetException,
        NoSuchMethodException,
        SecurityException
    {
        SimpleContainer c = new SimpleContainer();

        c.registerType(IFoo.class, Foo.class, false);
        IFoo f1 = (IFoo) c.resolve(IFoo.class);
        assertEquals(f1.getClass(), Foo.class);

        c.registerType(IFoo.class, Bar.class, false);
        IFoo f2 = (IFoo) c.resolve(IFoo.class);
        assertEquals(f2.getClass(), Bar.class);

        c.registerType(Baz.class, Qux.class, false);
        Baz b = (Baz) c.resolve(Baz.class);
        assertEquals(b.getClass(), Qux.class);

        c.registerType(Foobar.class, Qux.class, false);
        Foobar fb = (Foobar) c.resolve(Foobar.class);
        assertEquals(fb.getClass(), Qux.class);
    }

    @Test(expected = NotRegisteredException.class)
    public void interfaceNotRegisteredTest() throws
        InstantiationException,
        IllegalAccessException,
        IllegalArgumentException,
        InvocationTargetException,
        NoSuchMethodException,
        SecurityException
    {
        SimpleContainer c = new SimpleContainer();
        IFoo f = (IFoo) c.resolve(IFoo.class);
    }

    @Test(expected = NotRegisteredException.class)
    public void abstractNotRegisteredTest() throws
        InstantiationException,
        IllegalAccessException,
        IllegalArgumentException,
        InvocationTargetException,
        NoSuchMethodException,
        SecurityException
    {
        SimpleContainer c = new SimpleContainer();
        Foobar f = (Foobar) c.resolve(Foobar.class);
    }

    @Test
    public void instantiableNotRegisteredTest() throws
        InstantiationException,
        IllegalAccessException,
        IllegalArgumentException,
        InvocationTargetException,
        NoSuchMethodException,
        SecurityException
    {
        SimpleContainer c = new SimpleContainer();
        Foo f = (Foo) c.resolve(Foo.class);
    }

    @Test(expected = InvalidRegistrationException.class)
    public void invalidRegistrationTest1() {
        SimpleContainer c = new SimpleContainer();
        c.registerType(IFoo.class, false);
    }

    @Test(expected = InvalidRegistrationException.class)
    public void invalidRegistrationTest2() {
        SimpleContainer c = new SimpleContainer();
        c.registerType(Foo.class, Bar.class, false);
    }

    @Test
    public void registerInstanceTest1() throws
        InstantiationException,
        IllegalAccessException,
        IllegalArgumentException,
        InvocationTargetException,
        NoSuchMethodException,
        SecurityException
    {
        SimpleContainer c = new SimpleContainer();
        IFoo f1 = new Foo();
        c.registerInstance(IFoo.class, f1);
        IFoo f2 = (IFoo) c.resolve(IFoo.class);
        assertEquals(f1, f2);
    }

    @Test
    public void registerInstanceTest2() throws
        InstantiationException,
        IllegalAccessException,
        IllegalArgumentException,
        InvocationTargetException,
        NoSuchMethodException,
        SecurityException
    {
        SimpleContainer c = new SimpleContainer();
        IFoo f1 = new Foo();
        IFoo b1 = new Bar();
        IFoo f2;

        c.registerInstance(IFoo.class, f1);
        c.registerInstance(IFoo.class, b1);
        c.registerType(IFoo.class, Bar.class, true);
        f2 = (IFoo) c.resolve(IFoo.class);
        assertNotEquals(f2, f1);
        assertNotEquals(f2, b1);
        assertEquals(f2.getClass(), Bar.class);

        c.registerInstance(IFoo.class, b1);
        c.registerType(IFoo.class, Bar.class, true);
        c.registerInstance(IFoo.class, f1);
        f2 = (IFoo) c.resolve(IFoo.class);
        assertEquals(f2, f1);
    }

    @Test(expected = InvalidRegistrationException.class)
    public void invalidRegistrationTest3() {
        SimpleContainer c = new SimpleContainer();
        IFoo f = new Foo();
        c.registerInstance(Baz.class, f);
    }

}
