package org.magicalpanda.projectmanagementbackend.model.enumeration;

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
}
