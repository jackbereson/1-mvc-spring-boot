package com.coremvc.service.impl;

import com.coremvc.dto.SettingDto;
import com.coremvc.exception.ResourceNotFoundException;
import com.coremvc.mapper.SettingMapper;
import com.coremvc.model.Setting;
import com.coremvc.repository.SettingRepository;
import com.coremvc.service.SettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettingServiceImpl implements SettingService {
    private final SettingRepository settingRepository;
    private final SettingMapper settingMapper;

    @Override
    public List<SettingDto> getAllSettings() {
        return settingRepository.findAll().stream()
                .map(settingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SettingDto> getAllSettings(Pageable pageable) {
        return settingRepository.findAll(pageable)
                .map(settingMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public SettingDto getSettingById(Long id) {
        Setting setting = settingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found"));

        return settingMapper.toDto(setting);
    }

    @Override
    public SettingDto createSetting(SettingDto settingDto) {
        Setting setting = settingMapper.toEntity(settingDto);
        Setting savedSetting = settingRepository.save(setting);
        return settingMapper.toDto(savedSetting);
    }

    @Override
    public SettingDto updateSetting(Long id, SettingDto settingDto) {
        Setting existingSetting = settingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found with id: " + id));

        if (settingDto.getName() != null) {
            existingSetting.setName(settingDto.getName());
        }
        if (settingDto.getIsActive() != null) {
            existingSetting.setIsActive(settingDto.getIsActive());
        }

        Setting updatedSetting = settingRepository.save(existingSetting);
        return settingMapper.toDto(updatedSetting);
    }

    @Override
    public void deleteSetting(Long id) {
        Setting setting = settingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found with id: " + id));
        settingRepository.delete(setting);
    }

    @Override
    public Page<SettingDto> searchSettingsByName(String name, Pageable pageable) {
        return settingRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(settingMapper::toDto);
    }
}
