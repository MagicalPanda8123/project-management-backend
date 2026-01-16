package org.magicalpanda.projectmanagementbackend.model.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter

public enum ProjectRole {
    OWNER,
    MANAGER,
    MEMBER;

    public boolean canManageMembers() {
        return this == OWNER || this == MANAGER;
    }

    public boolean canDeleteProject() {
        return this == OWNER;
    }

    @JsonCreator
    public static ProjectRole from(String value) {
        return ProjectRole.valueOf(value.toUpperCase());
    }
}
