package com.simibubi.create.api.event;

import com.simibubi.create.content.trains.graph.TrackGraph;

import io.github.fabricators_of_create.porting_lib.core.event.BaseEvent;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class TrackGraphMergeEvent extends BaseEvent {
	public static final Event<TrackGraphMergeCallback> EVENT = EventFactory.createArrayBacked(TrackGraphMergeCallback.class, callbacks -> event -> {
		for (TrackGraphMergeCallback callback : callbacks) {
			callback.onTrackGraphMerge(event);
		}
	});

	private final TrackGraph mergedInto;
	private final TrackGraph mergedFrom;
	public TrackGraphMergeEvent(TrackGraph from, TrackGraph into) {
		mergedInto = into;
		mergedFrom = from;
	}

	public TrackGraph getGraphMergedInto() {
		return mergedInto;
	}

	public TrackGraph getGraphMergedFrom() {
		return mergedFrom;
	}

	@Override
	public void sendEvent() {
		EVENT.invoker().onTrackGraphMerge(this);
	}

	public interface TrackGraphMergeCallback {
		void onTrackGraphMerge(TrackGraphMergeEvent event);
	}
}
