package org.magicalpanda.projectmanagementbackend.exception;

public class InvalidStateTransition extends BusinessException {
    public InvalidStateTransition(String message) {
        super(message);
    }
}
