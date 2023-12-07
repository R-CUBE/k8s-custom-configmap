package com.rcube.configmap.operator;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.fabric8.kubernetes.api.model.ConfigMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.With;

@Value
@With
@AllArgsConstructor(onConstructor = @__({@JsonCreator}))
public class CustomConfigMapSpec {
    CustomConfigMap config;

    @Value
    @AllArgsConstructor(onConstructor = @__({@JsonCreator}))
    public static class CustomConfigMap extends ConfigMap {
        @NonNull
        Map<String,String> schema;
    }
}
