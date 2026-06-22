package cn.kafei.mixin;

import cn.kafei.QuietlyCommon;
import cn.kafei.SilentMenuState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
	@Inject(
		method = "gameEvent(Lnet/minecraft/world/level/gameevent/GameEvent;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/level/gameevent/GameEvent$Context;)V",
		at = @At("HEAD"),
		cancellable = true,
		require = 0
	)
	// quietly$muteContainerGameEvent：静默开启期间阻止容器开关声感事件广播。
	private void quietly$muteContainerGameEvent(
		GameEvent gameEvent,
		Vec3 eventPosition,
		GameEvent.Context context,
		CallbackInfo ci
	) {
		if (shouldMuteContainerGameEvent((ServerLevel) (Object) this, gameEvent, eventPosition)) {
			ci.cancel();
		}
	}

	@Inject(
		method = "playSeededSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V",
		at = @At("HEAD"),
		cancellable = true,
		require = 0
	)
	private void quietly$muteContainerSeededSoundAtPosition(
		@Nullable Player player,
		double x,
		double y,
		double z,
		Holder<SoundEvent> soundEvent,
		SoundSource soundSource,
		float volume,
		float pitch,
		long seed,
		CallbackInfo ci
	) {
		if (shouldMuteContainerSound((ServerLevel) (Object) this, BlockPos.containing(x, y, z), soundEvent, "serverlevel+xyz")) {
			ci.cancel();
		}
	}

	@Inject(
		method = "playSeededSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V",
		at = @At("HEAD"),
		cancellable = true,
		require = 0
	)
	private void quietly$muteContainerSeededSoundAtEntity(
		@Nullable Player player,
		Entity entity,
		Holder<SoundEvent> soundEvent,
		SoundSource soundSource,
		float volume,
		float pitch,
		long seed,
		CallbackInfo ci
	) {
		if (shouldMuteContainerSound((ServerLevel) (Object) this, entity.blockPosition(), soundEvent, "serverlevel+entity")) {
			ci.cancel();
		}
	}

	private static boolean shouldMuteContainerSound(ServerLevel level, BlockPos pos, Holder<SoundEvent> soundEvent, String overload) {
		boolean shouldMute = SilentMenuState.shouldMuteContainerEffects(level, pos);
		if (!QuietlyCommon.isSilentContainerSound(soundEvent)) {
			return false;
		}

		return shouldMute;
	}

	private static boolean shouldMuteContainerGameEvent(ServerLevel level, GameEvent gameEvent, Vec3 eventPosition) {
		if (!QuietlyCommon.isSilentContainerGameEvent(gameEvent) || eventPosition == null) {
			return false;
		}

		return SilentMenuState.shouldMuteContainerEffects(
			level,
			BlockPos.containing(eventPosition.x, eventPosition.y, eventPosition.z)
		);
	}
}
