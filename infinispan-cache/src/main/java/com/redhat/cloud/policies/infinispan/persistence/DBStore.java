package com.redhat.cloud.policies.infinispan.persistence;

import com.redhat.cloud.policies.infinispan.persistence.configuration.DBStoreConfiguration;
import com.redhat.cloud.policies.infinispan.persistence.impl.EntityManagerFactoryRegistry;
import com.redhat.cloud.policies.infinispan.persistence.impl.KeyTransformer;
import com.redhat.cloud.policies.infinispan.persistence.impl.MetadataEntity;
import com.redhat.cloud.policies.infinispan.persistence.impl.MetadataEntityKey;
import com.redhat.cloud.policies.infinispan.persistence.impl.Stats;
import org.infinispan.commons.configuration.ConfiguredBy;
import org.infinispan.commons.persistence.Store;
import org.infinispan.commons.util.Util;
import org.infinispan.marshall.persistence.PersistenceMarshaller;
import org.infinispan.metadata.Metadata;
import org.infinispan.persistence.spi.AdvancedLoadWriteStore;
import org.infinispan.persistence.spi.InitializationContext;
import org.infinispan.persistence.spi.MarshallableEntry;
import org.infinispan.persistence.spi.MarshallableEntryFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.concurrent.Executor;

@Store
@ConfiguredBy(DBStoreConfiguration.class)
public class DBStore<K, V> implements AdvancedLoadWriteStore<K,V> {

    private Stats stats = new Stats();
    private DBStoreConfiguration configuration;
    private EntityManagerFactory emf;
    private EntityManagerFactoryRegistry emfRegistry;
    private PersistenceMarshaller marshaller;
    private MarshallableEntryFactory<K,V> marshallerEntryFactory;
    private Set<Class<?>> entities;
    private KeyTransformer keyTransformer;

    @Override
    public int size() {
        // Calculate the combined size of all tables?
        int size = 0;
        for (Class<?> entity : entities) {
            // Add to size
        }

        return size;
    }

    @Override
    public void clear() {

    }

    @Override
    public void purge(Executor executor, PurgeListener<? super K> purgeListener) {

    }

    @Override
    public void init(InitializationContext initializationContext) {
        this.configuration = initializationContext.getConfiguration();
        this.emfRegistry = initializationContext.getCache().getAdvancedCache().getComponentRegistry().getGlobalComponentRegistry().getComponent(EntityManagerFactoryRegistry.class);
        this.marshallerEntryFactory = initializationContext.getMarshallableEntryFactory();
        this.marshaller = initializationContext.getPersistenceMarshaller();
        this.entities = this.configuration.getEntities();

        String keyTransformerProperty = (String) this.configuration.properties().get(DBStoreConfiguration.KEY_TRANSFORMER_PROPERTY);
        Class<?> transformer = Util.loadClass(keyTransformerProperty, this.getClass().getClassLoader());
        try {
            this.keyTransformer = (KeyTransformer) transformer.getDeclaredConstructors()[0].newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(MarshallableEntry<? extends K, ? extends V> entry) {
        EntityManager em = emf.createEntityManager();

        Object entity = entry.getValue();
        MetadataEntity metadata = new MetadataEntity(entry);
        try {
            EntityTransaction txn = em.getTransaction();

            try {
                txn.begin();

                em.merge(entity);

                if (metadata.hasBytes()) {
                    em.merge(metadata);
                }

                txn.commit();
            } catch (Exception e) {
                throw new RuntimeException("Exception caught in write()", e);
            } finally {
                if (txn != null && txn.isActive())
                    txn.rollback();
            }
        } finally {
            em.close();
        }
    }

    public Class<?> findEntityClass(Object key) {
        return keyTransformer.keyToClass(key);
    }

    public Object findEntityId(Object key) {
        return keyTransformer.keyToID(key);
    }

    public Object findEntity(EntityManager em, Object key) {
        Class<?> targetClass = findEntityClass(key);
        Object primaryKey = findEntityId(key);
        Object entity = em.find(targetClass, primaryKey);
        return entity;
    }

    // Infinispan's metadata handling
    private MetadataEntityKey getMetadataKey(Object key) {
        byte[] keyBytes;
        try {
            keyBytes = marshaller.objectToByteBuffer(key);
        } catch (Exception e) {
            throw new RuntimeException("Failed to marshall key", e);
        }
        return new MetadataEntityKey(keyBytes);
    }

    private boolean isExpired(MetadataEntity entity) {
        long expiry = entity.getExpiration();
        return expiry > 0 && expiry <= System.currentTimeMillis();
    }

    @Override
    public MarshallableEntry<K, V> loadEntry(Object key) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        // What about metadata?

        try {
            // Do I really need a TX?
            tx.begin();
            // MetadataEntity fetching should do index seek to check if the expiration is beyond. No point loading the data
            MetadataEntity metadataEntity = em.find(MetadataEntity.class, getMetadataKey(key));
            Object entity = findEntity(em, key);
            tx.commit();
            if (entity != null && metadataEntity != null && metadataEntity.getMetadata() != null) {
                if (isExpired(metadataEntity)) {
                    return null;
                }

                Metadata metadata;
                try {
                    metadata = (Metadata) marshaller.objectFromByteBuffer(metadataEntity.getMetadata());
                } catch (Exception e) {
                    throw new RuntimeException("Failed to unmarshall metadata", e);
                }
                return marshallerEntryFactory.create(key, entity, metadata, metadataEntity.getCreated(), metadataEntity.getLastUsed());
            }
        } finally {
            if(tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                } catch(Exception e) {
                    // Print log instead
                    e.printStackTrace();
                }
            }
            em.close();
        }

        return null;
    }

    @Override
    public boolean delete(Object key) {
        EntityManager em = emf.createEntityManager();
        try {
            Object entity = findEntity(em, key);
            if (entity == null) {
                return false;
            }
            MetadataEntity metadataEntity = em.find(MetadataEntity.class, getMetadataKey(key));
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                em.remove(entity);
                if (metadataEntity != null) {
                    em.remove(metadataEntity);
                }
                tx.commit();
                return true;
            } catch (Exception e) {
                throw new RuntimeException("Exception caught in delete()", e);
            } finally {
                if (tx.isActive()) {
                    tx.rollback();
                }
            }
        } finally {
            em.close();
        }
    }

    @Override
    public boolean contains(Object key) {
        Class targetClass = findEntityClass(key);
        return false;
    }

    @Override
    public void start() {
        this.emf = emfRegistry.getEntityManagerFactory(configuration.persistenceUnitName());

    }

    @Override
    public void stop() {

    }
}
