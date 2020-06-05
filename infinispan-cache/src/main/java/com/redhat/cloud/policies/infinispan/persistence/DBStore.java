package com.redhat.cloud.policies.infinispan.persistence;

import com.redhat.cloud.policies.infinispan.persistence.configuration.DBStoreConfiguration;
import com.redhat.cloud.policies.infinispan.persistence.impl.EntityManagerFactoryRegistry;
import com.redhat.cloud.policies.infinispan.persistence.impl.KeyTransformer;
import com.redhat.cloud.policies.infinispan.persistence.impl.MetadataEntity;
import com.redhat.cloud.policies.infinispan.persistence.impl.Stats;
import org.infinispan.commons.configuration.ConfiguredBy;
import org.infinispan.commons.persistence.Store;
import org.infinispan.marshall.persistence.PersistenceMarshaller;
import org.infinispan.persistence.spi.AdvancedLoadWriteStore;
import org.infinispan.persistence.spi.InitializationContext;
import org.infinispan.persistence.spi.MarshallableEntry;
import org.infinispan.persistence.spi.MarshallableEntryFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
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
        // TODO Make this configurable and use classloader
//        this.keyTransformer = new DashKeyPrefixTransformer();
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

    @Override
    public MarshallableEntry<K, V> loadEntry(Object key) {
        Class<?> targetClass = findEntityClass(key);
        Object primaryKey = findEntityId(key);

        EntityManager em = emf.createEntityManager();
        // What about metadata?
        Object o = em.find(targetClass, primaryKey);
        // Cast to correct type?

        return null;
    }

    @Override
    public boolean delete(Object key) {
        Class targetClass = findEntityClass(key);
        return false;
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
