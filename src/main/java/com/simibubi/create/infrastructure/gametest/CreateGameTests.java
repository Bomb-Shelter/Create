package com.simibubi.create.infrastructure.gametest;

import java.util.Collection;

import com.simibubi.create.infrastructure.gametest.tests.TestContraptions;
import com.simibubi.create.infrastructure.gametest.tests.TestFluids;
import com.simibubi.create.infrastructure.gametest.tests.TestItems;
import com.simibubi.create.infrastructure.gametest.tests.TestMisc;
import com.simibubi.create.infrastructure.gametest.tests.TestProcessing;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;

//@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class CreateGameTests implements FabricGameTest {
	private static final Class<?>[] testHolders = {
			TestContraptions.class,
			TestFluids.class,
			TestItems.class,
			TestMisc.class,
			TestProcessing.class
	};

	@GameTestGenerator
	public static Collection<TestFunction> generateTests() {
		return CreateTestFunction.getTestsFrom(testHolders);
	}
}
