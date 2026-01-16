package org.magicalpanda.projectmanagementbackend.model.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ProjectStatus {
    ARCHIVED,
    IN_PROGRESS,
    COMPLETED,
    DELETED;

    @JsonCreator
    public static ProjectStatus from(String value) {
        return ProjectStatus.valueOf(value.toUpperCase());
    }
}
