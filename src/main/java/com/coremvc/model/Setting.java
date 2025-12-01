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

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "boolean default true")
    @Builder.Default
    private Boolean isActive = true;
}
