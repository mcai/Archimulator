package archimulator.util;

public interface ValueProviderFactory<T, ValueProviderT extends ValueProvider<T>> {
    ValueProviderT createValueProvider(Object... args);
}
