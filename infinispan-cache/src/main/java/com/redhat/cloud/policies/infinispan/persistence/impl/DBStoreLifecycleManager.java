package com.redhat.cloud.policies.infinispan.persistence.impl;

import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.factories.GlobalComponentRegistry;
import org.infinispan.factories.annotations.InfinispanModule;
import org.infinispan.lifecycle.ModuleLifecycle;

@InfinispanModule(name = "cachestore-db", requiredModules = "core")
public class DBStoreLifecycleManager implements ModuleLifecycle {
   @Override
   public void cacheManagerStarting(GlobalComponentRegistry gcr, GlobalConfiguration globalConfiguration) {
      gcr.registerComponent(new EntityManagerFactoryRegistry(), EntityManagerFactoryRegistry.class);
   }

   @Override
   public void cacheManagerStopping(GlobalComponentRegistry gcr) {
      gcr.getComponent(EntityManagerFactoryRegistry.class).closeAll();
   }
}
