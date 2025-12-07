package com.coremvc.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "settings")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Setting extends BaseEntity {

    @Column(name = "setting_name", nullable = false)
    private String name;

    @Column(name = "setting_type", nullable = false)
    private String type;

    @Column(name = "setting_key", nullable = false)
    private String key;

    @Column(name = "setting_value", nullable = false)
    private String value;

    @Column(nullable = false)
    private String isPrivate;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
