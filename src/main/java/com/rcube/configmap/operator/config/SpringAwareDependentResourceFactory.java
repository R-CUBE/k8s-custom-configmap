package com.rcube.configmap.operator.config;

import io.javaoperatorsdk.operator.api.config.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.config.dependent.DependentResourceConfigurationResolver;
import io.javaoperatorsdk.operator.api.config.dependent.DependentResourceSpec;
import io.javaoperatorsdk.operator.api.reconciler.dependent.DependentResource;
import io.javaoperatorsdk.operator.api.reconciler.dependent.DependentResourceFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SpringAwareDependentResourceFactory implements DependentResourceFactory<ControllerConfiguration<?>> {

    private final Map<String, DependentResource> dependentResourcesContainer;

    public SpringAwareDependentResourceFactory(final List<DependentResource> dependentResources) {
        this.dependentResourcesContainer = buildDependentResourcesContainer(dependentResources);
    }

    @Override
    public DependentResource createFrom(final DependentResourceSpec spec, final ControllerConfiguration<?> configuration) {
        final DependentResource dependentResourceFromSpringContext = dependentResourcesContainer.get(spec.getDependentResourceClass().getName());
        if (dependentResourceFromSpringContext == null) {
            return DependentResourceFactory.DEFAULT.createFrom(spec, configuration);
        }
        DependentResourceConfigurationResolver.configure(dependentResourceFromSpringContext, spec, configuration);
        return dependentResourceFromSpringContext;
    }

    private Map<String, DependentResource> buildDependentResourcesContainer(List<DependentResource> dependentResources) {
        return dependentResources.stream()
                .collect(Collectors.toMap(dependentResource -> dependentResource.getClass().getName(), Function.identity()));
    }
}
