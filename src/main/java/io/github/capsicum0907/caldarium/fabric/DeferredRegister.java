package io.github.capsicum0907.caldarium.fabric;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import io.github.capsicum0907.caldarium.Caldarium;

import net.minecraft.resources.ResourceKey;
//? if >=1.19.3
import net.minecraft.core.registries.BuiltInRegistries;

public final class DeferredRegister<T> {
    private final ResourceKey<? extends net.minecraft.core.Registry<T>> key;
    private final Map<String, Entry<? extends T>> entries = new LinkedHashMap<>();

    private DeferredRegister(ResourceKey<? extends net.minecraft.core.Registry<T>> key) {
        this.key = key;
    }

    public static <T> DeferredRegister<T> create(ResourceKey<? extends net.minecraft.core.Registry<T>> key, String namespace) {
        return new DeferredRegister<>(key);
    }

    public <I extends T> Supplier<I> register(String id, Supplier<I> factory) {
        Entry<I> entry = new Entry<>(factory);
        if (entries.putIfAbsent(id, entry) != null) {
            throw new IllegalStateException("Duplicate registration of " + id);
        }
        return entry;
    }

    @SuppressWarnings("unchecked")
    public void register() {
        //? if >=1.19.3 {
        net.minecraft.core.Registry<T> registry = (net.minecraft.core.Registry<T>) BuiltInRegistries.REGISTRY.get(key.location());
        //?} else
        //net.minecraft.core.Registry<T> registry = (net.minecraft.core.Registry<T>) net.minecraft.core.Registry.REGISTRY.get(key.location());
        if (registry == null) {
            throw new IllegalStateException("No registry " + key.location());
        }
        for (Map.Entry<String, Entry<? extends T>> entry : entries.entrySet()) {
            entry.getValue().register(registry, entry.getKey());
        }
    }

    private static final class Entry<I> implements Supplier<I> {
        private final Supplier<I> factory;
        private I value;

        private Entry(Supplier<I> factory) {
            this.factory = factory;
        }

        @SuppressWarnings("unchecked")
        private <T> void register(net.minecraft.core.Registry<T> registry, String id) {
            value = factory.get();
            net.minecraft.core.Registry.register(registry, Caldarium.id(id), (T) value);
        }

        @Override
        public I get() {
            if (value == null) {
                throw new IllegalStateException("Not registered yet");
            }
            return value;
        }
    }
}
