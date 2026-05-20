package io.github.hawah.shakenstir.foundation.datapack;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class RegistryHolder<T>{
    protected T warpedConent;
    protected DeferredHolder<T, T> deferredHolder;

    
    public T value() {
        return deferredHolder.value();
    }

    
    public T get() {
        return deferredHolder.get();
    }

    
    public Optional<T> asOptional() {
        return deferredHolder.asOptional();
    }


    
    public Identifier getId() {
        return deferredHolder.getId();
    }

    
    public ResourceKey<T> getKey() {
        return deferredHolder.getKey();
    }

    
    public boolean equals(Object obj) {
        return deferredHolder.equals(obj);
    }

    
    public int hashCode() {
        return deferredHolder.hashCode();
    }

    
    public String toString() {
        return deferredHolder.toString();
    }

    
    public boolean isBound() {
        return deferredHolder.isBound();
    }

    
    public boolean areComponentsBound() {
        return deferredHolder.areComponentsBound();
    }

    
    public DataComponentMap components() {
        return deferredHolder.components();
    }

    
    public boolean is(Identifier id) {
        return deferredHolder.is(id);
    }

    
    public boolean is(ResourceKey<T> key) {
        return deferredHolder.is(key);
    }

    
    public boolean is(Predicate<ResourceKey<T>> filter) {
        return deferredHolder.is(filter);
    }

    
    public boolean is(TagKey<T> tag) {
        return deferredHolder.is(tag);
    }

    
    public boolean is(Holder<T> holder) {
        return deferredHolder.is(holder);
    }

    
    public @Nullable <Z> Z getData(DataMapType<T, Z> type) {
        return deferredHolder.getData(type);
    }

    
    public Stream<TagKey<T>> tags() {
        return deferredHolder.tags();
    }

    
    public Either<ResourceKey<T>, T> unwrap() {
        return deferredHolder.unwrap();
    }

    
    public Optional<ResourceKey<T>> unwrapKey() {
        return deferredHolder.unwrapKey();
    }

    
    public Holder.Kind kind() {
        return deferredHolder.kind();
    }

    
    public boolean canSerializeIn(HolderOwner<T> owner) {
        return deferredHolder.canSerializeIn(owner);
    }

    
    public Holder<T> getDelegate() {
        return deferredHolder.getDelegate();
    }

    
    public String getRegisteredName() {
        return deferredHolder.getRegisteredName();
    }

    
    public HolderLookup.@Nullable RegistryLookup<T> unwrapLookup() {
        return deferredHolder.unwrapLookup();
    }

    public RegistryHolder(DeferredHolder<T, T> deferredHolder) {
        this.deferredHolder = deferredHolder;
    }

    public void warp(T t) {
        warpedConent = t;
    }

    public void warp(Supplier<T> t) {
        warpedConent = t.get();
    }

    public T getBeforeRegister() {
        return warpedConent;
    }
}
