package com.coremvc.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Slf4j
public class SettingData {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettingItem {
        private String type;
        private String name;
        private String key;
        private String value;
        private Boolean isActive;
        private Boolean isPrivate;
    }

    private static List<SettingItem> settingData;

    public static List<SettingItem> getSettingData() {
        if (settingData == null) {
            loadSettingData();
        }
        return settingData;
    }

    private static synchronized void loadSettingData() {
        if (settingData != null) {
            return;
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ClassPathResource resource = new ClassPathResource("settingData.json");
            try (InputStream inputStream = resource.getInputStream()) {
                settingData = objectMapper.readValue(inputStream, new TypeReference<List<SettingItem>>() {});
            }
            log.info("Loaded {} settings from settingData.json", settingData.size());
        } catch (IOException e) {
            log.error("Failed to load settingData.json", e);
            settingData = Collections.emptyList();
        }
    }

    /**
     * Get default settings map by key
     */
    public static Map<String, String> getDefaultSettingsMap() {
        Map<String, String> defaultSettings = new HashMap<>();
        for (SettingItem setting : getSettingData()) {
            defaultSettings.put(setting.getKey(), setting.getValue());
        }
        return defaultSettings;
    }
}
