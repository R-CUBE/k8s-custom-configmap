package com.rcube.configmap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.rcube.configmap.operator.config.SpringAwareBaseConfigService;
import com.rcube.configmap.operator.config.SpringAwareDependentResourceFactory;
import io.fabric8.kubernetes.client.utils.Serialization;
import io.javaoperatorsdk.operator.api.config.ConfigurationService;
import io.javaoperatorsdk.operator.api.config.ResourceClassResolver;
import io.javaoperatorsdk.operator.api.config.Utils;
import io.javaoperatorsdk.operator.api.monitoring.Metrics;
import io.javaoperatorsdk.operator.api.reconciler.dependent.DependentResource;
import io.javaoperatorsdk.operator.api.reconciler.dependent.DependentResourceFactory;
import io.javaoperatorsdk.operator.monitoring.micrometer.MicrometerMetrics;
import io.javaoperatorsdk.operator.springboot.starter.OperatorConfigurationProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;

@SpringBootApplication
public class ConfigOperatorApplication {

    static {
        Serialization.jsonMapper().findAndRegisterModules()
                .registerModule(new GuavaModule())
                .registerModule(new Jdk8Module())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                        false)
                .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    @Bean
    public MicrometerMetrics micrometerMetrics(final MeterRegistry meterRegistry) {
        return MicrometerMetrics.withoutPerResourceMetrics(meterRegistry);
    }


    @Bean
    public DependentResourceFactory dependentResourceFactory(final List<DependentResource> dependentResources) {
        return new SpringAwareDependentResourceFactory(dependentResources);
    }

    // Primary annotation is to ignore ConfigurationService auto configuration in
    // io.javaoperatorsdk.operator.springboot.starter.OperatorAutoConfiguration
    // and use SpringAwareBaseConfigService as the primary configuration service
    @Bean
    @Primary
    public ConfigurationService customBaseConfigService(final ResourceClassResolver resourceClassResolver,
                                                        final Metrics metrics,
                                                        final OperatorConfigurationProperties configuration,
                                                        final DependentResourceFactory dependentResourceFactory) {
        final SpringAwareBaseConfigService configService =
                new SpringAwareBaseConfigService(Utils.loadFromProperties());
        configService.setConcurrentReconciliationThreads(configuration.getConcurrentReconciliationThreads());
        configService.setMetrics(metrics);
        configService.setResourceClassResolver(resourceClassResolver);
        configService.setCheckCRDAndValidateLocalModel(configuration.getCheckCrdAndValidateLocalModel());
        configService.setDependentResourceFactory(dependentResourceFactory);
        return configService;
    }

    public static void main(String[] args) {
        SpringApplication.run(ConfigOperatorApplication.class, args);
    }
}
