package me.av306.chathook.tests;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

@SuppressWarnings( "UnstableApiUsage" )
public class ChatHookClientGameTest implements FabricClientGameTest
{
	@Override
	public void runTest( ClientGameTestContext context )
	{
		try ( TestSingleplayerContext singleplayer = context.worldBuilder().create() )
		{
			singleplayer.getClientWorld().waitForChunksRender();
			context.takeScreenshot( "chathook-client-test" );
		}
	}
}