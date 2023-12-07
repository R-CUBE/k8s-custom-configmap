package com.rcube.configmap.conifgvalidator;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.rcube.configmap.TestDataUtil;
import com.rcube.configmap.operator.ConfigMapCustomResource;
import com.rcube.configmap.operator.CustomConfigMapSpec;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.springboot.starter.test.EnableMockOperator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnableMockOperator(
        crdPaths = "install/cluster/configmapcustomresources.rcube.com-v1.yml"
)
public class ConfigValidatorTest {

    @Autowired
    private KubernetesClient client;

    private static Pair<String,String> TEST_DATA = TestDataUtil.validSchemaData();

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
    public void test() {
        String testNS = "test-ns";
        client.namespaces().resource(namespace(testNS)).create();
        assertNotNull(client.namespaces().withName(testNS).get());
        // create a CR
        client.resources(ConfigMapCustomResource.class).inNamespace(testNS)
                .resource(getValidConfigSpec())
                .create();
        final ConfigMapCustomResource resource = client.resources(ConfigMapCustomResource.class)
                .inNamespace(testNS)
                .withName("test-resource")
                .get();

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

    private Namespace namespace(String ns) {
        return new NamespaceBuilder()
                .withMetadata(
                        new ObjectMetaBuilder().withName(ns).build())
                .build();
    }

}
