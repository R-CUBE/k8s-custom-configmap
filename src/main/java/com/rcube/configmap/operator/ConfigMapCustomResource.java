package com.rcube.configmap.operator;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.ShortNames;
import io.fabric8.kubernetes.model.annotation.Version;

@Group("rcube.com")
@Version("v1")
@ShortNames("ctmap")
public class ConfigMapCustomResource extends CustomResource<CustomConfigMapSpec,ResourceStatus> implements Namespaced {
}
