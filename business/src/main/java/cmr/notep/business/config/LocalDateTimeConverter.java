package cmr.notep.business.config;

import org.dozer.CustomConverter;

import java.time.LocalDateTime;

public class LocalDateTimeConverter implements CustomConverter {
    @Override
    public Object convert(Object existingDestinationFieldValue, Object sourceFieldValue, Class<?> destinationClass, Class<?> sourceClass) {
        if (sourceFieldValue == null) {
            return null;
        }

        // Handle LocalDateTime mapping
        if (sourceFieldValue instanceof LocalDateTime) {
            return sourceFieldValue; // Return the same instance (copy-by-reference behavior)
        }

        return null;
    }
}