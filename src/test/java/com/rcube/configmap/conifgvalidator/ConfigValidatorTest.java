package com.rcube.configmap.conifgvalidator;

import com.rcube.configmap.TestDataUtil;
import com.rcube.configmap.operator.ConfigMapCustomResource;
import com.rcube.configmap.operator.CustomConfigMapSpec;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapVolumeSource;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.springboot.starter.test.EnableMockOperator;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@EnableMockOperator(
        crdPaths = "install/cluster/configmapcustomresources.rcube.com-v1.yml"
)
public class ConfigValidatorTest {

    @Autowired
    private KubernetesClient client;

    private static final Pair<String, String> TEST_DATA = TestDataUtil.validSchemaData();

    @Test
    public void contextLoads() {
        final CustomResourceDefinition customResourceDefinition = client.apiextensions()
                .v1()
                .customResourceDefinitions()
                .withName("configmapcustomresources.rcube.com")
                .get();

        assertNotNull(customResourceDefinition);
    }

    @Test
    public void testShouldCreateConfigMapFromCustomResource() {
        String testNS = "test-ns";
        client.namespaces().resource(namespace(testNS)).create();
        assertNotNull(client.namespaces().withName(testNS).get());
        // create a CR
        client.resources(ConfigMapCustomResource.class).inNamespace(testNS)
                .resource(getValidConfigSpec())
                .create();
        final ConfigMapCustomResource resource = getResource(testNS,"test-resource", ConfigMapCustomResource.class);

        assertNotNull(resource);
        assertEquals(TEST_DATA.getRight(), resource.getSpec().getConfig().getData().get("test.json"));
        assertEquals(TEST_DATA.getLeft(), resource.getSpec().getConfig().getSchema().get("test.json"));

        // test if a configmap was created
        await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(
                        () -> assertNotNull(
                                client.resources(ConfigMap.class)
                                        .inNamespace(testNS)
                                        .withName("test-resource").get()));
    }

    @Test
    public void testShouldCreateVersionedConfigMapFromCustomResource() throws URISyntaxException {
        final String nameSpace = "local";
        final String deploymentName = "nginx-deployment";
        client.load(fetchResource("app-deployment.yaml"))
                .create();
        await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(
                        () ->
                        {
                            final Deployment resource = getResource(nameSpace, deploymentName, Deployment.class);
                            assertNotNull(resource);
                            resource.getSpec()
                                    .getTemplate()
                                    .getSpec()
                                    .getVolumes()
                                    .stream()
                                    .map(Volume::getConfigMap)
                                    .filter(Objects::nonNull)
                                    .map(ConfigMapVolumeSource::getName)
                                    .forEach(requestedConfigMaps -> {
                                        System.out.println(requestedConfigMaps);
                                        assertNotNull(getResource(nameSpace, requestedConfigMaps, ConfigMap.class));
                                    });
                        });
    }

    private ConfigMapCustomResource getValidConfigSpec() {
        final ObjectMeta objectMeta = new ObjectMeta();
        objectMeta.setName("test-resource");

        CustomConfigMapSpec.CustomConfigMap configMap = new CustomConfigMapSpec.CustomConfigMap(Map.of("test.json", TEST_DATA.getLeft()));
        configMap.setData(Map.of("test.json", TEST_DATA.getRight()));
        ConfigMapCustomResource resource = new ConfigMapCustomResource();
        resource.setSpec(new CustomConfigMapSpec(configMap));
        resource.setMetadata(objectMeta);
        return resource;
    }

    private <T extends HasMetadata> T getResource(final String nameSpace,
                                                  final String name,
                                                  final Class<T> resourceType) {
        return client.resources(resourceType)
                .inNamespace(nameSpace)
                .withName(name).get();
    }

    private Namespace namespace(String ns) {
        return new NamespaceBuilder()
                .withMetadata(
                        new ObjectMetaBuilder().withName(ns).build())
                .build();
    }

    private InputStream fetchResource(String fileName) {
        return ConfigValidatorTest.class.getClassLoader().getResourceAsStream(fileName);
    }

}
