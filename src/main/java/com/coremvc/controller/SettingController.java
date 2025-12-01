package com.coremvc.controller;

import com.coremvc.dto.ApiResponse;
import com.coremvc.dto.SettingDto;
import com.coremvc.service.SettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/settings")
@CrossOrigin(origins= "*")
@RequiredArgsConstructor
public class SettingController {

    private final SettingService settingService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<SettingDto>>> getAllSettings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<SettingDto> settings = settingService.getAllSettings(pageable);

        return ResponseEntity.ok(
                new ApiResponse<>("Settings retrieved successfully", settings, true)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SettingDto>> getSettingById(@PathVariable Long id) {
        SettingDto setting = settingService.getSettingById(id);
        return ResponseEntity.ok(
                new ApiResponse<>("Setting retrieved successfully", setting, true)
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SettingDto>> createSetting(@Valid @RequestBody SettingDto settingDto) {
        SettingDto createdSetting = settingService.createSetting(settingDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>("Setting created successfully", createdSetting, true)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SettingDto>> updateSetting(
            @PathVariable Long id,
            @Valid @RequestBody SettingDto settingDto) {

        SettingDto updatedSetting = settingService.updateSetting(id, settingDto);
        return ResponseEntity.ok(
                new ApiResponse<>("Setting updated successfully", updatedSetting, true)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSetting(@PathVariable Long id) {
        settingService.deleteSetting(id);
        return ResponseEntity.ok(
                new ApiResponse<>("Setting deleted successfully", null, true)
        );
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<SettingDto>>> searchSettings(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<SettingDto> settings = settingService.searchSettingsByName(name, pageable);
        return ResponseEntity.ok(
                new ApiResponse<>("Settings retrieved successfully", settings, true)
        );
    }
}
