package com.redhat.cloud.policies.infinispan.persistence.impl;

public interface KeyTransformer {
    Class<?> keyToClass(Object key);
    Object keyToID(Object key);
}
