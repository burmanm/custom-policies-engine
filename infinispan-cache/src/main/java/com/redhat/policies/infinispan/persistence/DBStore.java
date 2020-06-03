package com.redhat.policies.infinispan.persistence;

import com.redhat.policies.infinispan.persistence.configuration.DBStoreConfiguration;
import com.redhat.policies.infinispan.persistence.impl.DashKeyPrefixTransformer;
import com.redhat.policies.infinispan.persistence.impl.EntityManagerFactoryRegistry;
import com.redhat.policies.infinispan.persistence.impl.KeyTransformer;
import com.redhat.policies.infinispan.persistence.impl.MetadataEntity;
import com.redhat.policies.infinispan.persistence.impl.Stats;
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
import java.util.Map;
import java.util.concurrent.Executor;

@Store
@ConfiguredBy(DBStoreConfiguration.class)
public class DBStore<K, V> implements AdvancedLoadWriteStore<K,V> {

//    private final MsgLogger log = MsgLogging.getMsgLogger(DBStore.class);
//    static Map<String, Class> keyPrefixToClass;
//
//    static {
//        keyPrefixToClass = new HashMap<>(8);
//        // TODO Move these classes to API from engine?
//        // TODO Also, if configured - there's no need to add them manually here
//        keyPrefixToClass.put("Action", IspnAction.class);
//        keyPrefixToClass.put("ActionDefinition", IspnActionDefinition.class);
//        keyPrefixToClass.put("Condition", IspnCondition.class);
//        keyPrefixToClass.put("Dampening", IspnDampening.class);
//        keyPrefixToClass.put("Event", IspnEvent.class);
//        keyPrefixToClass.put("Alert", IspnEvent.class);
//        keyPrefixToClass.put("Trigger", IspnTrigger.class);
//    }

    private Stats stats = new Stats();
    private DBStoreConfiguration configuration;
    private EntityManagerFactory emf;
    private EntityManagerFactoryRegistry emfRegistry;
    private PersistenceMarshaller marshaller;
    private MarshallableEntryFactory<K,V> marshallerEntryFactory;
    private Map<String, Class<?>> keyPrefixToClass;
    private KeyTransformer keyTransformer;

    @Override
    public int size() {
        // Calculate the combined size of all tables?
        return 0;
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
        this.keyPrefixToClass = this.configuration.prefixToClass();
        // TODO Make this configurable
        this.keyTransformer = new DashKeyPrefixTransformer();
    }

    @Override
    public void write(MarshallableEntry<? extends K, ? extends V> entry) {
//        log.infof("Writing %s to storage", entry.getKey().toString());
        EntityManager em = emf.createEntityManager();

        Object entity = entry.getValue();
        MetadataEntity metadata = new MetadataEntity(entry);
        try {
            EntityTransaction txn = em.getTransaction();

//            long txnBegin = timeService.time();
            try {
//                if (trace) log.trace("Writing " + entity + "(" + toString(metadata) + ")");
                txn.begin();

                em.merge(entity);

//                mergeEntity(em, entity);
                if (metadata.hasBytes()) {
                    em.merge(metadata);
//                    mergeMetadata(em, metadata);
                }

                txn.commit();
//                stats.addWriteTxCommited(timeService.time() - txnBegin);
            } catch (Exception e) {
//                stats.addWriteTxFailed(timeService.time() - txnBegin);
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
        String prefixKey = keyTransformer.keyToSearchKey(key.toString());
        return keyPrefixToClass.get(prefixKey);
    }

    @Override
    public MarshallableEntry<K, V> loadEntry(Object key) {
        Class targetClass = findEntityClass(key);
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

    }

    @Override
    public void stop() {

    }
}
