package com.coremvc.service;

import com.coremvc.dto.SettingDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SettingService {
    List<SettingDto> getAllSettings();
    
    Page<SettingDto> getAllSettings(Pageable pageable);
    
    SettingDto getSettingById(Long id);
    
    SettingDto createSetting(SettingDto settingDto);
    
    SettingDto updateSetting(Long id, SettingDto settingDto);
    
    void deleteSetting(Long id);
    
    Page<SettingDto> searchSettingsByName(String name, Pageable pageable);
}
