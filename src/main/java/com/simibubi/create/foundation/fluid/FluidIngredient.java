package com.simibubi.create.foundation.fluid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecs;
import net.createmod.catnip.lang.Lang;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;

import net.minecraft.world.level.material.Fluids;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;

// If anymore fluid ingredient "types" are added then you must update FluidIngredient.Type
// TODO - See if we can use neoforge's classes for this
public abstract sealed class FluidIngredient implements Predicate<FluidStack> {
	public static final FluidIngredient EMPTY = new FluidStackIngredient();

	public static final Codec<FluidIngredient> CODEC = FluidIngredient.Type.CODEC.dispatch(FluidIngredient::getType, type -> type.codec);
	public static final StreamCodec<RegistryFriendlyByteBuf, FluidIngredient> STREAM_CODEC = Type.STREAM_CODEC.dispatch(FluidIngredient::getType, type -> type.streamCodec);

	public List<FluidStack> matchingFluidStacks;

	public static FluidIngredient fromTag(TagKey<Fluid> tag, int amount) {
		return new FluidTagIngredient(tag, amount);
	}

	public static FluidIngredient fromFluid(Fluid fluid, int amount) {
		FluidStackIngredient ingredient = new FluidStackIngredient(fluid, amount);
		ingredient.fixFlowing();
		return ingredient;
	}

	public static FluidIngredient fromFluidStack(FluidStack fluidStack) {
		FluidStackIngredient ingredient = new FluidStackIngredient(fluidStack.getFluid(), fluidStack.getAmount());
		ingredient.fixFlowing();
		if (!fluidStack.isComponentsPatchEmpty())
			ingredient.components = fluidStack.getComponentsPatch();
		return ingredient;
	}

	protected long amountRequired;

	public long getRequiredAmount() {
		return amountRequired;
	}

	protected abstract boolean testInternal(FluidStack t);

	protected abstract void readInternal(RegistryFriendlyByteBuf buffer);

	protected abstract void writeInternal(RegistryFriendlyByteBuf buffer);

	protected abstract List<FluidStack> determineMatchingFluidStacks();

	protected abstract Type getType();

	public List<FluidStack> getMatchingFluidStacks() {
		if (matchingFluidStacks != null)
			return matchingFluidStacks;
		return matchingFluidStacks = determineMatchingFluidStacks();
	}

	@Override
	public boolean test(FluidStack t) {
		if (t == null)
			throw new IllegalArgumentException("FluidStack cannot be null");
		return testInternal(t);
	}

	public static void write(RegistryFriendlyByteBuf buffer, FluidIngredient ingredient) {
		buffer.writeBoolean(ingredient instanceof FluidTagIngredient);
		buffer.writeVarLong(ingredient.amountRequired);
		ingredient.writeInternal(buffer);
	}

	public static FluidIngredient read(RegistryFriendlyByteBuf buffer) {
		boolean isTagIngredient = buffer.readBoolean();
		FluidIngredient ingredient = isTagIngredient ? new FluidTagIngredient() : new FluidStackIngredient();
		ingredient.amountRequired = buffer.readVarInt();
		ingredient.readInternal(buffer);
		return ingredient;
	}

	public static final class FluidStackIngredient extends FluidIngredient {
		private static final Codec<Fluid> FLUID_NON_AIR_CODEC = BuiltInRegistries.FLUID
			.byNameCodec()
			.validate(fluid -> fluid == Fluids.EMPTY ? DataResult.error(() -> "Fluid must not be minecraft:empty") : DataResult.success(fluid));

