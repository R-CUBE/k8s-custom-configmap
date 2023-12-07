package com.rcube.configmap.validators;

import com.networknt.schema.ValidationMessage;
import com.rcube.configmap.operator.ConfigMapCustomResource;
import com.rcube.configmap.operator.ResourceStatus;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
@AllArgsConstructor
public class SpecValidator {
    private final JsonContentValidators jsonContentValidators;

    public void validatesResourceContent(final ConfigMapCustomResource resource) {
        try{
            final Map<String, String> schemaResource = resource.getSpec().getConfig().getSchema();
            final Map<String, String> dataResource = resource.getSpec().getConfig().getData();

            if (CollectionUtils.isEmpty(schemaResource) || CollectionUtils.isEmpty(dataResource)) {
                throw new RuntimeException("Missing required fields");
            }

            final Set<String> dataKeys = dataResource.keySet();
            final Set<String> schemaKeys = schemaResource.keySet();
            if (!dataKeys.containsAll(schemaKeys)) {
                dataKeys.removeAll(schemaKeys);
                throw new RuntimeException(String.format("Missing schemas for %s", dataKeys));
            }

            final Map<String, Set<ValidationMessage>> schemaValidationResult = new HashMap<>();
            dataResource.forEach((k, data) -> {
                final Set<ValidationMessage> validation = jsonContentValidators.validate(schemaResource.get(k), data);
                if(!CollectionUtils.isEmpty(validation)){
                    schemaValidationResult.put(k, validation);
                }
            });

            if (!CollectionUtils.isEmpty(schemaValidationResult)) {
                throw new RuntimeException(String.format("Invalid data format %s", schemaValidationResult));
            }
        }catch (Exception ex){
            resource.setStatus(new ResourceStatus(ex.getMessage()));
            throw ex;
        }
    }
}
