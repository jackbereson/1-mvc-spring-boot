package com.coremvc.service.impl;

import com.coremvc.dto.RestPage;
import com.coremvc.dto.SettingDto;
import com.coremvc.exception.ResourceNotFoundException;
import com.coremvc.mapper.SettingMapper;
import com.coremvc.model.Setting;
import com.coremvc.repository.SettingRepository;
import com.coremvc.service.SettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
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
    @Cacheable(value = "setting::list", key = "'all'")
    public List<SettingDto> getAllSettings() {
        return settingRepository.findAll().stream()
                .map(settingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "setting::list", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<SettingDto> getAllSettings(Pageable pageable) {
        Page<SettingDto> page = settingRepository.findAll(pageable)
                .map(settingMapper::toDto);
        return new RestPage<>(page.getContent(), pageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "settings", key = "#id")
    public SettingDto getSettingById(Long id) {
        Setting setting = settingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found"));

        return settingMapper.toDto(setting);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "settings", key = "'key:' + #key")
    public SettingDto getSettingByKey(String key) {
        Setting setting = settingRepository.findByKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found with key: " + key));

        return settingMapper.toDto(setting);
    }

    @Override
    @CacheEvict(value = {"settings", "setting::list"}, allEntries = true)
    public SettingDto createSetting(SettingDto settingDto) {
        Setting setting = settingMapper.toEntity(settingDto);
        Setting savedSetting = settingRepository.save(setting);
        return settingMapper.toDto(savedSetting);
    }

    @Override
    @CachePut(value = "settings", key = "#id")
    @CacheEvict(value = "setting::list", allEntries = true)
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
    @CacheEvict(value = {"settings", "setting::list"}, allEntries = true)
    public void deleteSetting(Long id) {
        Setting setting = settingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found with id: " + id));
        settingRepository.delete(setting);
    }

    @Override
    @Cacheable(value = "setting::list", key = "'search:' + #name + ':' + #pageable.pageNumber")
    public Page<SettingDto> searchSettingsByName(String name, Pageable pageable) {
        Page<SettingDto> page = settingRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(settingMapper::toDto);
        return new RestPage<>(page.getContent(), pageable, page.getTotalElements());
    }
}
