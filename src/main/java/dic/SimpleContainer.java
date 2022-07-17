package dic;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SimpleContainer {
    private Map<Class<?>, Class<?>> fromTo = new HashMap<Class<?>, Class<?>>();
    private Set<Class<?>> singletons = new HashSet<Class<?>>();
    private Map<Class<?>, Object> instances = new HashMap<Class<?>, Object>();

    public void registerType(Class<?> type, boolean singleton) {
        int mod = type.getModifiers();
        if(Modifier.isInterface(mod) || Modifier.isAbstract(mod)) {
            throw new InvalidRegistrationException(
                type.getName() + " is not instantiable"
            );
        }
        if(singleton) {
            singletons.add(type);
        }
        else if(singletons.contains(type)) {
            singletons.remove(type);
            instances.remove(type);
        }
    }

    public void registerType(Class<?> from, Class<?> to, boolean singleton) {
        if(!from.isAssignableFrom(to)) {
            throw new InvalidRegistrationException(
                from.getName() + " is neither superclass nor superinterface of " + to.getName()
            );
        }
        singletons.remove(from);
        if(singleton) {
            singletons.add(from);
        }
        instances.remove(from);
        fromTo.put(from, to);
    }

    public void registerInstance(Class<?> type, Object instance) {
        if(!type.isAssignableFrom(instance.getClass())) {
            throw new InvalidRegistrationException(
                type.getName() + " is not a proper type for the specified instance"
            );
        }
        singletons.add(type);
        instances.put(type, instance);
        if(!type.equals(instance.getClass())) {
            fromTo.put(type, instance.getClass());
        }
    }

    public Object resolve(Class<?> type) throws
        InstantiationException,
        IllegalAccessException,
        IllegalArgumentException,
        InvocationTargetException,
        NoSuchMethodException,
        SecurityException
    {
        return _resolve(type, new HashSet<Class<?>>());
    }

    private Object _resolve(Class<?> type, Set<Class<?>> occurred) throws
        InstantiationException,
        IllegalAccessException,
        IllegalArgumentException,
        InvocationTargetException,
        NoSuchMethodException,
        SecurityException
    {
        if(fromTo.containsKey(type)) {
            Class<?> to = fromTo.get(type);
            if(singletons.contains(type) && instances.containsKey(type)) {
                return instances.get(type);
            }
            Set<Class<?>> o = new HashSet<Class<?>>();
            o.addAll(occurred);
            o.add(type);
            Object instance = _resolve(to, o);
            if(singletons.contains(type) && !instances.containsKey(type)) {
                instances.put(type, instance);
            }
            return instance;
        }
        else {
            int mod = type.getModifiers();
            if(Modifier.isInterface(mod) || Modifier.isAbstract(mod)) {
                throw new NotRegisteredException(
                    type.getName() + " hasn't been registered"
                );
            }
            if(singletons.contains(type) && instances.containsKey(type)) {
                return instances.get(type);
            }

            Constructor<?>[] ctors = type.getConstructors();
            int maxLen = 0;
            Constructor<?> ctor = null;
            for(Constructor<?> c : ctors) {
                if(c.isAnnotationPresent(DependencyConstructor.class)) {
                    ctor = c;
                    break;
                }
                int len = c.getParameterCount();
                if(len > maxLen) {
                    maxLen = len;
                }
            }

            if(ctor == null) {
                int count = 0;
                for(Constructor<?> c : ctors) {
                    int len = c.getParameterCount();
                    if(len == maxLen) {
                        ctor = c;
                        count++;
                    }
                }
                if(count > 1) {
                    throw new ConstructorAmbiguityException(
                        type.getName() + " has two or more constructors with the same (maximum) number of parameters"
                    );
                }
            }
            // annotation found
            else {
                int count = 0;
                for(Constructor<?> c : ctors) {
                    if(c.isAnnotationPresent(DependencyConstructor.class)) {
                        count++;
                    }
                }
                if(count > 1) {
                    throw new ConstructorAmbiguityException(
                        type.getName() + " has two or more constructors with the DependencyConstructor annotation"
                    );
                }
            }

            Class<?>[] paramTypes = ctor.getParameterTypes();
            Object[] params = new Object[ctor.getParameterCount()];

            for(int i = 0; i < ctor.getParameterCount(); i++) {
                Class<?> pt = paramTypes[i];
                if(occurred.contains(pt)) {
                    throw new CycleException(
                        "Cycle starting at " + pt.getName() + " has been detected"
                    );
                }
                Set<Class<?>> o = new HashSet<Class<?>>();
                o.addAll(occurred);
                o.add(pt);
                params[i] = _resolve(pt, o);
            }

            Object instance = ctor.newInstance(params);
            if(singletons.contains(type) && !instances.containsKey(type)) {
                instances.put(type, instance);
            }
            return instance;
        }
    }
}
