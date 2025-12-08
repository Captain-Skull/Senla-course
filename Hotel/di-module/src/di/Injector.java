package di;

import annotations.Inject;

import java.lang.reflect.Field;

public class Injector {
    private static final DependencyContainer container = new DependencyContainer();

    static {
        // Авторегистрация базовых компонентов
        container.registerComponent(ApplicationContext.class);
    }

    public static <T> T getInstance(Class<T> type) {
        return container.getBean(type);
    }

    public static Object getInstance(String beanName) {
        return container.getBean(beanName);
    }

    public static void injectDependencies(Object target, ApplicationContext context) {
        Class<?> clazz = target.getClass();

        // Внедряем зависимости в поля
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                try {
                    Object dependency = context.getBean(field.getType());
                    if (dependency != null) {
                        field.set(target, dependency);
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to inject dependency", e);
                }
            }
        }
    }

    public static void registerComponent(Class<?> componentClass) {
        container.registerComponent(componentClass);
    }

    public static void initialize() {
        // Можно добавить инициализацию контейнера
    }
}