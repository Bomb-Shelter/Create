package com.simibubi.create;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.simibubi.create.infrastructure.fabric.client.CustomRenderHandlerFluidType;
import com.simibubi.create.infrastructure.fabric.client.CreateFluidRenderHandler;

import io.github.fabricators_of_create.porting_lib.event.client.FogEvents;
import io.github.fabricators_of_create.porting_lib.fluids.BaseFlowingFluid;

import io.github.fabricators_of_create.porting_lib.fluids.FluidInteractionRegistry;

import io.github.fabricators_of_create.porting_lib.fluids.FluidInteractionRegistry.InteractionInformation;

import io.github.fabricators_of_create.porting_lib.fluids.FluidType;
import io.github.fabricators_of_create.porting_lib.fluids.PortingLibFluids;

import io.github.fabricators_of_create.porting_lib.tags.Tags;

import net.createmod.catnip.platform.CatnipServices;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;

import org.joml.Vector3f;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.content.decoration.palettes.AllPaletteStoneTypes;
import com.simibubi.create.content.fluids.VirtualFluid;
import com.simibubi.create.content.fluids.potion.PotionFluid;
import com.simibubi.create.content.fluids.potion.PotionFluid.PotionFluidType;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.tterrag.registrate.builders.FluidBuilder.FluidTypeFactory;
import com.tterrag.registrate.util.entry.FluidEntry;

import net.createmod.catnip.theme.Color;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer.FogMode;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;

public class AllFluids {
	private static final CreateRegistrate REGISTRATE = Create.registrate();

	static {
		REGISTRATE.setCreativeTab(AllCreativeModeTabs.BASE_CREATIVE_TAB);
	}

	public static final FluidEntry<PotionFluid> POTION =
		REGISTRATE.virtualFluid("potion", PotionFluidType::new, PotionFluid::createSource, PotionFluid::createFlowing)
			.lang("Potion")
			.register();

	public static final FluidEntry<VirtualFluid> TEA = REGISTRATE.virtualFluid("tea")
		.lang("Builder's Tea")
		.tag(AllTags.commonFluidTag("teas"))
		.register();

