package com.simibubi.create.foundation.item;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.foundation.codec.CreateCodecs;

import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

/**
 * Utility class representing non-empty slots in an item inventory.
 */
public class ItemSlots {
	public static final Codec<ItemSlots> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.unboundedMap(CreateCodecs.boundedIntStr(0), ItemStack.CODEC).fieldOf("items").forGetter(ItemSlots::toBoxedMap),
		ExtraCodecs.NON_NEGATIVE_INT.fieldOf("size").forGetter(ItemSlots::getSize)
	).apply(instance, ItemSlots::deserialize));

	public static final StreamCodec<RegistryFriendlyByteBuf, ItemSlots> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.map(HashMap::new, ByteBufCodecs.INT, ItemStack.STREAM_CODEC), ItemSlots::toBoxedMap,
		ByteBufCodecs.INT, ItemSlots::getSize,
		ItemSlots::deserialize
	);

	private final Int2ObjectMap<ItemStack> map;
	private int size;

	public ItemSlots() {
		this.map = new Int2ObjectOpenHashMap<>();
		this.size = 0;
	}

	public void set(int slot, ItemStack stack) {
		if (slot < 0) {
			throw new IllegalArgumentException("Slot must be positive");
		} else if (!stack.isEmpty()) {
			this.map.put(slot, stack);
			this.size = Math.max(this.size, slot + 1);
		}
	}

	public int getSize() {
		return this.size;
	}

	public void setSize(int size) {
		if (size <= this.getHighestSlot()) {
			throw new IllegalStateException("cannot set size to below the highest slot");
		}
		this.size = size;
	}

	public void forEach(SlotConsumer consumer) {
		for (Int2ObjectMap.Entry<ItemStack> entry : this.map.int2ObjectEntrySet()) {
			consumer.accept(entry.getIntKey(), entry.getValue());
		}
	}

	private int getHighestSlot() {
		return this.map.keySet().intStream().max().orElse(-1);
	}

	public <T extends SlottedStackStorage> T toHandler(IntFunction<T> factory) {
		T handler = factory.apply(this.size);
		this.forEach(handler::setStackInSlot);
		return handler;
	}

	public static ItemSlots fromHandler(SlottedStackStorage handler) {
		ItemSlots slots = new ItemSlots();
		slots.setSize(handler.getSlotCount());
		for (int i = 0; i < handler.getSlotCount(); i++) {
			ItemStack stack = handler.getStackInSlot(i);
			if (!stack.isEmpty()) {
				slots.set(i, stack.copy());
			}
		}
		return slots;
	}

	public Map<Integer, ItemStack> toBoxedMap() {
		Map<Integer, ItemStack> map = new HashMap<>();
		this.forEach(map::put);
		return map;
	}

	public static ItemSlots fromBoxedMap(Map<Integer, ItemStack> map) {
		ItemSlots slots = new ItemSlots();
		map.forEach(slots::set);
		return slots;
	}

	public static Codec<ItemSlots> maxSizeCodec(int maxSize) {
		return CODEC.validate(slots -> slots.size <= maxSize
			? DataResult.success(slots)
			: DataResult.error(() -> "Slots above maximum of " + maxSize));
	}

	private static ItemSlots deserialize(Map<Integer, ItemStack> map, int size) {
		ItemSlots slots = fromBoxedMap(map);
		slots.setSize(size);
		return slots;
	}

	@FunctionalInterface
	public interface SlotConsumer {
		void accept(int slot, ItemStack stack);
	}
}
