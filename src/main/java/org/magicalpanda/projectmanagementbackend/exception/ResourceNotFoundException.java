package org.magicalpanda.projectmanagementbackend.exception;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resourceName, Object id) {
        super(String.format("Resource %s not found with id %s", resourceName, id));
    }
}
