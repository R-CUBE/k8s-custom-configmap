package com.rcube.configmap.operator.dependentresource;

import com.rcube.configmap.operator.ConfigMapCustomResource;
import com.rcube.configmap.validators.SpecValidator;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfigMapDependentResource extends CRUDKubernetesDependentResource<ConfigMap, ConfigMapCustomResource> {

    @Autowired
    private SpecValidator specValidator;

    public ConfigMapDependentResource() {
        super(ConfigMap.class);
    }

    @Override
    protected ConfigMap desired(final ConfigMapCustomResource primary, final Context<ConfigMapCustomResource> context) {
        specValidator.validateResourceContent(primary);
        return createConfigMap(primary);
    }

    private ConfigMap createConfigMap(final ConfigMapCustomResource resource) {
        val configMapData = resource.getSpec().getConfig();
        val configMap = buildConfigMap(resource);
        configMap.setData(configMapData.getData());
        configMap.setAdditionalProperties(configMap.getAdditionalProperties());
        configMap.setBinaryData(configMapData.getBinaryData());
        configMap.setImmutable(configMapData.getImmutable());
        return configMap;
    }
    private ConfigMap buildConfigMap(final ConfigMapCustomResource resource) {
        return new ConfigMapBuilder()
                .withNewMetadata()
                .withName(resource.getMetadata().getName())
                .withNamespace(resource.getMetadata().getNamespace())
                .withLabels(resource.getMetadata().getLabels())
                .addNewOwnerReference()
                .withController(true)
                .withKind(resource.getKind())
                .withApiVersion(resource.getApiVersion())
                .withName(resource.getMetadata().getName())
                .withUid(resource.getMetadata().getUid())
                .endOwnerReference()
                .endMetadata()
                .build();
    }
}
