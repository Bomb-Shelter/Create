package com.simibubi.create.infrastructure.fabric.transfer;

import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

public class FinalCommitSnapshot extends SnapshotParticipant<Long> {
	public long amount;
	public Runnable runnable;

	public FinalCommitSnapshot(long amount, Runnable runnable) {
		this.amount = amount;
	}

	@Override
	protected Long createSnapshot() {
		return amount;
	}

	@Override
	protected void readSnapshot(Long snapshot) {
		this.amount = snapshot;
	}

	@Override
	protected void onFinalCommit() {
		this.runnable.run();
	}
}
