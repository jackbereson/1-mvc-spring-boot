package com.coremvc.repository;

import com.coremvc.model.Setting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettingRepository extends JpaRepository<Setting, Long> {
    Optional<Setting> findById(Long id);

    Optional<Setting> findByKey(String key);
    
    Page<Setting> findByIsActiveTrue(Pageable pageable);
    
    Page<Setting> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
