package com.skillSwap.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    // NEW: NULL (old rows) counts as approved. Every skill created from now on starts PENDING.
    @Enumerated(EnumType.STRING)
    private SkillStatus status;

    @PrePersist
    void defaultStatus() {
        if (status == null) status = SkillStatus.PENDING;
    }
}
