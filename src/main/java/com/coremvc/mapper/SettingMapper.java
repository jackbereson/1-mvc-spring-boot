package com.coremvc.mapper;

import com.coremvc.dto.SettingDto;
import com.coremvc.model.Setting;
import org.springframework.stereotype.Component;

@Component
public class SettingMapper {
    public SettingDto toDto(Setting setting) {
        if (setting == null) {
            return null;
        }

        return SettingDto.builder()
                .id(setting.getId())
                .name(setting.getName())
                .isActive(setting.getIsActive())
                .createdAt(setting.getCreatedAt())
                .updatedAt(setting.getUpdatedAt())
                .build();
    }

    public Setting toEntity(SettingDto settingDto) {
        if (settingDto == null) {
            return null;
        }

        return Setting.builder()
                .name(settingDto.getName())
                .isActive(settingDto.getIsActive())
                .build();
    }
}
