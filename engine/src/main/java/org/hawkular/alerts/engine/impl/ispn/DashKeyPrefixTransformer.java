package com.redhat.policies.infinispan.persistence.impl;

public class DashKeyPrefixTransformer implements KeyTransformer {

    static String keyToSearchKey(String key) {
        return key.substring(0, key.indexOf("-"));
    }

    @Override
    public Object keyToID(Object okey) {
        String key = (String) okey;
        String entityType = keyToSearchKey(key);
        return null;
    }
}
