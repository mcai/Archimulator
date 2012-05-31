package archimulator.util;

public interface ValueProvider<T> {
    T get();

    T getInitialValue();
}
