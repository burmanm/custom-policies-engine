package com.redhat.cloud.policies.infinispan.persistence.configuration;

import org.infinispan.commons.configuration.ConfigurationBuilderInfo;
import org.infinispan.commons.util.Util;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.configuration.parsing.ConfigurationParser;
import org.infinispan.configuration.parsing.Namespace;
import org.infinispan.configuration.parsing.ParseUtils;
import org.infinispan.configuration.parsing.Parser;
import org.infinispan.configuration.parsing.XMLExtendedStreamReader;
import org.kohsuke.MetaInfServices;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

@MetaInfServices

@Namespace(root = "db-store")
@Namespace(uri = "urn:infinispan:config:store:db:*", root = "db-store")
public class DBStoreConfigurationParser implements ConfigurationParser {
   @Override
   public void readElement(XMLExtendedStreamReader reader,
                           ConfigurationBuilderHolder holder) throws XMLStreamException {
      Element element = Element.forName(reader.getLocalName());
      switch (element) {
         case DB_STORE: {
            ConfigurationBuilder builder = holder.getCurrentConfigurationBuilder();
            parseDBCacheStore(reader, builder.persistence().addStore(DBStoreConfigurationBuilder.class), holder.getClassLoader());
            break;
         }
         default: {
            throw ParseUtils.unexpectedElement(reader);
         }
      }
   }

   private void parseDBCacheStore(XMLExtendedStreamReader reader, DBStoreConfigurationBuilder builder, ClassLoader classLoader)
         throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = reader.getAttributeValue(i);
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));

         switch (attribute) {
            case PERSISTENCE_UNIT_NAME: {
               builder.persistenceUnitName(value);
               break;
            }
            default: {
               Parser.parseStoreAttribute(reader, i, builder);
            }
         }
      }

      while(reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch(element) {
            case ENTITIES:
               parseEntities(reader, builder, classLoader);
               break;
            default:
               Parser.parseStoreElement(reader, builder);
         }
      }
   }

   private void parseEntities(XMLExtendedStreamReader reader, DBStoreConfigurationBuilder builder, ClassLoader classLoader) throws XMLStreamException {
      while(reader.hasNext() && reader.nextTag() != 2) {
         Element element = Element.forName(reader.getLocalName());
         switch(element) {
            case ENTITY:
               String className = reader.getElementText();
               Class<?> storedEntity = Util.loadClass(className, classLoader);
               builder.addStoredEntity(storedEntity);
               break;
            default:
               throw ParseUtils.unexpectedElement(reader);
         }
      }
   }

   @Override
   public Namespace[] getNamespaces() {
      return ParseUtils.getNamespaceAnnotations(getClass());
   }

   @Override
   public Class<? extends ConfigurationBuilderInfo> getConfigurationBuilderInfo() {
      return DBStoreConfigurationBuilder.class;
   }
}
