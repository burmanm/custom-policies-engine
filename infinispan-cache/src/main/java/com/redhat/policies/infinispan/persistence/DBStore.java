package com.redhat.policies.infinispan.persistence;

import org.hawkular.alerts.engine.impl.ispn.IspnDefinitionsServiceImpl;
import org.hawkular.alerts.engine.impl.ispn.model.IspnAction;
import org.hawkular.alerts.engine.impl.ispn.model.IspnActionDefinition;
import org.hawkular.alerts.engine.impl.ispn.model.IspnCondition;
import org.hawkular.alerts.engine.impl.ispn.model.IspnDampening;
import org.hawkular.alerts.engine.impl.ispn.model.IspnEvent;
import org.hawkular.alerts.engine.impl.ispn.model.IspnTrigger;
import org.infinispan.commons.persistence.Store;
import org.infinispan.persistence.spi.AdvancedLoadWriteStore;
import org.infinispan.persistence.spi.InitializationContext;
import org.infinispan.persistence.spi.MarshallableEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

@Store
public class DBStore<K, V> implements AdvancedLoadWriteStore<K,V> {

    static Map<String, Class> keyPrefixToClass;

    static {
        keyPrefixToClass = new HashMap<>(8);
        keyPrefixToClass.put("Action", IspnAction.class);
        keyPrefixToClass.put("ActionDefinition", IspnActionDefinition.class);
        keyPrefixToClass.put("Condition", IspnCondition.class);
        keyPrefixToClass.put("Dampening", IspnDampening.class);
        keyPrefixToClass.put("Event", IspnEvent.class);
        keyPrefixToClass.put("Alert", IspnEvent.class);
        keyPrefixToClass.put("Trigger", IspnTrigger.class);
    }

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

    }

    @Override
    public void write(MarshallableEntry<? extends K, ? extends V> entry) {

    }

    @Override
    public MarshallableEntry<K, V> loadEntry(Object key) {
        Class targetClass = keyPrefixToClass.get(key);
        return null;
    }

    @Override
    public boolean delete(Object key) {
        Class targetClass = keyPrefixToClass.get(key);
        return false;
    }

    @Override
    public boolean contains(Object key) {
        Class targetClass = keyPrefixToClass.get(key);
        return false;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
