package cn.kafei.forge;

import cn.kafei.QuietlyCommon;
import cn.kafei.SilentOpenManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

@SuppressWarnings("null")
@Mod(QuietlyCommon.MOD_ID)
public final class QuietlyForge {
	// Forge 模组入口：初始化公共配置并挂接服务端事件。
	public QuietlyForge() {
		QuietlyCommon.initialize(FMLPaths.CONFIGDIR.get());
		MinecraftForge.EVENT_BUS.addListener(QuietlyForge::onRightClickBlock);
		MinecraftForge.EVENT_BUS.addListener(QuietlyForge::onServerTick);
		QuietlyCommon.LOGGER.info("Quietly Forge initialized");
	}

	// onRightClickBlock：拦截潜行空手右键容器，转入静默开启流程。
	private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) {
			return;
		}

		if (!QuietlyCommon.shouldCancelVanillaUse(
			player.isShiftKeyDown(),
			event.getHand(),
			player.getItemInHand(event.getHand()),
			event.getLevel(),
			event.getPos()
		)) {
			return;
		}

		SilentOpenManager.queueOrStart(player, event.getPos());
		event.setCancellationResult(QuietlyCommon.interactionResultSuccess());
		event.setCanceled(true);
	}

	// onServerTick：在服务端 tick 末尾推进所有静默开启进度。
	private static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.END) {
			return;
		}
		SilentOpenManager.tickActiveOpens();
	}
}
