package com.redhat.cloud.policies.infinispan.persistence;

import com.redhat.cloud.policies.infinispan.persistence.impl.KeyTransformer;

public class TestKeyTransformer implements KeyTransformer {
    @Override
    public Class<?> keyToClass(Object key) {
        return null;
    }

    @Override
    public Object keyToID(Object key) {
        return null;
    }
}
