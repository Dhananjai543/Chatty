package com.chatty.config;

import com.chatty.dto.MessageDTO;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.properties.security.protocol:PLAINTEXT}")
    private String securityProtocol;

    @Value("${spring.kafka.properties.sasl.mechanism:#{null}}")
    private String saslMechanism;

    @Value("${spring.kafka.properties.sasl.jaas.config:#{null}}")
    private String saslJaasConfig;

    @Value("${spring.kafka.ssl.trust-store-location:#{null}}")
    private String trustStoreLocation;

    @Value("${spring.kafka.ssl.trust-store-password:#{null}}")
    private String trustStorePassword;

    @Value("${spring.kafka.ssl.key-store-location:#{null}}")
    private String keyStoreLocation;

    @Value("${spring.kafka.ssl.key-store-password:#{null}}")
    private String keyStorePassword;

    @Value("${spring.kafka.ssl.key-password:#{null}}")
    private String keyPassword;

    @Bean
    public ConsumerFactory<String, MessageDTO> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.chatty.dto,com.chatty.entity");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, MessageDTO.class.getName());

        // Add security configuration
        if (securityProtocol != null && !securityProtocol.equals("PLAINTEXT")) {
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
            props.put("ssl.endpoint.identification.algorithm", "");

            // SASL configuration
            if (saslMechanism != null) {
                props.put(SaslConfigs.SASL_MECHANISM, saslMechanism);
            }
            if (saslJaasConfig != null) {
                props.put(SaslConfigs.SASL_JAAS_CONFIG, saslJaasConfig);
            }

            // SSL/TLS configuration (for client certificates)
            if (trustStoreLocation != null) {
                props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, trustStoreLocation.replace("file:", ""));
                props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, trustStorePassword);
            }
            if (keyStoreLocation != null) {
                props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keyStoreLocation.replace("file:", ""));
                props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, keyStorePassword);
                props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, keyPassword);
            }
        }

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MessageDTO> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, MessageDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
