package com.redhat.cloud.policies.infinispan.persistence.configuration;

import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.StoreConfiguration;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.configuration.parsing.ParserRegistry;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ConfigurationTest {

    @Test
    public void testXmlConfig() throws IOException {
        URL config = ConfigurationTest.class.getResource("/config/example-config.xml");
        ConfigurationBuilderHolder configHolder = new ParserRegistry().parse(config);

        // These are generic Infinispan config checks - test them to ensure we didn't break them
        Configuration cacheConfig = configHolder.getNamedConfigurationBuilders().get("testCache").build();
        assertFalse(cacheConfig.persistence().passivation());
        assertEquals(cacheConfig.persistence().stores().size(), 1);

        StoreConfiguration cacheLoaderConfig = cacheConfig.persistence().stores().get(0);
        assertFalse(cacheLoaderConfig.shared());
        assertTrue(cacheLoaderConfig.preload());
        assertTrue(cacheLoaderConfig instanceof DBStoreConfiguration);

        // Test our own parsing
        DBStoreConfiguration dbConfig = (DBStoreConfiguration) cacheLoaderConfig;
        assertEquals("infinispan-engine", dbConfig.persistenceUnitName());
        assertEquals(2, dbConfig.getEntities().size());

        // Test that property was correctly parsed by the Infinispan after our config
        assertEquals(1, dbConfig.properties().size());
        assertEquals("com.redhat.cloud.policies.infinispan.persistence.TestKeyTransformer", dbConfig.properties().get("key-transformer"));
    }
}
