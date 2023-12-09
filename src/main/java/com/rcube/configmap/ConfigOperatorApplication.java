package com.rcube.configmap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.fabric8.kubernetes.client.utils.Serialization;
import io.javaoperatorsdk.operator.monitoring.micrometer.MicrometerMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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
    public MicrometerMetrics micrometerMetrics(final MeterRegistry meterRegistry){
        return MicrometerMetrics.withoutPerResourceMetrics(meterRegistry);
    }

    public static void main(String[] args) {
        SpringApplication.run(ConfigOperatorApplication.class, args);
    }
}
