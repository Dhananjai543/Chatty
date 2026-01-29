package com.chatty.config;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topics.public-messages}")
    private String publicMessagesTopic;

    @Value("${kafka.topics.private-messages}")
    private String privateMessagesTopic;

    @Value("${kafka.topics.notifications}")
    private String notificationsTopic;

    @Value("${kafka.partitions:3}")
    private int partitions;

    @Value("${kafka.replicas:1}")
    private int replicas;

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
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Add security configuration
        if (securityProtocol != null && !securityProtocol.equals("PLAINTEXT")) {
            configs.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
            configs.put("ssl.endpoint.identification.algorithm", "");

            // SASL configuration
            if (saslMechanism != null) {
                configs.put(SaslConfigs.SASL_MECHANISM, saslMechanism);
            }
            if (saslJaasConfig != null) {
                configs.put(SaslConfigs.SASL_JAAS_CONFIG, saslJaasConfig);
            }

            // SSL/TLS configuration (for client certificates)
            if (trustStoreLocation != null) {
                configs.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, trustStoreLocation.replace("file:", ""));
                configs.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, trustStorePassword);
            }
            if (keyStoreLocation != null) {
                configs.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keyStoreLocation.replace("file:", ""));
                configs.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, keyStorePassword);
                configs.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, keyPassword);
            }
        }

        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic publicMessagesTopic() {
        return TopicBuilder.name(publicMessagesTopic)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }

    @Bean
    public NewTopic privateMessagesTopic() {
        return TopicBuilder.name(privateMessagesTopic)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }

    @Bean
    public NewTopic notificationsTopic() {
        return TopicBuilder.name(notificationsTopic)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }
}
