package com.coremvc.util;

import com.coremvc.config.SettingData;
import com.coremvc.model.Setting;
import com.coremvc.repository.SettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Helper class to load settings by key with caching and fallback to default values
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class SettingHelper {

    private final SettingRepository settingRepository;

    private static final Map<String, String> DEFAULT_SETTINGS = SettingData.getDefaultSettingsMap();

    // new static reference to allow static methods to access repository
    private static SettingRepository staticSettingRepository;

    @PostConstruct
    private void initStatic() {
        staticSettingRepository = this.settingRepository;
    }

    /**
     * Load setting value by key synchronously
     *
     * @param key    Setting key
     * @param secure If true, private settings will not be returned
     * @return Setting value or null if not found/inactive
     */
    public String load(String key, boolean secure) {
        Optional<Setting> setting = settingRepository.findByKey(key);

        if (setting.isPresent()) {
            Setting s = setting.get();
            if (!s.getIsActive()) {
                return null;
            }
            if (s.getIsPrivate().equals("true") && secure) {
                return null;
            }
            return s.getValue();
        }

        return null;
    }

    /**
     * Load setting value by key with default secure = false
     */
    public String load(String key) {
        return load(key, false);
    }

    /**
     * Load multiple settings by keys asynchronously
     *
     * @param keys   List of setting keys
     * @param secure If true, private settings will not be returned
     * @return CompletableFuture with list of setting values
     */
    public CompletableFuture<List<String>> loadMany(List<String> keys, boolean secure) {
        return CompletableFuture.supplyAsync(() -> {
            List<Setting> settings = settingRepository.findAll();
            Map<String, Setting> settingMap = settings.stream()
                    .collect(Collectors.toMap(Setting::getKey, s -> s));

            return keys.stream()
                    .map(key -> {
                        Setting setting = settingMap.get(key);
                        if (setting != null) {
                            if (!setting.getIsActive()) {
                                return null;
                            }
                            if (setting.getIsPrivate().equals("true") && secure) {
                                return null;
                            }
                            return setting.getValue();
                        }
                        return null;
                    })
                    .collect(Collectors.toList());
        });
    }

    /**
     * Load multiple settings by keys with default secure = false
     */
    public CompletableFuture<List<String>> loadMany(List<String> keys) {
        return loadMany(keys, false);
    }

    /**
     * Get default settings map
     */
    public static Map<String, String> getDefaultSettings() {
        return new HashMap<>(DEFAULT_SETTINGS);
    }

    /**
     * Get setting by key (raw object)
     */
    public Optional<Setting> getSettingRaw(String key) {
        return settingRepository.findByKey(key);
    }

    /**
     * Static counterpart of load(key, secure).
     * Attempts to lazily obtain the repository from SpringContext if not set yet.
     * Returns null if repository still cannot be obtained.
     */
    public static String loadStatic(String key, boolean secure) {
        if (staticSettingRepository == null) {
            staticSettingRepository = SpringContext.getBean(SettingRepository.class);
            if (staticSettingRepository == null) {
                return null; // repository not available yet
            }
        }
        Optional<Setting> setting = staticSettingRepository.findByKey(key);

        if (setting.isPresent()) {
            Setting s = setting.get();
            if (!s.getIsActive()) {
                return null;
            }
            if (s.getIsPrivate().equals("true") && secure) {
                return null;
            }
            return s.getValue();
        }

        return null;
    }

    /**
     * Static overload with secure = false
     */
    public static String loadStatic(String key) {
        return loadStatic(key, false);
    }

    /**
     * Static counterpart of loadMany(keys, secure).
     * Attempts to lazily obtain the repository from SpringContext if not set yet.
     * Returns a completed future with nulls if repository cannot be obtained.
     */
    public static CompletableFuture<List<String>> loadManyStatic(List<String> keys, boolean secure) {
        if (staticSettingRepository == null) {
            staticSettingRepository = SpringContext.getBean(SettingRepository.class);
            if (staticSettingRepository == null) {
                List<String> nulls = new ArrayList<>(Collections.nCopies(keys.size(), null));
                return CompletableFuture.completedFuture(nulls);
            }
        }

        return CompletableFuture.supplyAsync(() -> {
            List<Setting> settings = staticSettingRepository.findAll();
            Map<String, Setting> settingMap = settings.stream()
                    .collect(Collectors.toMap(Setting::getKey, s -> s));

            return keys.stream()
                    .map(key -> {
                        Setting setting = settingMap.get(key);
                        if (setting != null) {
                            if (!setting.getIsActive()) {
                                return null;
                            }
                            if (setting.getIsPrivate().equals("true") && secure) {
                                return null;
                            }
                            return setting.getValue();
                        }
                        return null;
                    })
                    .collect(Collectors.toList());
        });
    }

    /**
     * Static overload with secure = false
     */
    public static CompletableFuture<List<String>> loadManyStatic(List<String> keys) {
        return loadManyStatic(keys, false);
    }
}
