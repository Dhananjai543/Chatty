package com.chatty.config;

import com.chatty.dto.MessageDTO;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

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
    public ProducerFactory<String, MessageDTO> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);

        // Add security configuration
        if (securityProtocol != null && !securityProtocol.equals("PLAINTEXT")) {
            configProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
            configProps.put("ssl.endpoint.identification.algorithm", "");

            // SASL configuration
            if (saslMechanism != null) {
                configProps.put(SaslConfigs.SASL_MECHANISM, saslMechanism);
            }
            if (saslJaasConfig != null) {
                configProps.put(SaslConfigs.SASL_JAAS_CONFIG, saslJaasConfig);
            }

            // SSL/TLS configuration (for client certificates)
            if (trustStoreLocation != null) {
                configProps.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, trustStoreLocation.replace("file:", ""));
                configProps.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, trustStorePassword);
            }
            if (keyStoreLocation != null) {
                configProps.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keyStoreLocation.replace("file:", ""));
                configProps.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, keyStorePassword);
                configProps.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, keyPassword);
            }
        }
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, MessageDTO> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
