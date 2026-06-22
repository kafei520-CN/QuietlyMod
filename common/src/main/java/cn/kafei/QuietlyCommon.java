package cn.kafei;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class QuietlyCommon {
	public static final String MOD_ID = "quietly";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final double DEFAULT_BLOCK_INTERACTION_RANGE = 4.5D;
	private static final InteractionResult INTERACTION_RESULT_SUCCESS = resolveInteractionResult("SUCCESS", 0, "field_5812", "c");

	private QuietlyCommon() {
	}

	public static void initialize(Path configDirectory) {
		QuietlyLocalization.initialize();
		QuietlyConfig.initialize(configDirectory);
		LOGGER.info("Quietly common initialized for Minecraft 1.20.1");
		LOGGER.info("Quietly server language: {}", QuietlyConfig.language());
	}

	public static boolean isSupportedSilentContainer(Level level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		return state.getMenuProvider(level, pos) != null || state.getBlock() instanceof EnderChestBlock;
	}

	public static boolean shouldCancelVanillaUse(boolean sneaking, InteractionHand hand, ItemStack heldStack, Level level, BlockPos pos) {
		return sneaking
			&& hand == InteractionHand.MAIN_HAND
			&& heldStack != null
			&& heldStack.isEmpty()
			&& isSupportedSilentContainer(level, pos);
	}

	public static InteractionResult interactionResultSuccess() {
		return INTERACTION_RESULT_SUCCESS;
	}

	public static ServerLevel getServerLevel(ServerPlayer player) {
		if (player == null) {
			return null;
		}

		for (String methodName : List.of("serverLevel", "level", "method_37908", "method_7325", "fY")) {
			try {
				Method method = player.getClass().getMethod(methodName);
				Object value = method.invoke(player);
				if (value instanceof ServerLevel serverLevel) {
					return serverLevel;
				}
			} catch (ReflectiveOperationException | RuntimeException ignored) {
			}
		}

		return null;
	}

	public static double blockInteractionRange(ServerPlayer player) {
		if (player == null) {
			return DEFAULT_BLOCK_INTERACTION_RANGE;
		}

		for (String methodName : List.of("blockInteractionRange", "method_55754")) {
			try {
				Method method = player.getClass().getMethod(methodName);
				Object value = method.invoke(player);
				if (value instanceof Number number) {
					return number.doubleValue();
				}
			} catch (ReflectiveOperationException | RuntimeException ignored) {
			}
		}

		return DEFAULT_BLOCK_INTERACTION_RANGE;
	}

	public static BlockState setBooleanProperty(BlockState state, String propertyName, boolean value) {
		for (Property<?> property : state.getProperties()) {
			if (property instanceof BooleanProperty booleanProperty && propertyName.equals(property.getName())) {
				return state.setValue(booleanProperty, value);
			}
		}
		return state;
	}

	public static boolean isSilentContainerSound(SoundEvent soundEvent) {
		if (soundEvent == null) {
			return false;
		}

		String soundId = resolveSoundEventId(soundEvent);
		return soundId != null && isSilentContainerSoundId(soundId);
	}

	public static boolean isSilentContainerSound(Holder<SoundEvent> soundHolder) {
		if (soundHolder == null) {
			return false;
		}

		String soundId = resolveHolderId(soundHolder);
		if (soundId == null) {
			try {
				soundId = resolveSoundEventId(soundHolder.value());
			} catch (RuntimeException ignored) {
				return false;
			}
		}

		return soundId != null && isSilentContainerSoundId(soundId);
	}

	public static boolean isSilentContainerGameEvent(Holder<?> gameEventHolder) {
		if (gameEventHolder == null) {
			return false;
		}

		String gameEventId = resolveHolderId(gameEventHolder);
		return "minecraft:container_open".equals(gameEventId)
			|| "minecraft:container_close".equals(gameEventId);
	}

	// isSilentContainerGameEvent：识别 1.20.1 直接传入的容器开关声感事件。
	public static boolean isSilentContainerGameEvent(GameEvent gameEvent) {
		if (gameEvent == null) {
			return false;
		}

		if (gameEvent == GameEvent.CONTAINER_OPEN || gameEvent == GameEvent.CONTAINER_CLOSE) {
			return true;
		}

		String gameEventId = resolveGameEventId(gameEvent);
		return "minecraft:container_open".equals(gameEventId)
			|| "minecraft:container_close".equals(gameEventId)
			|| "container_open".equals(gameEventId)
			|| "container_close".equals(gameEventId);
	}

	public static <E extends Enum<E>> E enumValue(Class<E> enumClass, String valueName) {
		return Enum.valueOf(enumClass, valueName);
	}

	private static boolean isSilentContainerSoundId(String soundId) {
		return "minecraft:block.chest.open".equals(soundId)
			|| "minecraft:block.chest.close".equals(soundId)
			|| "minecraft:block.barrel.open".equals(soundId)
			|| "minecraft:block.barrel.close".equals(soundId)
			|| "minecraft:block.ender_chest.open".equals(soundId)
			|| "minecraft:block.ender_chest.close".equals(soundId)
			|| "minecraft:block.shulker_box.open".equals(soundId)
			|| "minecraft:block.shulker_box.close".equals(soundId);
	}

	private static String resolveSoundEventId(SoundEvent soundEvent) {
		if (soundEvent == null) {
			return null;
		}

		try {
			ResourceLocation key = BuiltInRegistries.SOUND_EVENT.getKey(soundEvent);
			if (key != null) {
				return key.toString();
			}
		} catch (RuntimeException ignored) {
		}

		for (String methodName : List.of("getLocation", "location", "method_14833", "a")) {
			try {
				Method method = SoundEvent.class.getMethod(methodName);
				Object value = method.invoke(soundEvent);
				if (value != null) {
					return value.toString();
				}
			} catch (ReflectiveOperationException | RuntimeException ignored) {
			}
		}

		for (String fieldName : List.of("location", "field_15152", "a")) {
			try {
				Field field = SoundEvent.class.getDeclaredField(fieldName);
				field.setAccessible(true);
				Object value = field.get(soundEvent);
				if (value != null) {
					return value.toString();
				}
			} catch (ReflectiveOperationException | RuntimeException ignored) {
			}
		}

		return null;
	}

	// resolveHolderId：从 1.20.1 Holder 资源键解析注册名。
	private static String resolveHolderId(Holder<?> holder) {
		try {
			return holder.unwrapKey()
				.map(resourceKey -> resourceKey.location().toString())
				.orElse(null);
		} catch (RuntimeException ignored) {
			return null;
		}
	}

	// resolveGameEventId：解析 1.20.1 GameEvent 注册名，用于静默过滤兜底判断。
	private static String resolveGameEventId(GameEvent gameEvent) {
		try {
			ResourceLocation key = BuiltInRegistries.GAME_EVENT.getKey(gameEvent);
			if (key != null) {
				return key.toString();
			}
		} catch (RuntimeException ignored) {
		}

		try {
			return gameEvent.getName();
		} catch (RuntimeException ignored) {
			return null;
		}
	}

	private static InteractionResult resolveInteractionResult(String enumName, int nonEnumFieldIndex, String... fallbackFieldNames) {
		if (InteractionResult.class.isEnum()) {
			for (Object constant : InteractionResult.class.getEnumConstants()) {
				if (constant instanceof Enum<?> enumConstant && enumName.equals(enumConstant.name())) {
					return (InteractionResult) constant;
				}
			}
			throw new IllegalStateException("Unable to resolve enum InteractionResult " + enumName);
		}

		InteractionResult byNamedField = findNamedInteractionResultField(enumName);
		if (byNamedField != null) {
			return byNamedField;
		}

		for (String fallbackFieldName : fallbackFieldNames) {
			InteractionResult byFallbackField = findNamedInteractionResultField(fallbackFieldName);
			if (byFallbackField != null) {
				return byFallbackField;
			}
		}

		InteractionResult byIndexedField = findIndexedInteractionResultField(nonEnumFieldIndex);
		if (byIndexedField != null) {
			return byIndexedField;
		}

		throw new IllegalStateException("Unable to resolve InteractionResult " + enumName);
	}

	private static InteractionResult findNamedInteractionResultField(String fieldName) {
		try {
			Field field = InteractionResult.class.getField(fieldName);
			if (Modifier.isStatic(field.getModifiers()) && InteractionResult.class.isAssignableFrom(field.getType())) {
				return (InteractionResult) field.get(null);
			}
		} catch (ReflectiveOperationException ignored) {
		}
		return null;
	}

	private static InteractionResult findIndexedInteractionResultField(int fieldIndex) {
		List<Field> fields = new ArrayList<>();
		for (Field field : InteractionResult.class.getFields()) {
			if (Modifier.isStatic(field.getModifiers()) && InteractionResult.class.isAssignableFrom(field.getType())) {
				fields.add(field);
			}
		}

		if (fieldIndex < 0 || fieldIndex >= fields.size()) {
			return null;
		}

		try {
			return (InteractionResult) fields.get(fieldIndex).get(null);
		} catch (ReflectiveOperationException exception) {
			return null;
		}
	}
}
