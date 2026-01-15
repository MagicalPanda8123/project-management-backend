package org.magicalpanda.projectmanagementbackend.exception;

public class InvalidaProjectStateTransition extends BusinessException {
    public InvalidaProjectStateTransition(String message) {
        super(message);
    }
}
