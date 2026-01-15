package org.magicalpanda.projectmanagementbackend.dto.converter;

import org.magicalpanda.projectmanagementbackend.dto.enumeration.ProjectStatusFilter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ProjectStatusFilterConverter
        implements Converter<String, ProjectStatusFilter> {

    @Override
    public ProjectStatusFilter convert(String source) {
        try {
            return ProjectStatusFilter.valueOf(source.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Invalid status value: " + source
            );
        }
    }
}
