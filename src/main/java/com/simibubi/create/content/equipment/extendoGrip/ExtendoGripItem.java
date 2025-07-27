package com.simibubi.create.content.equipment.extendoGrip;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.content.equipment.armor.BacktankUtil;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import com.simibubi.create.infrastructure.config.AllConfigs;

import io.github.fabricators_of_create.porting_lib.core.event.entity.player.PlayerEvent;
import io.github.fabricators_of_create.porting_lib.entity.events.living.LivingDamageEvent;
import io.github.fabricators_of_create.porting_lib.entity.events.living.LivingKnockBackEvent;
import io.github.fabricators_of_create.porting_lib.entity.events.player.AttackEntityEvent;
import io.github.fabricators_of_create.porting_lib.entity.events.player.PlayerEvents.PlayerLoggedInEvent;
import io.github.fabricators_of_create.porting_lib.entity.events.player.PlayerInteractEvent;
import io.github.fabricators_of_create.porting_lib.entity.events.player.PlayerInteractEvent.EntityInteract;
import io.github.fabricators_of_create.porting_lib.entity.events.player.PlayerInteractEvent.EntityInteractSpecific;
import io.github.fabricators_of_create.porting_lib.entity.events.tick.EntityTickEvent;
import io.github.fabricators_of_create.porting_lib.entity.events.tick.EntityTickEvent.Pre;
import io.github.fabricators_of_create.porting_lib.event.client.InteractEvents;
import io.github.fabricators_of_create.porting_lib.item.extensions.SneakBypassUseItem;
import io.github.fabricators_of_create.porting_lib.level.events.BlockEvent.BreakEvent;
import io.github.fabricators_of_create.porting_lib.level.events.BlockEvent.EntityPlaceEvent;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.platform.CatnipServices;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class ExtendoGripItem extends Item implements SneakBypassUseItem {
	public static final int MAX_DAMAGE = 200;

	public static final AttributeModifier singleRangeAttributeModifier =
		new AttributeModifier(Create.asResource("single_range_attribute_modifier"), 3,
			AttributeModifier.Operation.ADD_VALUE);
	public static final AttributeModifier doubleRangeAttributeModifier =
		new AttributeModifier(Create.asResource("double_range_attribute_modifier"), 5,
			AttributeModifier.Operation.ADD_VALUE);

	private static final Supplier<Multimap<Holder<Attribute>, AttributeModifier>> rangeModifier = Suppliers.memoize(() ->
		// Holding an ExtendoGrip
		ImmutableMultimap.of(Attributes.BLOCK_INTERACTION_RANGE, singleRangeAttributeModifier));
	private static final Supplier<Multimap<Holder<Attribute>, AttributeModifier>> doubleRangeModifier = Suppliers.memoize(() ->
		// Holding two ExtendoGrips o.O
		ImmutableMultimap.of(Attributes.BLOCK_INTERACTION_RANGE, doubleRangeAttributeModifier));

	private static DamageSource lastActiveDamageSource;

	public ExtendoGripItem(Properties properties) {
		super(properties.durability(MAX_DAMAGE));
		CatnipServices.PLATFORM.executeOnClientOnly(() -> this::initializeClient);
	}

	public static final String EXTENDO_MARKER = "createExtendo";
	public static final String DUAL_EXTENDO_MARKER = "createDualExtendo";

	static {
		Pre.EVENT.register(ExtendoGripItem::holdingExtendoGripIncreasesRange);
	}

	public static void holdingExtendoGripIncreasesRange(EntityTickEvent.Pre event) {
		if (!(event.getEntity() instanceof Player player))
			return;

		CompoundTag persistentData = player.getCustomData();
		boolean inOff = AllItems.EXTENDO_GRIP.isIn(player.getOffhandItem());
		boolean inMain = AllItems.EXTENDO_GRIP.isIn(player.getMainHandItem());
		boolean holdingDualExtendo = inOff && inMain;
		boolean holdingExtendo = inOff ^ inMain;
		holdingExtendo &= !holdingDualExtendo;
		boolean wasHoldingExtendo = persistentData.contains(EXTENDO_MARKER);
		boolean wasHoldingDualExtendo = persistentData.contains(DUAL_EXTENDO_MARKER);

		if (holdingExtendo != wasHoldingExtendo) {
			if (!holdingExtendo) {
				player.getAttributes()
					.removeAttributeModifiers(rangeModifier.get());
				persistentData.remove(EXTENDO_MARKER);
			} else {
				AllAdvancements.EXTENDO_GRIP.awardTo(player);
				player.getAttributes()
					.addTransientAttributeModifiers(rangeModifier.get());
				persistentData.putBoolean(EXTENDO_MARKER, true);
			}
		}

		if (holdingDualExtendo != wasHoldingDualExtendo) {
			if (!holdingDualExtendo) {
				player.getAttributes()
					.removeAttributeModifiers(doubleRangeModifier.get());
				persistentData.remove(DUAL_EXTENDO_MARKER);
			} else {
				AllAdvancements.EXTENDO_GRIP_DUAL.awardTo(player);
				player.getAttributes()
					.addTransientAttributeModifiers(doubleRangeModifier.get());
				persistentData.putBoolean(DUAL_EXTENDO_MARKER, true);
			}
		}

	}

	static {
		PlayerLoggedInEvent.EVENT.register(ExtendoGripItem::addReachToJoiningPlayersHoldingExtendo);

		CatnipServices.PLATFORM.executeOnClientOnly(() -> ExtendoGripItem::registerClientEvents);
	}

	public static void addReachToJoiningPlayersHoldingExtendo(PlayerLoggedInEvent event) {
		Player player = event.getEntity();
		CompoundTag persistentData = player.getCustomData();

		if (persistentData.contains(DUAL_EXTENDO_MARKER))
			player.getAttributes()
				.addTransientAttributeModifiers(doubleRangeModifier.get());
		else if (persistentData.contains(EXTENDO_MARKER))
			player.getAttributes()
				.addTransientAttributeModifiers(rangeModifier.get());
	}

	@Environment(EnvType.CLIENT)
	private static void registerClientEvents() {
		InteractEvents.USE.register((mc, hit, hand) -> {
			dontMissEntitiesWhenYouHaveHighReachDistance();
			return InteractionResult.PASS;
		});

		AttackEntityEvent.EVENT.register(ExtendoGripItem::notifyServerOfLongRangeAttacks);
		EntityInteract.EVENT.register(ExtendoGripItem::notifyServerOfLongRangeInteractions);
		EntityInteractSpecific.EVENT.register(ExtendoGripItem::notifyServerOfLongRangeSpecificInteractions);
	}

	@Environment(EnvType.CLIENT)
	public static void dontMissEntitiesWhenYouHaveHighReachDistance() {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (mc.level == null || player == null)
			return;
		if (!isHoldingExtendoGrip(player))
			return;
		if (mc.hitResult instanceof BlockHitResult && mc.hitResult.getType() != Type.MISS)
			return;

		// Modified version of GameRenderer#getMouseOver
		double d0 = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
		if (!player.isCreative())
			d0 -= 0.5f;
		Vec3 Vector3d = player.getEyePosition(AnimationTickHolder.getPartialTicks());
		Vec3 Vector3d1 = player.getViewVector(1.0F);
		Vec3 Vector3d2 = Vector3d.add(Vector3d1.x * d0, Vector3d1.y * d0, Vector3d1.z * d0);
		AABB AABB = player.getBoundingBox()
			.expandTowards(Vector3d1.scale(d0))
			.inflate(1.0D, 1.0D, 1.0D);
		EntityHitResult entityraytraceresult =
			ProjectileUtil.getEntityHitResult(player, Vector3d, Vector3d2, AABB, (e) -> {
				return !e.isSpectator() && e.isPickable();
			}, d0 * d0);
		if (entityraytraceresult != null) {
			Entity entity1 = entityraytraceresult.getEntity();
			Vec3 Vector3d3 = entityraytraceresult.getLocation();
			double d2 = Vector3d.distanceToSqr(Vector3d3);
			if (d2 < d0 * d0 || mc.hitResult == null || mc.hitResult.getType() == Type.MISS) {
				mc.hitResult = entityraytraceresult;
				if (entity1 instanceof LivingEntity || entity1 instanceof ItemFrame)
					mc.crosshairPickEntity = entity1;
			}
		}
	}

	static {
		BreakEvent.EVENT.register(event -> {
			consumeDurabilityOnBlockBreak((BreakEvent) event);
		});

		EntityPlaceEvent.EVENT.register(ExtendoGripItem::consumeDurabilityOnPlace);
	}

	//@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void consumeDurabilityOnBlockBreak(BreakEvent event) {
		findAndDamageExtendoGrip(event.getPlayer());
	}

	//@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void consumeDurabilityOnPlace(EntityPlaceEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof Player)
			findAndDamageExtendoGrip((Player) entity);
	}

