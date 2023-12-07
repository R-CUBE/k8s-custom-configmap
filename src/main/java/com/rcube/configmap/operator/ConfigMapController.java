package com.rcube.configmap.operator;

import com.rcube.configmap.validators.SpecValidator;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.config.informer.InformerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.EventSourceContext;
import io.javaoperatorsdk.operator.api.reconciler.EventSourceInitializer;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.javaoperatorsdk.operator.processing.event.source.EventSource;
import io.javaoperatorsdk.operator.processing.event.source.informer.InformerEventSource;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
@ControllerConfiguration
public class ConfigMapController implements Reconciler<ConfigMapCustomResource>, EventSourceInitializer<ConfigMapCustomResource> {

    private final KubernetesClient kubernetesClient;

    private final SpecValidator specValidator;

    @Override
    public UpdateControl<ConfigMapCustomResource> reconcile(final ConfigMapCustomResource resource, final Context<ConfigMapCustomResource> context) {
        try {
            log.info("Context being reloaded for [{}]",context.getSecondaryResource(ConfigMap.class).map(map -> map.getMetadata().getName()).orElse("None"));
            specValidator.validatesResourceContent(resource);
            createConfigMap(resource);
            resource.setStatus(ResourceStatus.builder().build());
        } catch (Exception ex) {
            resource.setStatus(new ResourceStatus(ex.getMessage()));
        }
        return UpdateControl.updateResourceAndStatus(resource);
    }

    private void createConfigMap(final ConfigMapCustomResource resource) {
        val configMapData = resource.getSpec().getConfig();
        val configMap = buildConfigMap(resource);
        configMap.setData(configMapData.getData());
        configMap.setAdditionalProperties(configMap.getAdditionalProperties());
        configMap.setBinaryData(configMapData.getBinaryData());
        configMap.setImmutable(configMapData.getImmutable());
        kubernetesClient.configMaps()
                .inNamespace(resource.getMetadata().getNamespace())
                .resource(configMap)
                .createOrReplace();
    }

    private ConfigMap buildConfigMap(final ConfigMapCustomResource resource) {
        return new ConfigMapBuilder().withNewMetadata()
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

    @Override
    public Map<String, EventSource> prepareEventSources(final EventSourceContext<ConfigMapCustomResource> context) {
        InformerConfiguration<ConfigMap> configuration =
                InformerConfiguration.from(ConfigMap.class, context)
                        .build();
        return EventSourceInitializer
                .nameEventSources(new InformerEventSource<>(configuration, context));
    }
}
