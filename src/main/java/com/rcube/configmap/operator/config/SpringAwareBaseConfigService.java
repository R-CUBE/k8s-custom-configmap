package com.rcube.configmap.operator.config;

import io.javaoperatorsdk.operator.api.config.Version;
import io.javaoperatorsdk.operator.api.reconciler.dependent.DependentResourceFactory;
import io.javaoperatorsdk.operator.springboot.starter.OverridableBaseConfigService;

public class SpringAwareBaseConfigService extends OverridableBaseConfigService {

    private DependentResourceFactory dependentResourceFactory = DependentResourceFactory.DEFAULT;

    public SpringAwareBaseConfigService(final Version version) {
        super(version);
    }

    public void setDependentResourceFactory(final DependentResourceFactory dependentResourceFactory) {
        this.dependentResourceFactory = dependentResourceFactory;
    }

    @Override
    public DependentResourceFactory dependentResourceFactory() {
        return dependentResourceFactory;
    }
}