		public static final MapCodec<FluidStackIngredient> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
				FLUID_NON_AIR_CODEC.fieldOf("fluid").forGetter(fsi -> fsi.fluid),
				DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(fsi -> fsi.components),
				Codec.LONG.fieldOf("amount").forGetter(fti -> fti.amountRequired)
			).apply(i, FluidStackIngredient::new)
		);

		public static final StreamCodec<RegistryFriendlyByteBuf, FluidStackIngredient> STREAM_CODEC = StreamCodec.composite(
			CatnipStreamCodecs.FLUID, i -> i.fluid,
			DataComponentPatch.STREAM_CODEC, i -> i.components,
			ByteBufCodecs.VAR_LONG, i -> i.amountRequired,
			FluidStackIngredient::new
		);

		private Fluid fluid;
		private DataComponentPatch components;

		public FluidStackIngredient() {
			components = DataComponentPatch.EMPTY;
		}

		public FluidStackIngredient(Fluid fluid, DataComponentPatch tagToMatch, long amountRequired) {
			this.fluid = fluid;
			this.components = tagToMatch;
			this.amountRequired = amountRequired;
		}

		public FluidStackIngredient(Fluid fluid, long amountRequired) {
			this.fluid = fluid;
			this.components = DataComponentPatch.EMPTY;
			this.amountRequired = amountRequired;
		}

		void fixFlowing() {
			if (fluid instanceof FlowingFluid flowingFluid)
				fluid = flowingFluid.getSource();
		}

		@Override
		protected boolean testInternal(FluidStack t) {
			if (!FluidHelper.isSame(t, fluid))
				return false;
			if (components.isEmpty())
				return true;
			DataComponentPatch tag = t.getComponentsPatch();

			HashSet<Map.Entry<DataComponentType<?>, Optional<?>>> referenceSet = new HashSet<>(tag.entrySet());
			referenceSet.addAll(components.entrySet());
			return referenceSet.equals(tag.entrySet());
		}

		@Override
		protected void readInternal(RegistryFriendlyByteBuf buffer) {
			fluid = ByteBufCodecs.registry(Registries.FLUID).decode(buffer);
			components = DataComponentPatch.STREAM_CODEC.decode(buffer);
		}

		@Override
		protected void writeInternal(RegistryFriendlyByteBuf buffer) {
			ByteBufCodecs.registry(Registries.FLUID).encode(buffer, fluid);
			DataComponentPatch.STREAM_CODEC.encode(buffer, components);
		}

		@Override
		protected List<FluidStack> determineMatchingFluidStacks() {
			return ImmutableList.of(components.isEmpty() ? new FluidStack(fluid, amountRequired)
				: new FluidStack(BuiltInRegistries.FLUID.wrapAsHolder(fluid), amountRequired, components));
		}

		@Override
		protected Type getType() {
			return Type.FLUID_STACK;
		}
	}

	public static final class FluidTagIngredient extends FluidIngredient {
		public static final MapCodec<FluidTagIngredient> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			TagKey.codec(Registries.FLUID).fieldOf("fluid_tag").forGetter(fti -> fti.tag),
				Codec.LONG.fieldOf("amount").forGetter(fti -> fti.amountRequired)
			).apply(i, FluidTagIngredient::new)
		);

		public static final StreamCodec<RegistryFriendlyByteBuf, FluidTagIngredient> STREAM_CODEC = StreamCodec.composite(
			ResourceLocation.STREAM_CODEC, i -> i.tag.location(),
			ByteBufCodecs.VAR_LONG, i -> i.amountRequired,
			(tag, amount) -> new FluidTagIngredient(TagKey.create(Registries.FLUID, tag), amount)
		);

		private TagKey<Fluid> tag;

		public FluidTagIngredient() {}

		public FluidTagIngredient(TagKey<Fluid> tag, long amountRequired) {
			this.tag = tag;
			this.amountRequired = amountRequired;
		}

		@Override
		protected boolean testInternal(FluidStack t) {
			if (tag != null)
				return FluidHelper.isTag(t, tag);
			for (FluidStack accepted : getMatchingFluidStacks())
				if (FluidHelper.isSame(accepted, t))
					return true;
			return false;
		}

		@Override
		protected void readInternal(RegistryFriendlyByteBuf buffer) {
			matchingFluidStacks = FluidStack.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer);
		}

		@Override
		protected void writeInternal(RegistryFriendlyByteBuf buffer) {
			// Tag has to be resolved on the server before sending
			FluidStack.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, getMatchingFluidStacks());
		}

		@Override
		protected List<FluidStack> determineMatchingFluidStacks() {
			List<FluidStack> stacks = new ArrayList<>();
			for (Holder<Fluid> holder : BuiltInRegistries.FLUID.getTagOrEmpty(tag)) {
				Fluid f = holder.value();
				if (f instanceof FlowingFluid flowing) f = flowing.getSource();
				stacks.add(new FluidStack(f, amountRequired));
			}
			return stacks;
		}

		@Override
		protected Type getType() {
			return Type.FLUID_TAG;
		}
	}

	protected enum Type implements StringRepresentable {
		FLUID_STACK(FluidStackIngredient.CODEC, FluidStackIngredient.STREAM_CODEC),
		FLUID_TAG(FluidTagIngredient.CODEC, FluidTagIngredient.STREAM_CODEC);

		public static final Codec<Type> CODEC = StringRepresentable.fromValues(Type::values);
		public static final StreamCodec<RegistryFriendlyByteBuf, Type> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(Type.class);

		private final MapCodec<? extends FluidIngredient> codec;
		private final StreamCodec<RegistryFriendlyByteBuf, ? extends FluidIngredient> streamCodec;

		Type(MapCodec<? extends FluidIngredient> codec, StreamCodec<RegistryFriendlyByteBuf, ? extends FluidIngredient> streamCodec) {
			this.codec = codec;
			this.streamCodec = streamCodec;
		}

		@Override
		public @NotNull String getSerializedName() {
			return Lang.asId(name());
		}
	}
}
