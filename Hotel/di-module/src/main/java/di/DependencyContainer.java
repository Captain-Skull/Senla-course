package di;

import annotations.Bean;
import annotations.Component;
import annotations.Configuration;
import annotations.Singleton;
import annotations.Inject;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DependencyContainer {
    private final Map<String, Object> singletons = new ConcurrentHashMap<>();
    private final Map<String, Class<?>> components = new ConcurrentHashMap<>();
    private final Map<Class<?>, Object> typeToInstance = new ConcurrentHashMap<>();
    private final Map<Class<?>, Object> configurationInstances = new ConcurrentHashMap<>();

    public void registerComponent(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Component.class) ||
                clazz.isAnnotationPresent(Configuration.class)) {

            Component componentAnno = clazz.getAnnotation(Component.class);
            String beanName = (componentAnno != null && !componentAnno.name().isEmpty()) ?
                    componentAnno.name() : clazz.getSimpleName();

            components.put(beanName, clazz);

            if (clazz.isAnnotationPresent(Singleton.class) ||
                    clazz.isAnnotationPresent(Configuration.class)) {
                Object instance = createInstance(clazz);
                singletons.put(beanName, instance);
                typeToInstance.put(clazz, instance);

                if (clazz.isAnnotationPresent(Configuration.class)) {
                    configurationInstances.put(clazz, instance);
                    registerConfigurationBeans(instance);
                }
            }
        }
    }

    private void registerConfigurationBeans(Object configInstance) {
        Class<?> configClass = configInstance.getClass();

        for (var method : configClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Bean.class)) {
                try {
                    method.setAccessible(true);
                    Object bean = method.invoke(configInstance);
                    Bean beanAnno = method.getAnnotation(Bean.class);
                    String beanName = beanAnno.name().isEmpty() ?
                            method.getName() : beanAnno.name();

                    components.put(beanName, bean.getClass());
                    typeToInstance.put(bean.getClass(), bean);

                    if (bean.getClass().isAnnotationPresent(Singleton.class)) {
                        singletons.put(beanName, bean);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to create bean from configuration", e);
                }
            }
        }
    }

    public Object getBean(String name) {
        if (singletons.containsKey(name)) {
            return singletons.get(name);
        }

        Class<?> clazz = components.get(name);
        if (clazz == null) {
            throw new IllegalArgumentException("No bean found with name: " + name);
        }

        return createInstance(clazz);
    }

    private Object createInstance(Class<?> clazz) {
        try {
            var constructors = clazz.getConstructors();
            var constructor = Arrays.stream(constructors)
                    .filter(c -> c.isAnnotationPresent(Inject.class))
                    .findFirst()
                    .orElseGet(() -> {
                        try {
                            return clazz.getDeclaredConstructor();
                        } catch (NoSuchMethodException e) {
                            throw new RuntimeException("No default constructor found for " + clazz.getName(), e);
                        }
                    });

            Object[] params = Arrays.stream(constructor.getParameterTypes())
                    .map(this::resolveDependency)
                    .toArray();

            Object instance = constructor.newInstance(params);

            injectFields(instance);

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + clazz.getName(), e);
        }
    }

    private Object resolveDependency(Class<?> type) {
        for (Map.Entry<Class<?>, Object> entry : typeToInstance.entrySet()) {
            if (type.isAssignableFrom(entry.getKey())) {
                return entry.getValue();
            }
        }

        for (Map.Entry<String, Class<?>> entry : components.entrySet()) {
            if (type.isAssignableFrom(entry.getValue())) {
                if (!entry.getValue().isAnnotationPresent(Singleton.class)) {
                    return createInstance(entry.getValue());
                }
                return getBean(entry.getKey());
            }
        }

        try {
            Object instance = type.getDeclaredConstructor().newInstance();
            injectFields(instance); // Рекурсивно внедряем зависимости
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Cannot resolve dependency for type: " + type.getName(), e);
        }
    }

    private void injectFields(Object instance) {
        Class<?> clazz = instance.getClass();

        Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Inject.class))
                .forEach(field -> {
                    try {
                        field.setAccessible(true);
                        Object dependency = resolveDependency(field.getType());
                        field.set(instance, dependency);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to inject field: " + field.getName(), e);
                    }
                });
    }
}