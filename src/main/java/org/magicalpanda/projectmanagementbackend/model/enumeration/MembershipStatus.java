package org.magicalpanda.projectmanagementbackend.model.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum MembershipStatus {
    PENDING,
    ACTIVE,
    REJECTED,
    LEFT,
    DELETED;

    @JsonCreator
    public static MembershipStatus from(String value)
    {
        return MembershipStatus.valueOf(value.toUpperCase());
    }
}
