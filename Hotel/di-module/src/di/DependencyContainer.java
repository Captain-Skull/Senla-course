package di;

import annotations.Component;
import annotations.Inject;
import annotations.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DependencyContainer {
    private final Map<String, Object> singletons = new ConcurrentHashMap<>();
    private final Map<String, Class<?>> components = new ConcurrentHashMap<>();
    private final Map<Class<?>, Object> typeToInstance = new ConcurrentHashMap<>();

    public void registerComponent(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " is not a @Component");
        }

        Component annotation = clazz.getAnnotation(Component.class);
        String beanName = annotation.name().isEmpty() ?
                clazz.getSimpleName().toLowerCase() : annotation.name();

        components.put(beanName, clazz);

        // Если это синглтон, создаем экземпляр сразу
        if (clazz.isAnnotationPresent(Singleton.class)) {
            Object instance = createInstance(clazz);
            singletons.put(beanName, instance);
            typeToInstance.put(clazz, instance);
        }
    }

    public Object getBean(String name) {
        // Проверяем синглтоны
        if (singletons.containsKey(name)) {
            return singletons.get(name);
        }

        // Создаем новый экземпляр для не-синглтонов
        Class<?> clazz = components.get(name);
        if (clazz == null) {
            throw new IllegalArgumentException("No bean found with name: " + name);
        }

        return createInstance(clazz);
    }

    public <T> T getBean(Class<T> type) {
        // Ищем по типу
        if (typeToInstance.containsKey(type)) {
            return type.cast(typeToInstance.get(type));
        }

        // Ищем по имени (по умолчанию)
        String beanName = type.getSimpleName().toLowerCase();
        return type.cast(getBean(beanName));
    }

    private Object createInstance(Class<?> clazz) {
        try {
            // Находим конструктор с @Inject или публичный конструктор по умолчанию
            var constructors = clazz.getConstructors();
            var constructor = Arrays.stream(constructors)
                    .filter(c -> c.isAnnotationPresent(Inject.class))
                    .findFirst()
                    .orElse(clazz.getDeclaredConstructor());

            // Собираем параметры для конструктора
            Object[] params = Arrays.stream(constructor.getParameterTypes())
                    .map(this::resolveDependency)
                    .toArray();

            // Создаем экземпляр
            Object instance = constructor.newInstance(params);

            // Внедряем зависимости в поля
            injectFields(instance);

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + clazz.getName(), e);
        }
    }

    private Object resolveDependency(Class<?> type) {
        // Пытаемся найти по типу
        if (typeToInstance.containsKey(type)) {
            return typeToInstance.get(type);
        }

        // Ищем компонент этого типа
        for (Class<?> component : components.values()) {
            if (type.isAssignableFrom(component)) {
                return getBean(component.getSimpleName().toLowerCase());
            }
        }

        throw new RuntimeException("Cannot resolve dependency for type: " + type.getName());
    }

    private void injectFields(Object instance) {
        Class<?> clazz = instance.getClass();

        // Внедряем в поля с @Inject
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

    public void scanPackage(String packageName) {
        try {
            // Простой сканер пакетов (для учебного проекта)
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = packageName.replace('.', '/');
            var resources = classLoader.getResources(path);

            while (resources.hasMoreElements()) {
                var resource = resources.nextElement();
                // В реальном проекте нужен полноценный сканер классов
                // Здесь упрощенная версия
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to scan package: " + packageName, e);
        }
    }
}