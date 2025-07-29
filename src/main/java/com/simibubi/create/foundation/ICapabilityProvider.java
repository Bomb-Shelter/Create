package com.simibubi.create.foundation;

import java.util.function.Supplier;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;

import net.minecraft.core.Direction;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface ICapabilityProvider<T> {
	@Nullable
	T getCapability();

	static <T, C> ICapabilityProvider<T> of(BlockApiCache<T, C> cache, C context) {
		return new BlockCapabilityCacheProvider<>(cache, context);
	}

	static <T> ICapabilityProvider<T> of(Supplier<T> supplier) {
		return new SupplierProvider<>(supplier);
	}

	static <T> ICapabilityProvider<T> of(T cap) {
		return new SimpleProvider<>(cap);
	}

	@ApiStatus.Internal
	class BlockCapabilityCacheProvider<T, C> implements ICapabilityProvider<T> {
		private final BlockApiCache<T, C> inner;
		private final C context;

		private BlockCapabilityCacheProvider(BlockApiCache<T, C> inner, C context) {
			this.inner = inner;
			this.context = context;
		}

		@Override
		public @Nullable T getCapability() {
			return inner == null ? null : inner.find(context);
		}
	}

	class SupplierProvider<T> implements ICapabilityProvider<T> {
		private final Supplier<T> inner;

		private SupplierProvider(Supplier<T> inner) {
			this.inner = inner;
		}

		@Override
		public @Nullable T getCapability() {
			return inner == null ? null : inner.get();
		}
	}

	@ApiStatus.Internal
	class SimpleProvider<T> implements ICapabilityProvider<T> {
		private final T inner;

		private SimpleProvider(T inner) {
			this.inner = inner;
		}

		@Override
		public @Nullable T getCapability() {
			return inner;
		}
	}
}
