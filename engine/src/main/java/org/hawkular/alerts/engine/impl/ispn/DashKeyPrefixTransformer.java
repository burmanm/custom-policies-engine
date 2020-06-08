package org.hawkular.alerts.engine.impl.ispn;

import com.redhat.cloud.policies.infinispan.persistence.impl.KeyTransformer;
import org.hawkular.alerts.engine.impl.ispn.model.IspnAction;
import org.hawkular.alerts.engine.impl.ispn.model.IspnActionDefinition;
import org.hawkular.alerts.engine.impl.ispn.model.IspnActionPlugin;
import org.hawkular.alerts.engine.impl.ispn.model.IspnCondition;
import org.hawkular.alerts.engine.impl.ispn.model.IspnDampening;
import org.hawkular.alerts.engine.impl.ispn.model.IspnEvent;
import org.hawkular.alerts.engine.impl.ispn.model.IspnTrigger;

import javax.persistence.IdClass;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * This is the counterpart of {@link IspnPk} class, returning the ISPN key back to the Entity key for
 * DB searches.
 */
public class DashKeyPrefixTransformer implements KeyTransformer {

    // TODO This class needs tests

    @Override
    public Class<?> keyToClass(Object okey) {
        String key = (String) okey;
        String[] parts = key.split("-");
        switch(parts[0]) {
            case "Action":
                return IspnAction.class;
            case "ActionPlugin":
                // ActionPlugin's PK is a String
                return IspnActionPlugin.class;
            case "ActionDefinition":
                return IspnActionDefinition.class;
            case "Condition":
                return IspnCondition.class;
            case "Dampening":
                return IspnDampening.class;
            case "Event":
                return IspnEvent.class;
            case "Trigger":
                return IspnTrigger.class;
            default:
                throw new RuntimeException("Invalid key detected, could not find correct entityType: " + key);
        }
    }

    public <T> Object keyToId(Class<T> targetClass, String[] keyParts) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        IdClass annotation = targetClass.getAnnotation(IdClass.class);
        Class<?> idKeyClass = annotation.value();
        for (Constructor<?> declaredConstructor : idKeyClass.getDeclaredConstructors()) {
            // TODO Some classes have "long" in the constructor, not String.
            if(declaredConstructor.getParameterCount() < 1) {
                // Skip Entity's default constructor
                continue;
            }
            return declaredConstructor.newInstance(Arrays.copyOfRange(keyParts, 0, declaredConstructor.getParameterCount()));
//            if(declaredConstructor.getParameterCount() == keyParts.length) {
//                return declaredConstructor.newInstance((Object[]) keyParts);
//            } else {
//                System.out.printf("Sizes %s: %d vs %d\n", declaredConstructor.toGenericString(), declaredConstructor.getParameterCount(), keyParts.length);
//            }
        }
        return null;
    }

    @Override
    public Object keyToID(Object okey) {
        String key = (String) okey;
//        System.out.printf("keyToId-> Received key: %s\n", key);
        Class<?> targetClass = keyToClass(okey);
        String[] parts = key.split("-");
        String[] idParts = Arrays.copyOfRange(parts, 1, parts.length);

//        if(targetClass == IspnActionPlugin.class) {
//            // TODO Or should I modify it to work with the common code?
//            return idParts[0];
//        }

        try {
            return keyToId(targetClass, idParts);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }
}
