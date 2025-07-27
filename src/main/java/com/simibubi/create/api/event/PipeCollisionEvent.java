package com.simibubi.create.api.event;

import com.simibubi.create.api.event.PipeCollisionEvent.Spill.SpillCallback;

import io.github.fabricators_of_create.porting_lib.core.event.BaseEvent;

import net.fabricmc.fabric.api.event.Event;

import net.fabricmc.fabric.api.event.EventFactory;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

/**
 * This Event is fired when two fluids meet in a pipe ({@link Flow})<br>
 * or when a fluid in a pipe meets with a fluid in the world
 * ({@link Spill}).<br>
 * <br>
 * If it is not null, the event's BlockState will be placed in world after
 * firing.
 */
public abstract class PipeCollisionEvent extends BaseEvent {

	protected final Fluid firstFluid, secondFluid;
	private final Level level;
	private final BlockPos pos;
	@Nullable
	private BlockState state;

	protected PipeCollisionEvent(Level level, BlockPos pos, Fluid firstFluid, Fluid secondFluid,
								 @Nullable BlockState defaultState) {
		this.level = level;
		this.pos = pos;
		this.firstFluid = firstFluid;
		this.secondFluid = secondFluid;
		this.state = defaultState;
	}

	public Level getLevel() {
		return level;
	}

	public BlockPos getPos() {
		return pos;
	}

	@Nullable
	public BlockState getState() {
		return state;
	}

	public void setState(@Nullable BlockState state) {
		this.state = state;
	}

	public static class Flow extends PipeCollisionEvent {
		public static final Event<FlowCallback> EVENT = EventFactory.createArrayBacked(FlowCallback.class, callbacks -> event -> {
			for (FlowCallback callback : callbacks) {
				callback.onPipeCollisionFlow(event);
			}
		});

		public Flow(Level level, BlockPos pos, Fluid firstFluid, Fluid secondFluid, @Nullable BlockState defaultState) {
			super(level, pos, firstFluid, secondFluid, defaultState);
		}

		public Fluid getFirstFluid() {
			return firstFluid;
		}

		public Fluid getSecondFluid() {
			return secondFluid;
		}

		@Override
		public void sendEvent() {
			EVENT.invoker().onPipeCollisionFlow(this);
		}

		public interface FlowCallback {
			void onPipeCollisionFlow(Flow event);
		}
	}

	public static class Spill extends PipeCollisionEvent {
		public static final Event<SpillCallback> EVENT = EventFactory.createArrayBacked(SpillCallback.class, callbacks -> event -> {
			for (SpillCallback callback : callbacks) {
				callback.onPipeCollisionSpill(event);
			}
		});

		public Spill(Level level, BlockPos pos, Fluid worldFluid, Fluid pipeFluid, @Nullable BlockState defaultState) {
			super(level, pos, worldFluid, pipeFluid, defaultState);
		}

		public Fluid getWorldFluid() {
			return firstFluid;
		}

		public Fluid getPipeFluid() {
			return secondFluid;
		}

		@Override
		public void sendEvent() {
			EVENT.invoker().onPipeCollisionSpill(this);
		}

		public interface SpillCallback {
			void onPipeCollisionSpill(Spill event);
		}
	}
}
