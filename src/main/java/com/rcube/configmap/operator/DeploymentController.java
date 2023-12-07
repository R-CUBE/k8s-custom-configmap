package com.rcube.configmap.operator;

import io.fabric8.kubernetes.api.model.ConfigMapVolumeSource;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
@ControllerConfiguration(labelSelector = "config-map-version")
public class DeploymentController implements Reconciler<Deployment> {

    private final KubernetesClient kubernetesClient;

    @Override
    public UpdateControl<Deployment> reconcile(Deployment resource, Context<Deployment> context) {
        log.info("Running for deployment [{}]", context.getControllerConfiguration());

        final String namespace = resource.getMetadata().getNamespace();
        final String configMapVersionPattern = computeRegexPattern(resource.getMetadata().getLabels().get("config-map-version"));

        final Set<String> requiredConfigMapNames = resource.getSpec()
                .getTemplate()
                .getSpec()
                .getVolumes()
                .stream()
                .map(Volume::getConfigMap)
                .filter(Objects::nonNull)
                .map(ConfigMapVolumeSource::getName)
                .collect(Collectors.toSet());

        final Set<String> requiredProjectedConfigMaps = resource.getSpec()
                .getTemplate()
                .getSpec()
                .getVolumes()
                .stream()
                .map(Volume::getProjected)
                .filter(Objects::nonNull)
                .flatMap(projectedResource -> projectedResource.getSources().stream())
                .map(pv -> pv.getConfigMap().getName())
                .collect(Collectors.toSet());

        requiredConfigMapNames.addAll(requiredProjectedConfigMaps);

        if (!CollectionUtils.isEmpty(requiredConfigMapNames)) {
            requiredConfigMapNames.stream()
                    .filter(configMapName -> !isConfigResourcePresent(namespace, configMapName))
                    .forEach(configMapName -> create(namespace, configMapName, configMapVersionPattern));
        }

        return UpdateControl.updateResource(resource);
    }

    private void create(final String nameSpace, final String configMapName,
                        final String configMapVersionPattern) {
        log.info("To be created [{}] in namespace [{}]", configMapName, nameSpace);
        final ConfigMapCustomResource configMapCustomResource = kubernetesClient.resources(
                        ConfigMapCustomResource.class)
                .inNamespace(nameSpace)
                .withName(configMapName.replaceFirst(configMapVersionPattern, ""))
                .get();

        if (configMapCustomResource != null) {
            configMapCustomResource.getMetadata()
                    .setName(configMapName);
            kubernetesClient.resources(ConfigMapCustomResource.class)
                    .inNamespace(nameSpace)
                    .createOrReplace(configMapCustomResource);
        }
    }

    private boolean isConfigResourcePresent(final String nameSpace, final String configMapName) {

        final ConfigMapCustomResource configResource = kubernetesClient.resources(
                        ConfigMapCustomResource.class)
                .inNamespace(nameSpace)
                .withName(configMapName)
                .get();

        return configResource != null;
    }

    private String computeRegexPattern(final String requestedConfigMapVersion) {
        //example for version `main-34` the method would return `(-main-34)$`
        return "(-" + requestedConfigMapVersion + ")$";
    }

}
