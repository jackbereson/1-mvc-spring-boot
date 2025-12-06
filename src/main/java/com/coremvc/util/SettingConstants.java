package com.coremvc.util;

import lombok.Getter;

@SuppressWarnings("unused")
public class SettingConstants {

    @Getter
    public enum SettingGroupSlug {
        COMMON("COMMON");

        private final String value;

        SettingGroupSlug(String value) {
            this.value = value;
        }
    }

    @Getter
    public enum SettingKey {
        TITLE("TITLE"),
        WEBSITE_DOMAIN("WEBSITE_DOMAIN"),
        API_DOMAIN("API_DOMAIN"),
        MEDIA_DOMAIN("MEDIA_DOMAIN"),
        MAINTENANCE("MAINTENANCE");

        private final String value;

        SettingKey(String value) {
            this.value = value;
        }
    }

    @Getter
    public enum SettingType {
        STRING("string"),
        BOOLEAN("boolean"),
        NUMBER("number"),
        JSON("json");

        private final String value;

        SettingType(String value) {
            this.value = value;
        }
    }
}

