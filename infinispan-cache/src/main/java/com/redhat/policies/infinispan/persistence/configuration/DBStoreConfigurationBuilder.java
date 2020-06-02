package com.redhat.policies.infinispan.persistence.configuration;

import com.redhat.policies.infinispan.persistence.DBStore;
import org.infinispan.commons.configuration.ConfigurationBuilderInfo;
import org.infinispan.commons.configuration.elements.ElementDefinition;
import org.infinispan.configuration.cache.AbstractStoreConfigurationBuilder;
import org.infinispan.configuration.cache.PersistenceConfigurationBuilder;

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


   @Deprecated
   public DBStoreConfigurationBuilder batchSize(long batchSize) {
      int size = batchSize > Integer.MAX_VALUE ? Integer.MAX_VALUE : (batchSize < Integer.MIN_VALUE ? Integer.MIN_VALUE : (int) batchSize);
      maxBatchSize(size);
      return self();
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
