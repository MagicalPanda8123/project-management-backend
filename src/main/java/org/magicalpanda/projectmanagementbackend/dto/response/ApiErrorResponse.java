package org.magicalpanda.projectmanagementbackend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final Instant timestamp;
    private final Map<String, String> validationErrors;

}
