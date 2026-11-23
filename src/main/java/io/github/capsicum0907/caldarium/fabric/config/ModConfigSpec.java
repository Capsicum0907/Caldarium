package io.github.capsicum0907.caldarium.fabric.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class ModConfigSpec {
    private final List<ConfigValue<?>> values;
    private boolean loaded;

    private ModConfigSpec(List<ConfigValue<?>> values) {
        this.values = values;
    }

    public void load(Path file) {
        Map<String, Object> read = new LinkedHashMap<>();
        try {
            if (Files.exists(file)) {
                read = Toml.read(new String(Files.readAllBytes(file), StandardCharsets.UTF_8));
            }
            for (ConfigValue<?> value : values) {
                value.read(read.get(value.key()));
            }
            Files.createDirectories(file.getParent());
            Files.write(file, Toml.write(values).getBytes(StandardCharsets.UTF_8));
            loaded = true;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public boolean isLoaded() {
        return loaded;
    }

    public static final class Builder {
        private final Deque<String> path = new ArrayDeque<>();
        private final List<ConfigValue<?>> values = new ArrayList<>();

        public Builder push(String name) {
            path.addLast(name);
            return this;
        }

        public Builder pop() {
            path.removeLast();
            return this;
        }

        public IntValue defineInRange(String name, int fallback, int min, int max) {
            return add(new IntValue(at(name), fallback, min, max));
        }

        public DoubleValue defineInRange(String name, double fallback, double min, double max) {
            return add(new DoubleValue(at(name), fallback, min, max));
        }

        public <T> ConfigValue<List<? extends T>> defineList(String name, List<? extends T> fallback,
                Supplier<T> newElement, Predicate<Object> valid) {
            return add(new ListValue<>(at(name), fallback, valid));
        }

        public ModConfigSpec build() {
            return new ModConfigSpec(List.copyOf(values));
        }

        private List<String> at(String name) {
            List<String> full = new ArrayList<>(path);
            full.add(name);
            return full;
        }

        private <V extends ConfigValue<?>> V add(V value) {
            values.add(value);
            return value;
        }
    }

    public abstract static class ConfigValue<T> {
        private final List<String> path;
        private final T fallback;
        private T value;

        ConfigValue(List<String> path, T fallback) {
            this.path = path;
            this.fallback = fallback;
            this.value = fallback;
        }

        public T get() {
            return value;
        }

        public void set(T value) {
            this.value = value;
        }

        List<String> path() {
            return path;
        }

        String key() {
            return String.join(".", path);
        }

        void read(Object raw) {
            T parsed = raw == null ? null : parse(raw);
            value = parsed == null ? fallback : parsed;
        }

        abstract T parse(Object raw);
    }

    public static final class IntValue extends ConfigValue<Integer> {
        private final int min;
        private final int max;

        IntValue(List<String> path, int fallback, int min, int max) {
            super(path, fallback);
            this.min = min;
            this.max = max;
        }

        @Override
        Integer parse(Object raw) {
            if (!(raw instanceof Long number) || number < min || number > max) {
                return null;
            }
            return number.intValue();
        }
    }

    public static final class DoubleValue extends ConfigValue<Double> {
        private final double min;
        private final double max;

        DoubleValue(List<String> path, double fallback, double min, double max) {
            super(path, fallback);
            this.min = min;
            this.max = max;
        }

        @Override
        Double parse(Object raw) {
            double number;
            if (raw instanceof Double real) {
                number = real;
            } else if (raw instanceof Long whole) {
                number = whole;
            } else {
                return null;
            }
            return number < min || number > max ? null : number;
        }
    }

    static final class ListValue<T> extends ConfigValue<List<? extends T>> {
        private final Predicate<Object> valid;

        ListValue(List<String> path, List<? extends T> fallback, Predicate<Object> valid) {
            super(path, fallback);
            this.valid = valid;
        }

        @Override
        @SuppressWarnings("unchecked")
        List<? extends T> parse(Object raw) {
            if (!(raw instanceof List<?> list)) {
                return null;
            }
            List<Object> out = new ArrayList<>();
            for (Object element : list) {
                Object plain = element instanceof Long whole && whole >= Integer.MIN_VALUE && whole <= Integer.MAX_VALUE
                        ? (Object) whole.intValue() : element;
                if (!valid.test(plain)) {
                    return null;
                }
                out.add(plain);
            }
            return (List<? extends T>) out;
        }
    }
}
