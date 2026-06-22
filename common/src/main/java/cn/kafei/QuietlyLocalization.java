package cn.kafei;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class QuietlyLocalization {
	private static final Gson GSON = new Gson();
	private static final Type LANG_TYPE = new TypeToken<Map<String, String>>() { }.getType();
	private static final String DEFAULT_LANGUAGE = "zh_cn";
	private static final String FALLBACK_LANGUAGE = "en_us";
	private static final String[] SUPPORTED_LANGUAGES = { DEFAULT_LANGUAGE, FALLBACK_LANGUAGE };
	private static final Map<String, Map<String, String>> LANGUAGES = new ConcurrentHashMap<>();

	private QuietlyLocalization() {
	}

	public static void initialize() {
		LANGUAGES.clear();
		for (String language : SUPPORTED_LANGUAGES) {
			LANGUAGES.put(language, loadLanguage(language));
		}
	}

	public static boolean hasLanguage(String language) {
		for (String supportedLanguage : SUPPORTED_LANGUAGES) {
			if (supportedLanguage.equals(language)) {
				return true;
			}
		}
		return false;
	}

	public static MutableComponent component(String language, String key, Object... args) {
		String template = translate(language, key);
		MutableComponent component = Component.literal("");
		int searchIndex = 0;
		int argIndex = 0;

		while (true) {
			int placeholderIndex = template.indexOf("%s", searchIndex);
			if (placeholderIndex < 0) {
				if (searchIndex < template.length()) {
					component.append(Component.literal(template.substring(searchIndex)));
				}
				return component;
			}

			if (placeholderIndex > searchIndex) {
				component.append(Component.literal(template.substring(searchIndex, placeholderIndex)));
			}

			if (argIndex < args.length) {
				Object argument = args[argIndex++];
				component.append(argument instanceof Component nested ? nested : Component.literal(String.valueOf(argument)));
			} else {
				component.append(Component.literal("%s"));
			}

			searchIndex = placeholderIndex + 2;
		}
	}

	private static String translate(String language, String key) {
		Map<String, String> selectedLanguage = LANGUAGES.get(language);
		if (selectedLanguage != null && selectedLanguage.containsKey(key)) {
			return selectedLanguage.get(key);
		}

		Map<String, String> defaultLanguage = LANGUAGES.get(DEFAULT_LANGUAGE);
		if (defaultLanguage != null && defaultLanguage.containsKey(key)) {
			return defaultLanguage.get(key);
		}

		Map<String, String> fallbackLanguage = LANGUAGES.get(FALLBACK_LANGUAGE);
		if (fallbackLanguage != null && fallbackLanguage.containsKey(key)) {
			return fallbackLanguage.get(key);
		}

		return key;
	}

	private static Map<String, String> loadLanguage(String language) {
		String resourcePath = "/assets/quietly/lang/" + language + ".json";
		InputStream inputStream = QuietlyLocalization.class.getResourceAsStream(resourcePath);
		if (inputStream == null) {
			QuietlyCommon.LOGGER.warn("Missing Quietly language file {}", resourcePath);
			return Map.of();
		}

		try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
			Map<String, String> values = GSON.fromJson(reader, LANG_TYPE);
			return values != null ? values : Map.of();
		} catch (IOException | RuntimeException exception) {
			QuietlyCommon.LOGGER.warn("Failed to load Quietly language file {}", resourcePath, exception);
			return Map.of();
		}
	}
}
