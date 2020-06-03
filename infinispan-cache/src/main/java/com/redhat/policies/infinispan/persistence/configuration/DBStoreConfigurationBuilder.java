package com.redhat.policies.infinispan.persistence.configuration;

import com.redhat.policies.infinispan.persistence.DBStore;
import org.infinispan.commons.configuration.ConfigurationBuilderInfo;
import org.infinispan.commons.configuration.elements.ElementDefinition;
import org.infinispan.configuration.cache.AbstractStoreConfigurationBuilder;
import org.infinispan.configuration.cache.PersistenceConfigurationBuilder;

import java.util.Map;

import static org.infinispan.configuration.cache.AbstractStoreConfiguration.SEGMENTED;
import static org.infinispan.util.logging.Log.CONFIG;

public class DBStoreConfigurationBuilder
      extends AbstractStoreConfigurationBuilder<DBStoreConfiguration, DBStoreConfigurationBuilder> implements ConfigurationBuilderInfo {

   public DBStoreConfigurationBuilder(PersistenceConfigurationBuilder builder) {
      super(builder, DBStoreConfiguration.attributeDefinitionSet());
   }

   public DBStoreConfigurationBuilder persistenceUnitName(String persistenceUnitName) {
      attributes.attribute(DBStoreConfiguration.PERSISTENCE_UNIT_NAME).set(persistenceUnitName);
      return self();
   }

   @Override
   public ElementDefinition getElementDefinition() {
      return DBStoreConfiguration.ELEMENT_DEFINITION;
   }


   public DBStoreConfigurationBuilder storeMetadata(boolean storeMetadata) {
      attributes.attribute(DBStoreConfiguration.STORE_METADATA).set(storeMetadata);
      return self();
   }

   @Override
   public void validate() {
      Boolean segmented = attributes.attribute(SEGMENTED).get();
      if (segmented == null || segmented) {
         throw CONFIG.storeDoesNotSupportBeingSegmented(DBStore.class.getSimpleName());
      }
      // how do you validate required attributes?
      super.validate();
   }

   public void addStoredEntity(String prefix, Class<?> targetClass) {
      Map<String, Class<?>> prefixToClassMap = attributes.attribute(DBStoreConfiguration.ENTITIES).get();
      prefixToClassMap.put(prefix, targetClass);
      attributes.attribute(DBStoreConfiguration.ENTITIES).set(prefixToClassMap);
   }

   @Override
   public DBStoreConfiguration create() {
      return new DBStoreConfiguration(attributes.protect(), async.create());
   }

   @Override
   public DBStoreConfigurationBuilder read(DBStoreConfiguration template) {
      super.read(template);
      return this;
   }

   @Override
   public DBStoreConfigurationBuilder self() {
      return this;
   }
}
