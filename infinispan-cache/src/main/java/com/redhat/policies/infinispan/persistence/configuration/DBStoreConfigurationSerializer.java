package com.redhat.policies.infinispan.persistence.configuration;

import org.infinispan.configuration.serializing.AbstractStoreSerializer;
import org.infinispan.configuration.serializing.ConfigurationSerializer;
import org.infinispan.configuration.serializing.XMLExtendedStreamWriter;

import javax.xml.stream.XMLStreamException;

public class DBStoreConfigurationSerializer extends AbstractStoreSerializer implements ConfigurationSerializer<DBStoreConfiguration> {

   @Override
   public void serialize(XMLExtendedStreamWriter writer, DBStoreConfiguration configuration) throws XMLStreamException {
      writer.writeStartElement(Element.DB_STORE);
      configuration.attributes().write(writer);
      writeCommonStoreSubAttributes(writer, configuration);
      writeCommonStoreElements(writer, configuration);
      writer.writeEndElement();
   }

}
