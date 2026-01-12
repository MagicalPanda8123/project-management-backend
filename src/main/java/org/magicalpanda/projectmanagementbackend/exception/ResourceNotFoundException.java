package org.magicalpanda.projectmanagementbackend.exception;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Object id) {
        super(String.format("Resource %s not found with the provided identifier %s", resourceName, id));
    }
}
