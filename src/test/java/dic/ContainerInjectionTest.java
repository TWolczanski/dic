package dic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;


public class ContainerInjectionTest {
    private interface IFoo {
        
    }

    private class Foo implements IFoo {
        public Foobar fb;
        public String s;

        public Foo() { }

        @DependencyConstructor
        public Foo(Foobar fb) {
            this.fb = fb;
        }

        public Foo(String s) {
            this.s = s;
        }
    }

    private class Bar implements IFoo {
        public IFoo f;
        public String s;

        public Bar() { }

        public Bar(IFoo f) {
            this.f = f;
        }
        
        public Bar(IFoo f, String s) {
            this.f = f;
            this.s = s;
        }
    }

    private abstract class Foobar {
        
    }

    private class Baz extends Foobar {
        public Foo f;
        public Bar b;

        public Baz() { }

        public Baz(Bar b) {
            this.b = b;
        }
    }

    private class Qux extends Baz {
        public Integer n;
        public String s;

        public Qux(Integer n) {
            this.n = n;
        }

        public Qux(String s) {
            this.s = s;
        }
    }

    private class Barbaz extends Foobar {
        public Barbaz() { }
    }
    
    @Test
    public void resolveTest1() throws
        InstantiationException,
        IllegalAccessException,
        IllegalArgumentException,
        InvocationTargetException,
        NoSuchMethodException,
        SecurityException
    {
        SimpleContainer c = new SimpleContainer();
        c.registerType(Foobar.class, Barbaz.class, true);
        Foo f = (Foo) c.resolve(Foo.class);
        assertNotNull(f.fb);
    }

    // String constructor ambiguity
    @Test(expected = ConstructorAmbiguityException.class)
    public void resolveTest2() throws
        InstantiationException,
        IllegalAccessException,
        IllegalArgumentException,
        InvocationTargetException,
        NoSuchMethodException,
        SecurityException
    {
        SimpleContainer c = new SimpleContainer();
        c.registerType(IFoo.class, Foo.class, false);
        c.registerType(Foobar.class, Barbaz.class, true);
        Bar f = (Bar) c.resolve(Bar.class);
    }

    @Test
    public void resolveTest3() throws
        InstantiationException,
        IllegalAccessException,
        IllegalArgumentException,
        InvocationTargetException,
        NoSuchMethodException,
        SecurityException
    {
        SimpleContainer c = new SimpleContainer();
        c.registerType(IFoo.class, Foo.class, false);
        c.registerType(Foobar.class, Barbaz.class, true);
        c.registerInstance(String.class, "abc");
        Bar b = (Bar) c.resolve(Bar.class);
        assertNotNull(b.f);
        assertNotNull(b.s);
    }

    @Test
    public void resolveTest4() throws
        InstantiationException,
        IllegalAccessException,
        IllegalArgumentException,
        InvocationTargetException,
        NoSuchMethodException,
        SecurityException
    {
        SimpleContainer c = new SimpleContainer();
        c.registerType(IFoo.class, Foo.class, false);
        c.registerType(Foobar.class, Barbaz.class, true);
        c.registerInstance(String.class, "abc");
        Baz b = (Baz) c.resolve(Baz.class);
        assertNotNull(b.b);
    }

    @Test(expected = CycleException.class)
    public void resolveCycleTest1() throws
        InstantiationException,
        IllegalAccessException,
        IllegalArgumentException,
        InvocationTargetException,
        NoSuchMethodException,
        SecurityException
    {
        SimpleContainer c = new SimpleContainer();
        c.registerType(IFoo.class, Bar.class, false);
        c.registerInstance(String.class, "abc");
        Bar b = (Bar) c.resolve(Bar.class);
    }

    @Test(expected = CycleException.class)
    public void resolveCycleTest2() throws
        InstantiationException,
        IllegalAccessException,
        IllegalArgumentException,
        InvocationTargetException,
        NoSuchMethodException,
        SecurityException
    {
        SimpleContainer c = new SimpleContainer();
        c.registerType(IFoo.class, Foo.class, false);
        c.registerType(Foobar.class, Baz.class, false);
        c.registerInstance(String.class, "abc");
        Foo f = (Foo) c.resolve(Foo.class);
    }

    @Test(expected = ConstructorAmbiguityException.class)
    public void resolveAmbiguityTest() throws
        InstantiationException,
        IllegalAccessException,
        IllegalArgumentException,
        InvocationTargetException,
        NoSuchMethodException,
        SecurityException
    {
        SimpleContainer c = new SimpleContainer();
        c.registerInstance(Integer.class, Integer.valueOf(5));
        c.registerInstance(String.class, "abc");
        Qux q = (Qux) c.resolve(Qux.class);
    }
}
