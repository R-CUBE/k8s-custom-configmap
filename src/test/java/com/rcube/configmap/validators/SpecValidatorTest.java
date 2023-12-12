package com.rcube.configmap.validators;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.rcube.configmap.TestDataUtil;
import com.rcube.configmap.operator.ConfigMapCustomResource;
import com.rcube.configmap.operator.CustomConfigMapSpec;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SpecValidatorTest {

    @Test
    public void testSpecValidations() {
        SpecValidator validator = new SpecValidator(new JsonContentValidators(new ObjectMapper()));
        Assertions.assertDoesNotThrow(() -> validator.validateResourceContent(getValidConfigSpec()));
        Assertions.assertThrows(RuntimeException.class, () -> validator.validateResourceContent(getConfigSpecWithInvalidData()));
    }


    private ConfigMapCustomResource getValidConfigSpec(){
        final Pair<String, String> schemaData = TestDataUtil.validSchemaData();
        CustomConfigMapSpec.CustomConfigMap configMap = new CustomConfigMapSpec.CustomConfigMap(Map.of("test.json",schemaData.getLeft()));
        configMap.setData(Map.of("test.json",schemaData.getRight()));
        ConfigMapCustomResource resource = new ConfigMapCustomResource();
        resource.setSpec(new CustomConfigMapSpec(configMap));
        return resource;
    }

    private ConfigMapCustomResource getConfigSpecWithoutSchema(){
        CustomConfigMapSpec.CustomConfigMap configMap = new CustomConfigMapSpec.CustomConfigMap(Map.of());
        configMap.setData(Map.of("test.json",TestDataUtil.validSchemaData().getRight()));
        ConfigMapCustomResource resource = new ConfigMapCustomResource();
        resource.setSpec(new CustomConfigMapSpec(configMap));
        return resource;
    }

    private ConfigMapCustomResource getConfigSpecWithInvalidData(){
        final Pair<String, String> testData = TestDataUtil.validSchemaInvalidData();
        CustomConfigMapSpec.CustomConfigMap configMap = new CustomConfigMapSpec.CustomConfigMap(Map.of("test.json",testData.getLeft()));
        configMap.setData(Map.of("test.json",testData.getRight()));
        ConfigMapCustomResource resource = new ConfigMapCustomResource();
        resource.setSpec(new CustomConfigMapSpec(configMap));
        return resource;
    }


    private ConfigMapCustomResource getCustomResource(String fileName) throws Exception{
        try (InputStream inputStream = SpecValidatorTest.class.getClassLoader().getResourceAsStream(fileName)) {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(inputStream, ConfigMapCustomResource.class);
        }
    }

}
