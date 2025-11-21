package com.example.back.entity;

import com.example.back.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 系统配置
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "system_config")
public class SystemConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_key", nullable = false, length = 100, unique = true)
    private String configKey;

    @Column(name = "config_value")
    private String configValue;

    @Column(name = "config_type", nullable = false, length = 20)
    private String configType;

    @Column(name = "description", length = 200)
    private String description;

    public SystemConfig(String configKey, String configType) {
        this.configKey = configKey;
        this.configType = configType;
    }
}

