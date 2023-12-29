package com.rcube.configmap.operator;

import com.rcube.configmap.operator.dependentresource.ConfigMapDependentResource;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.ErrorStatusHandler;
import io.javaoperatorsdk.operator.api.reconciler.ErrorStatusUpdateControl;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ControllerConfiguration(dependents = {
        @Dependent(type = ConfigMapDependentResource.class)
})
public class ConfigMapController implements Reconciler<ConfigMapCustomResource>,
        ErrorStatusHandler<ConfigMapCustomResource> {

    @Override
    public UpdateControl<ConfigMapCustomResource> reconcile(final ConfigMapCustomResource resource, final Context<ConfigMapCustomResource> context) {
        try {
            val reloadedResource = context.getSecondaryResource(ConfigMap.class)
                    .map(map -> map.getMetadata().getName())
                    .orElse("None");
            log.info("Context being reloaded for [{}]", reloadedResource);
            resource.setStatus(ResourceStatus.builder().
                    build());
        } catch (Exception ex) {
            resource.setStatus(new ResourceStatus(ex.getMessage()));
        }
        return UpdateControl.updateResourceAndStatus(resource);
    }

    @Override
    public ErrorStatusUpdateControl<ConfigMapCustomResource> updateErrorStatus(final ConfigMapCustomResource resource, final Context<ConfigMapCustomResource> context, final Exception e) {
        resource.setStatus(new ResourceStatus(e.getMessage()));
        return ErrorStatusUpdateControl.updateStatus(resource);
    }
}