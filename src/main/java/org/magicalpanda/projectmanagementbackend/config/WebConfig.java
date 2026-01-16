package org.magicalpanda.projectmanagementbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

/**
 * Enables Spring Data web support with stable JSON serialization for paginated responses.
 *
 * Page<T> is a framework type of which the default JSON structure is not guaranteed to be
 * stable across Spring versions. This configuration forces Spring to serialize
 * Page<T> through a DTO-based format, preventing API clients from depending on
 * Spring internals.
 *
 * This applies globally to all controller endpoints returning Page<T> and only
 * affects JSON serialization, not repository or business logic.
 */

@Configuration
@EnableSpringDataWebSupport(
        pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO
)
public class WebConfig {
}
