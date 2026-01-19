package di;

import annotations.Component;
import annotations.Inject;
import annotations.PostConstruct;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

public class Injector {
    private static final Map<Class<?>, Object> container = new HashMap<>();
    private static final Set<Class<?>> inCreation = new HashSet<>();
    private static boolean initialized = false;

    public static <T> T getInstance(Class<T> type) {
        if (!container.containsKey(type)) {
            if (type.isAnnotationPresent(Component.class)) {
                registerComponent(type);
                return type.cast(container.get(type));
            }
            throw new RuntimeException("Component not registered: " + type.getSimpleName());
        }
        return type.cast(container.get(type));
    }

    public static void registerComponent(Class<?> type) {
        if (container.containsKey(type)) {
            return;
        }

        if (!type.isAnnotationPresent(Component.class)) {
            throw new IllegalArgumentException(
                    "Class " + type.getSimpleName() + " is not annotated with @Component"
            );
        }

        if (inCreation.contains(type)) {
            throw new RuntimeException("Circular dependency detected: " + type.getSimpleName());
        }

        inCreation.add(type);

        try {
            Object instance = type.getDeclaredConstructor().newInstance();
            container.put(type, instance);

            injectFields(instance);

            invokePostConstruct(instance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to register " + type.getSimpleName(), e);
        } finally {
            inCreation.remove(type);
        }
    }

    public static <T> void registerComponent(Class<T> type, T instance) {
        if (container.containsKey(type)) {
            throw new RuntimeException(type.getSimpleName() + " already registered");
        }

        try {
            container.put(type, instance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to register existing instance", e);
        }
    }

    public static void initialize() {
        if (initialized) return;

        for (Map.Entry<Class<?>, Object> entry : container.entrySet()) {
            try {
                invokePostConstruct(entry.getValue());
            } catch (Exception e) {
                System.err.println("⚠️ Failed @PostConstruct for " +
                        entry.getKey().getSimpleName() + ": " + e.getMessage());
            }
        }

        initialized = true;
    }

    public static void injectDependencies(Object target) {
        if (target == null) return;

        try {
            injectFields(target);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject dependencies into " +
                    target.getClass().getSimpleName(), e);
        }
    }

    private static void injectFields(Object target) throws Exception {
        Class<?> currentClass = target.getClass();

        // Проходим вверх по иерархии наследования
        while (currentClass != null && currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);

                    // Если поле уже инициализировано — не перезаписываем (опционально)
                    if (field.get(target) != null) continue;

                    Object dependency = resolveDependency(field.getType());
                    if (dependency != null) {
                        field.set(target, dependency);
                    } else {
                        throw new RuntimeException("Dependency not found for field: " +
                                field.getName() + " in " + currentClass.getSimpleName());
                    }
                }
            }
            currentClass = currentClass.getSuperclass(); // Переходим к родителю
        }
    }

    private static Object resolveDependency(Class<?> type) {
        if (container.containsKey(type)) {
            return container.get(type);
        }

        if (type.isAnnotationPresent(Component.class)) {
            registerComponent(type);
            return container.get(type);
        }

        try {
            Object instance = type.getDeclaredConstructor().newInstance();
            injectFields(instance); // Рекурсивно инжектим зависимости
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Cannot resolve dependency: " + type.getSimpleName(), e);
        }
    }

    private static void invokePostConstruct(Object target) throws Exception {
        if (target == null) return;

        Class<?> clazz = target.getClass();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(PostConstruct.class)) {
                method.setAccessible(true);

                if (method.getParameterCount() != 0) {
                    throw new RuntimeException(
                            "@PostConstruct method must have zero parameters: " +
                                    method.getName()
                    );
                }

                if (isPostConstructInvoked(target, method)) {
                    continue;
                }

                method.invoke(target);
                markPostConstructInvoked(target, method);
            }
        }
    }

    private static final Map<Object, Set<String>> postConstructInvoked = new HashMap<>();

    private static boolean isPostConstructInvoked(Object target, Method method) {
        Set<String> methods = postConstructInvoked.get(target);
        return methods != null && methods.contains(method.getName());
    }

    private static void markPostConstructInvoked(Object target, Method method) {
        postConstructInvoked.computeIfAbsent(target, k -> new HashSet<>())
                .add(method.getName());
    }
}