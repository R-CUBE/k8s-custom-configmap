package com.rcube.configmap.validators;

import com.networknt.schema.ValidationMessage;
import com.rcube.configmap.operator.ConfigMapCustomResource;
import com.rcube.configmap.operator.ResourceStatus;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
@AllArgsConstructor
public class SpecValidator {
    private final JsonContentValidators jsonContentValidators;

    public void validateResourceContent(final ConfigMapCustomResource resource) {
        try {
            final Map<String, String> schemaResource = resource.getSpec().getConfig().getSchema();

            if (schemaResource == null || CollectionUtils.isEmpty(schemaResource)) {
                log.warn("Resource [{}] does not have a schema, it is recommended to have one", resource.getMetadata().getName());
                return;
            }

            final Map<String, String> dataResource = resource.getSpec().getConfig().getData();

            if (CollectionUtils.isEmpty(dataResource)) {
                throw new RuntimeException("Missing required fields in the data configuration.");
            }
            final Map<String, Set<ValidationMessage>> schemaValidationResult = getValidationFailures(dataResource, schemaResource);
            if (!CollectionUtils.isEmpty(schemaValidationResult)) {
                throw new RuntimeException(String.format("Invalid data format %s", schemaValidationResult));
            }

        } catch (Exception ex) {
            resource.setStatus(new ResourceStatus(ex.getMessage()));
            throw ex;
        }
    }

    @NotNull
    private Map<String, Set<ValidationMessage>> getValidationFailures(final Map<String, String> dataResource,
                                                                      final Map<String, String> schemaResource) {
        final Map<String, Set<ValidationMessage>> schemaValidationResult = new HashMap<>();
        dataResource.forEach((key, data) -> {
            if (schemaResource.containsKey(key)) {
                final Set<ValidationMessage> validation = jsonContentValidators.validate(schemaResource.get(key), data);
                if (!CollectionUtils.isEmpty(validation)) {
                    schemaValidationResult.put(key, validation);
                }
            }
        });
        return schemaValidationResult;
    }
}
