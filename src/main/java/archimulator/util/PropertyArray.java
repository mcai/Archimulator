package archimulator.util;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Property array annotation.
 *
 * @author Min Cai
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface PropertyArray {
}
