package com.redhat.policies.infinispan.persistence.configuration;

import com.redhat.policies.infinispan.persistence.DBStore;
import org.infinispan.commons.configuration.BuiltBy;
import org.infinispan.commons.configuration.ConfigurationFor;
import org.infinispan.commons.configuration.attributes.Attribute;
import org.infinispan.commons.configuration.attributes.AttributeDefinition;
import org.infinispan.commons.configuration.attributes.AttributeSet;
import org.infinispan.commons.configuration.elements.DefaultElementDefinition;
import org.infinispan.commons.configuration.elements.ElementDefinition;
import org.infinispan.configuration.cache.AbstractStoreConfiguration;
import org.infinispan.configuration.cache.AsyncStoreConfiguration;
import org.infinispan.configuration.serializing.SerializedWith;

@BuiltBy(DBStoreConfigurationBuilder.class)
@ConfigurationFor(DBStore.class)
@SerializedWith(DBStoreConfigurationSerializer.class)
public class DBStoreConfiguration extends AbstractStoreConfiguration {

   static final AttributeDefinition<String> PERSISTENCE_UNIT_NAME = AttributeDefinition.builder("persistenceUnitName", null, String.class).immutable().xmlName("persistence-unit").build();
   static final AttributeDefinition<Boolean> STORE_METADATA = AttributeDefinition.builder("storeMetadata", true).immutable().build();

   public static AttributeSet attributeDefinitionSet() {
      return new AttributeSet(DBStoreConfiguration.class, AbstractStoreConfiguration.attributeDefinitionSet(), PERSISTENCE_UNIT_NAME, STORE_METADATA);
   }

   static ElementDefinition ELEMENT_DEFINITION = new DefaultElementDefinition(Element.DB_STORE.getLocalName());

   private final Attribute<String> persistenceUnitName;
   private final Attribute<Boolean> storeMetadata;

   protected DBStoreConfiguration(AttributeSet attributes, AsyncStoreConfiguration async) {
      super(attributes, async);
      persistenceUnitName = attributes.attribute(PERSISTENCE_UNIT_NAME);
      storeMetadata = attributes.attribute(STORE_METADATA);
   }

   @Override
   public ElementDefinition getElementDefinition() {
      return ELEMENT_DEFINITION;
   }

   public String persistenceUnitName() {
      return persistenceUnitName.get();
   }

   public long batchSize() {
      return attributes.attribute(MAX_BATCH_SIZE).get();
   }

   public boolean storeMetadata() {
      return storeMetadata.get();
   }
}
