package com.coremvc.initializer;

import com.coremvc.config.SettingData;
import com.coremvc.model.Setting;
import com.coremvc.repository.SettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SettingDataInitializer implements CommandLineRunner {
    private final SettingRepository settingRepository;

    @Override
    public void run(String... args) throws Exception {
        if(settingRepository.count() == 0) {
            initializeSettings();
        }
    }

    private void initializeSettings() {
        SettingData.getSettingData().forEach(item -> settingRepository.save(Setting.builder()
            .name(item.getName())
            .key(item.getKey())
            .type(item.getType())
            .value(item.getValue())
            .isPrivate(item.getIsPrivate() ? "true" : "false")
            .isActive(item.getIsActive())
            .build()));
    }
}
