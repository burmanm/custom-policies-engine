package com.redhat.policies.infinispan.persistence.impl;

public class DashKeyPrefixTransformer implements KeyTransformer {
    @Override
    public String keyToSearchKey(String key) {
        return key.substring(0, key.indexOf("-"));
    }
}