//	@SubscribeEvent(priority = EventPriority.LOWEST)
//	public static void consumeDurabilityOnPlace(PlayerInteractEvent event) {
//		findAndDamageExtendoGrip(event.getPlayer());
//	}

	private static void findAndDamageExtendoGrip(Player player) {
		if (player == null)
			return;
		if (player.level().isClientSide)
			return;
		EquipmentSlot equipmentSlot = EquipmentSlot.MAINHAND;
		ItemStack extendo = player.getMainHandItem();
		if (!AllItems.EXTENDO_GRIP.isIn(extendo)) {
			extendo = player.getOffhandItem();
			equipmentSlot = EquipmentSlot.OFFHAND;
		}
		if (!AllItems.EXTENDO_GRIP.isIn(extendo))
			return;
		if (!BacktankUtil.canAbsorbDamage(player, maxUses()))
			extendo.hurtAndBreak(1, player, equipmentSlot);
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return BacktankUtil.isBarVisible(stack, maxUses());
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		return BacktankUtil.getBarWidth(stack, maxUses());
	}

	@Override
	public int getBarColor(ItemStack stack) {
		return BacktankUtil.getBarColor(stack, maxUses());
	}

	private static int maxUses() {
		return AllConfigs.server().equipment.maxExtendoGripActions.get();
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
		return true;
	}

	static {
		LivingDamageEvent.DAMAGE.register(ExtendoGripItem::bufferLivingAttackEvent);
		LivingKnockBackEvent.EVENT.register(ExtendoGripItem::attacksByExtendoGripHaveMoreKnockback);
	}

	public static void bufferLivingAttackEvent(LivingDamageEvent event) {
		// Workaround for removed patch to get the attacking entity.
		lastActiveDamageSource = event.getSource();

		DamageSource source = event.getSource();
		if (source == null)
			return;
		Entity trueSource = source.getEntity();
		if (trueSource instanceof Player)
			findAndDamageExtendoGrip((Player) trueSource);
	}

	public static void attacksByExtendoGripHaveMoreKnockback(LivingKnockBackEvent event) {
		if (lastActiveDamageSource == null)
			return;
		Entity entity = lastActiveDamageSource.getDirectEntity();
		lastActiveDamageSource = null;
		if (!(entity instanceof Player player))
			return;
		if (!isHoldingExtendoGrip(player))
			return;
		event.setStrength(event.getStrength() + 2);
	}

	private static boolean isUncaughtClientInteraction(Entity entity, Entity target) {
		// Server ignores entity interaction further than 6m
		if (entity.distanceToSqr(target) < 36)
			return false;
		if (!entity.level().isClientSide)
			return false;
		if (!(entity instanceof Player))
			return false;
		return true;
	}

	@Environment(EnvType.CLIENT)
	public static void notifyServerOfLongRangeAttacks(AttackEntityEvent event) {
		Entity entity = event.getEntity();
		Entity target = event.getTarget();
		if (!isUncaughtClientInteraction(entity, target))
			return;
		Player player = (Player) entity;
		if (isHoldingExtendoGrip(player))
			CatnipServices.NETWORK.sendToServer(new ExtendoGripInteractionPacket(target));
	}

	@Environment(EnvType.CLIENT)
	public static void notifyServerOfLongRangeInteractions(PlayerInteractEvent.EntityInteract event) {
		Entity entity = event.getEntity();
		Entity target = event.getTarget();
		if (!isUncaughtClientInteraction(entity, target))
			return;
		Player player = (Player) entity;
		if (isHoldingExtendoGrip(player))
			CatnipServices.NETWORK.sendToServer(new ExtendoGripInteractionPacket(target, event.getHand()));
	}

	@Environment(EnvType.CLIENT)
	public static void notifyServerOfLongRangeSpecificInteractions(PlayerInteractEvent.EntityInteractSpecific event) {
		Player entity = event.getEntity();
		Entity target = event.getTarget();
		if (!isUncaughtClientInteraction(entity, target))
			return;
		if (isHoldingExtendoGrip(entity))
			CatnipServices.NETWORK.sendToServer(new ExtendoGripInteractionPacket(target, event.getHand(), event.getLocalPos()));
	}

	public static boolean isHoldingExtendoGrip(Player player) {
		boolean inOff = AllItems.EXTENDO_GRIP.isIn(player.getOffhandItem());
		boolean inMain = AllItems.EXTENDO_GRIP.isIn(player.getMainHandItem());
		boolean holdingGrip = inOff || inMain;
		return holdingGrip;
	}

	@Environment(EnvType.CLIENT)
	public void initializeClient() {
		BuiltinItemRendererRegistry.INSTANCE.register(this, SimpleCustomRenderer.create(this, new ExtendoGripItemRenderer()));
	}

}
