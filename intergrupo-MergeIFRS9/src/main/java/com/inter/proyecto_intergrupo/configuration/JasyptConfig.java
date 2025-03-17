package com.inter.proyecto_intergrupo.configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.salt.RandomSaltGenerator;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

@Configuration
public class JasyptConfig {

    @Bean(name = "jasyptStringEncryptor")
    public PooledPBEStringEncryptor stringEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword("JAVASPRING"); // Asegúrate de que sea la misma que en application.properties
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setSaltGenerator(new RandomSaltGenerator());
        config.setStringOutputType("base64");
        encryptor.setConfig(config);
        return encryptor;
    }
}
