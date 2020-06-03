package com.redhat.policies.infinispan.persistence.configuration;

import com.redhat.policies.infinispan.persistence.DBStore;
import org.infinispan.commons.configuration.BuiltBy;
import org.infinispan.commons.configuration.ConfigurationFor;
import org.infinispan.commons.configuration.attributes.Attribute;
import org.infinispan.commons.configuration.attributes.AttributeDefinition;
import org.infinispan.commons.configuration.attributes.AttributeSet;
import org.infinispan.commons.configuration.attributes.CollectionAttributeCopier;
import org.infinispan.commons.configuration.elements.DefaultElementDefinition;
import org.infinispan.commons.configuration.elements.ElementDefinition;
import org.infinispan.configuration.cache.AbstractStoreConfiguration;
import org.infinispan.configuration.cache.AsyncStoreConfiguration;
import org.infinispan.configuration.serializing.SerializedWith;

import java.util.HashMap;
import java.util.Map;

@BuiltBy(DBStoreConfigurationBuilder.class)
@ConfigurationFor(DBStore.class)
@SerializedWith(DBStoreConfigurationSerializer.class)
public class DBStoreConfiguration extends AbstractStoreConfiguration {

   static final AttributeDefinition<String> PERSISTENCE_UNIT_NAME = AttributeDefinition.builder("persistenceUnitName", null, String.class).immutable().xmlName("persistence-unit").build();
   static final AttributeDefinition<Boolean> STORE_METADATA = AttributeDefinition.builder("storeMetadata", true).immutable().build();
   static final AttributeDefinition<Map<String, Class<?>>> ENTITIES = AttributeDefinition.builder("entities", null, (Class<Map<String, Class<?>>>) (Class<?>) Map.class)
           .copier(CollectionAttributeCopier.INSTANCE)
           .initializer(HashMap::new).immutable().build();

   // TODO Enable later for key -> key-prefix transformer
//   static final AttributeDefinition<Map<Class<?>, Class<?>>> KEY_TRANSFORMERS = AttributeDefinition.builder("key-transformers", null, (Class<Map<Class<?>, Class<?>>>) (Class<?>) Map.class)
//           .copier(CollectionAttributeCopier.INSTANCE)
//           .initializer(HashMap::new).immutable().build();


   public static AttributeSet attributeDefinitionSet() {
      return new AttributeSet(DBStoreConfiguration.class, AbstractStoreConfiguration.attributeDefinitionSet(), PERSISTENCE_UNIT_NAME, STORE_METADATA);
   }

   static ElementDefinition ELEMENT_DEFINITION = new DefaultElementDefinition(Element.DB_STORE.getLocalName());

   private final Attribute<String> persistenceUnitName;
   private final Attribute<Boolean> storeMetadata;
   private final Attribute<Map<String, Class<?>>> keyPrefixTable;

   protected DBStoreConfiguration(AttributeSet attributes, AsyncStoreConfiguration async) {
      super(attributes, async);
      persistenceUnitName = attributes.attribute(PERSISTENCE_UNIT_NAME);
      storeMetadata = attributes.attribute(STORE_METADATA);
      keyPrefixTable = attributes.attribute(ENTITIES);
   }

   @Override
   public ElementDefinition getElementDefinition() {
      return ELEMENT_DEFINITION;
   }

   public String persistenceUnitName() {
      return persistenceUnitName.get();
   }

   public boolean storeMetadata() {
      return storeMetadata.get();
   }

   public Map<String, Class<?>> prefixToClass() {
      return keyPrefixTable.get();
   }
}
