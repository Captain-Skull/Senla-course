package annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigProperty {
    String configFileName() default "application.properties";
    String propertyName() default "";
    PropertyType type() default PropertyType.AUTO;
}