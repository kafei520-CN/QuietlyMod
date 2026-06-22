package cn.kafei;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class SilentMenuState {
	private static final long NANOS_PER_TICK = 50_000_000L;
	private static final int DEFAULT_SUPPRESSION_TICKS = 10;
	private static final ThreadLocal<Boolean> OPENING_SILENTLY = ThreadLocal.withInitial(() -> false);
	private static final ThreadLocal<Integer> SUPPRESS_CONTAINER_SOUNDS = ThreadLocal.withInitial(() -> 0);
	private static final Set<AbstractContainerMenu> SILENT_MENUS = ConcurrentHashMap.newKeySet();
	private static final Map<SuppressionKey, Long> SUPPRESSED_CONTAINER_POSITIONS = new ConcurrentHashMap<>();

	private SilentMenuState() {
	}

	public static void beginSilentOpen() {
		OPENING_SILENTLY.set(true);
	}

	public static void endSilentOpen() {
		OPENING_SILENTLY.remove();
	}

	public static boolean isOpeningSilently() {
		return OPENING_SILENTLY.get();
	}

	public static void beginSuppressContainerSounds() {
		SUPPRESS_CONTAINER_SOUNDS.set(SUPPRESS_CONTAINER_SOUNDS.get() + 1);
	}

	public static void endSuppressContainerSounds() {
		int depth = SUPPRESS_CONTAINER_SOUNDS.get() - 1;
		if (depth <= 0) {
			SUPPRESS_CONTAINER_SOUNDS.remove();
			return;
		}
		SUPPRESS_CONTAINER_SOUNDS.set(depth);
	}

	public static boolean isSuppressingContainerSounds() {
		return SUPPRESS_CONTAINER_SOUNDS.get() > 0;
	}

	public static void suppressContainerAt(Level level, BlockPos pos) {
		suppressContainerAt(level, pos, DEFAULT_SUPPRESSION_TICKS);
	}

	public static void suppressContainerAt(Level level, BlockPos pos, int ticks) {
		if (level == null || pos == null) {
			return;
		}

		long expiresAt = System.nanoTime() + Math.max(1, ticks) * NANOS_PER_TICK;
		ResourceKey<Level> dimension = level.dimension();
		BlockPos immutablePos = pos.immutable();
		cleanupExpiredSuppressions(System.nanoTime());
		SUPPRESSED_CONTAINER_POSITIONS.put(new SuppressionKey(dimension, immutablePos), expiresAt);
	}

	public static boolean shouldMuteContainerEffects(Level level, BlockPos pos) {
		if (level == null) {
			return false;
		}

		return shouldMuteContainerEffects(level.dimension(), pos);
	}

	public static boolean shouldMuteContainerEffects(ResourceKey<Level> dimension, BlockPos pos) {
		if (isOpeningSilently() || isSuppressingContainerSounds()) {
			return true;
		}

		if (dimension == null || pos == null) {
			return false;
		}

		long now = System.nanoTime();
		cleanupExpiredSuppressions(now);
		Long expiresAt = SUPPRESSED_CONTAINER_POSITIONS.get(new SuppressionKey(dimension, pos));
		return expiresAt != null && expiresAt >= now;
	}

	public static void markSilentMenu(AbstractContainerMenu menu) {
		if (menu != null) {
			SILENT_MENUS.add(menu);
		}
	}

	public static boolean isSilentMenu(AbstractContainerMenu menu) {
		return SILENT_MENUS.contains(menu);
	}

	public static void clearSilentMenu(AbstractContainerMenu menu) {
		SILENT_MENUS.remove(menu);
	}

	private static void cleanupExpiredSuppressions(long now) {
		Iterator<Map.Entry<SuppressionKey, Long>> iterator = SUPPRESSED_CONTAINER_POSITIONS.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<SuppressionKey, Long> entry = iterator.next();
			if (entry.getValue() < now) {
				iterator.remove();
			}
		}
	}

	private record SuppressionKey(ResourceKey<Level> dimension, BlockPos pos) {
	}
}
