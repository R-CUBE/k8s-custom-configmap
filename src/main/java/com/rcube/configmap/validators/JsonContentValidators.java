package com.rcube.configmap.validators;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@AllArgsConstructor
public class JsonContentValidators {

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public Set<ValidationMessage> validate(final String schema, final String jsonData) {
        final JsonSchema jsonSchema = getJsonSchemaFromStringContent(schema);
        final JsonNode node = getJsonNodeFromStringContent(jsonData);
        return jsonSchema.validate(node);
    }

    private JsonSchema getJsonSchemaFromStringContent(String schemaContent) {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
        return factory.getSchema(schemaContent);
    }

    private JsonNode getJsonNodeFromStringContent(String content) throws JsonProcessingException {
        return objectMapper.readTree(content);
    }

}