	public static final FluidEntry<BaseFlowingFluid.Flowing> HONEY =
		REGISTRATE.standardFluid("honey",
				SolidRenderedPlaceableFluidType.create(0xEAAE2F,
					() -> 1f / 8f * AllConfigs.client().honeyTransparencyMultiplier.getF()))
			.lang("Honey")
			.properties(b -> b.viscosity(2000)
				.density(1400))
			.fluidProperties(p -> p.levelDecreasePerBlock(2)
				.tickRate(25)
				.slopeFindDistance(3)
				.explosionResistance(100f))
			.tag(Tags.Fluids.HONEY, FluidTags.WATER) // fabric: water tag controls physics
			.source(BaseFlowingFluid.Source::new) // TODO: remove when Registrate fixes FluidBuilder
			.block()
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_YELLOW))
			.build()
			.bucket()
			.tag(AllTags.commonItemTag("buckets/honey"))
			.build()
			.register();

	public static final FluidEntry<BaseFlowingFluid.Flowing> CHOCOLATE =
		REGISTRATE.standardFluid("chocolate",
				SolidRenderedPlaceableFluidType.create(0x622020,
					() -> 1f / 32f * AllConfigs.client().chocolateTransparencyMultiplier.getF()))
			.lang("Chocolate")
			.tag(AllTags.commonFluidTag("chocolates"), FluidTags.WATER) // fabric: water tag controls physics
			.properties(b -> b.viscosity(1500)
				.density(1400))
			.fluidProperties(p -> p.levelDecreasePerBlock(2)
				.tickRate(25)
				.slopeFindDistance(3)
				.explosionResistance(100f))
			.block()
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN))
			.build()
			.register();

	// Load this class

	public static void register() {
	}

	private static void registerClient() {
		CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> {
			ClientFluidEvents.registerFluidRenderHandler(POTION);
			ClientFluidEvents.registerFluidRenderHandler(TEA);
			ClientFluidEvents.registerFluidRenderHandler(HONEY);
			ClientFluidEvents.registerFluidRenderHandler(CHOCOLATE);
		});
	}

	@Environment(EnvType.CLIENT)
	private static class ClientFluidEvents {
		public static <T extends BaseFlowingFluid> void registerFluidRenderHandler(FluidEntry<T> fluid) {
			if (fluid.getType() instanceof CustomRenderHandlerFluidType fluidType) {
				FluidRenderHandlerRegistry.INSTANCE.register(fluid.getSource(), fluid.get(), fluidType.getRenderHandler());
			}

			if (fluid.getType() instanceof TintedFluidType tintedFluidType) {
				FogEvents.SET_COLOR.register((data, partialTicks) -> {
					Level level = data.getCamera().getEntity().level();
					FluidState fluidState = level.getFluidState(data.getCamera().getBlockPosition());
					if (fluidState.is((Fluid) fluid.getSource()) || fluidState.is(fluid.get())) {
						Vector3f modified = tintedFluidType.modifyFogColor(data.getCamera(), partialTicks, (ClientLevel) level, Minecraft.getInstance().options.getEffectiveRenderDistance(), Minecraft.getInstance().gameRenderer.getDarkenWorldAmount(partialTicks), new Vector3f(data.getRed(), data.getGreen(), data.getBlue()));
						data.setRed(modified.x);
						data.setGreen(modified.y);
						data.setBlue(modified.z);
					}
				});

				FogEvents.RENDER_FOG.register((mode, type, camera, partialTick, renderDistance, nearDistance, farDistance, shape, fogData) -> {
					Level level = camera.getEntity().level();
					FluidState fluidState = level.getFluidState(camera.getBlockPosition());
					if (fluidState.is((Fluid) fluid.getSource()) || fluidState.is(fluid.get())) {
						tintedFluidType.modifyFogRender(camera, mode, renderDistance, partialTick, nearDistance, farDistance, shape);
					}

					return false;
				});
			}
		}
	}

	public static void registerFluidInteractions() {
		FluidInteractionRegistry.addInteraction(PortingLibFluids.LAVA_TYPE, new InteractionInformation(
			HONEY.get().getFluidType(),
			fluidState -> {
				if (fluidState.isSource()) {
					return Blocks.OBSIDIAN.defaultBlockState();
				} else {
					return AllPaletteStoneTypes.LIMESTONE.getBaseBlock()
						.get()
						.defaultBlockState();
				}
			}
		));

		FluidInteractionRegistry.addInteraction(PortingLibFluids.LAVA_TYPE, new InteractionInformation(
			CHOCOLATE.get().getFluidType(),
			fluidState -> {
				if (fluidState.isSource()) {
					return Blocks.OBSIDIAN.defaultBlockState();
				} else {
					return AllPaletteStoneTypes.SCORIA.getBaseBlock()
						.get()
						.defaultBlockState();
				}
			}
		));

		registerClient();
	}

	@Nullable
	public static BlockState getLavaInteraction(FluidState fluidState) {
		Fluid fluid = fluidState.getType();
		if (fluid.isSame(HONEY.get()))
			return AllPaletteStoneTypes.LIMESTONE.getBaseBlock()
				.get()
				.defaultBlockState();
		if (fluid.isSame(CHOCOLATE.get()))
			return AllPaletteStoneTypes.SCORIA.getBaseBlock()
				.get()
				.defaultBlockState();
		return null;
	}

	public static abstract class TintedFluidType extends FluidType implements CustomRenderHandlerFluidType {

		protected static final int NO_TINT = 0xffffffff;
		private ResourceLocation stillTexture;
		private ResourceLocation flowingTexture;

		public TintedFluidType(Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
			super(properties);
			this.stillTexture = stillTexture;
			this.flowingTexture = flowingTexture;
		}

		@Environment(EnvType.CLIENT)
		public FluidRenderHandler getRenderHandler() {
			return new CreateFluidRenderHandler() {
				@Override
				public ResourceLocation getStillTexture() {
					return stillTexture;
				}

				@Override
				public ResourceLocation getFlowingTexture() {
					return flowingTexture;
				}

				@Override
				public int getTintColor(FluidState state, BlockAndTintGetter level, BlockPos pos) {
					return TintedFluidType.this.getTintColor(state, level, pos);
				}
			};
		}

		@Environment(EnvType.CLIENT)
		public Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level,
									   int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
			Vector3f customFogColor = TintedFluidType.this.getCustomFogColor();
			return customFogColor == null ? fluidFogColor : customFogColor;
		}

		@Environment(EnvType.CLIENT)
		public void modifyFogRender(Camera camera, FogMode mode, float renderDistance, float partialTick,
									float nearDistance, float farDistance, FogShape shape) {
			float modifier = TintedFluidType.this.getFogDistanceModifier();
			float baseWaterFog = 96.0f;
			if (modifier != 1f) {
				RenderSystem.setShaderFogShape(FogShape.CYLINDER);
				RenderSystem.setShaderFogStart(-8);
				RenderSystem.setShaderFogEnd(baseWaterFog * modifier);
			}
		}

		protected abstract int getTintColor(FluidStack stack);

		protected abstract int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos);

		protected Vector3f getCustomFogColor() {
			return null;
		}

		protected float getFogDistanceModifier() {
			return 1f;
		}

	}

	private static class SolidRenderedPlaceableFluidType extends TintedFluidType {

		private Vector3f fogColor;
		private Supplier<Float> fogDistance;

		public static FluidTypeFactory create(int fogColor, Supplier<Float> fogDistance) {
			return (p, s, f) -> {
				SolidRenderedPlaceableFluidType fluidType = new SolidRenderedPlaceableFluidType(p, s, f);
				fluidType.fogColor = new Color(fogColor, false).asVectorF();
				fluidType.fogDistance = fogDistance;
				return fluidType;
			};
		}

		private SolidRenderedPlaceableFluidType(Properties properties, ResourceLocation stillTexture,
												ResourceLocation flowingTexture) {
			super(properties, stillTexture, flowingTexture);
		}

		@Override
		protected int getTintColor(FluidStack stack) {
			return NO_TINT;
		}

		/*
		 * Removing alpha from tint prevents optifine from forcibly applying biome
		 * colors to modded fluids (this workaround only works for fluids in the solid
		 * render layer)
		 */
		@Override
		public int getTintColor(FluidState state, BlockAndTintGetter world, BlockPos pos) {
			return 0x00ffffff;
		}

		@Override
		protected Vector3f getCustomFogColor() {
			return fogColor;
		}

		@Override
		protected float getFogDistanceModifier() {
			return fogDistance.get();
		}

	}

}
