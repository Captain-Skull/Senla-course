// di-module/src/main/java/di/ApplicationContext.java
package di;

import annotations.Component;
import annotations.Singleton;
import java.util.HashMap;
import java.util.Map;

@Component
@Singleton
public class ApplicationContext {
    private static ApplicationContext instance;
    private final Map<String, Object> beans = new HashMap<>();

    private ApplicationContext() {
        // Приватный конструктор
    }

    public static ApplicationContext getInstance() {
        if (instance == null) {
            instance = new ApplicationContext();
        }
        return instance;
    }

    public void registerBean(String name, Object bean) {
        beans.put(name, bean);
    }

    public void registerBean(Class<?> type, Object bean) {
        beans.put(type.getName(), bean);
    }

    public <T> T getBean(Class<T> type) {
        Object bean = beans.get(type.getName());
        if (bean != null && type.isInstance(bean)) {
            return type.cast(bean);
        }
        throw new RuntimeException("Bean not found: " + type.getName());
    }

    public Object getBean(String name) {
        return beans.get(name);
    }
}