package com.redhat.cloud.policies.infinispan.persistence.configuration;

import com.redhat.cloud.policies.infinispan.persistence.DBStore;
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

import java.util.HashSet;
import java.util.Set;

@BuiltBy(DBStoreConfigurationBuilder.class)
@ConfigurationFor(DBStore.class)
@SerializedWith(DBStoreConfigurationSerializer.class)
public class DBStoreConfiguration extends AbstractStoreConfiguration {

   public static final String KEY_TRANSFORMER_PROPERTY = "key-transformer";

   static final AttributeDefinition<String> PERSISTENCE_UNIT_NAME = AttributeDefinition.builder("persistenceUnitName", null, String.class).immutable().xmlName("persistence-unit").build();
   // Set is fine
   public static final AttributeDefinition<Set<Class<?>>> ENTITIES = AttributeDefinition.builder("entities", null, (Class<Set<Class<?>>>) (Class<?>) Set.class)
           .copier(CollectionAttributeCopier.INSTANCE)
           .initializer(HashSet::new).immutable().build();

   // TODO Needs key-transformer property

   public static AttributeSet attributeDefinitionSet() {
      return new AttributeSet(DBStoreConfiguration.class, AbstractStoreConfiguration.attributeDefinitionSet(), PERSISTENCE_UNIT_NAME, ENTITIES);
   }

   static ElementDefinition ELEMENT_DEFINITION = new DefaultElementDefinition(Element.DB_STORE.getLocalName());

   private final Attribute<String> persistenceUnitName;
   private final Attribute<Set<Class<?>>> entitiesTable;

   protected DBStoreConfiguration(AttributeSet attributes, AsyncStoreConfiguration async) {
      super(attributes, async);
      persistenceUnitName = attributes.attribute(PERSISTENCE_UNIT_NAME);
      entitiesTable = attributes.attribute(ENTITIES);
   }

   @Override
   public ElementDefinition getElementDefinition() {
      return ELEMENT_DEFINITION;
   }

   public String persistenceUnitName() {
      return persistenceUnitName.get();
   }

   public Set<Class<?>> getEntities() {
      return entitiesTable.get();
   }
}
