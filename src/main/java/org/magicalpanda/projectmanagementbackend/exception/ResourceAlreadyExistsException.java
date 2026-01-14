package org.magicalpanda.projectmanagementbackend.exception;

public class ResourceAlreadyExistsException extends BusinessException {
    public ResourceAlreadyExistsException(String message) {
        super(message);
    }

    public ResourceAlreadyExistsException(String resourceName, String field, Object value) {
        super(String.format(
                "%s already exists with %s = %s",
                resourceName, field, value
        ));
    }
}
