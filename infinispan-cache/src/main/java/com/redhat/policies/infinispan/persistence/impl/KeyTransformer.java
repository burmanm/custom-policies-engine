package com.redhat.policies.infinispan.persistence.impl;

public interface KeyTransformer {
    String keyToSearchKey(String key);
}
