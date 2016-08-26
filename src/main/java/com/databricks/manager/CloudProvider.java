package com.databricks.manager;

import java.util.Set;

/**
 * You should NOT modify this file for the assignment.
 * 
 * Public API provided by the cloud provider.
 */
public interface CloudProvider {

    /**
     * Synchronously request a new instance. The size of the instance will depend on the classes
     * that implement this interface. Note that this call may be blocked for a long period of time
     * before it is fulfilled (i.e. a few minutes). The implementation of this method must be
     * thread-safe.
     */
    Instance requestInstance();

    /**
     * The implementation of this method must be thread-safe.
     *
     * @return a set of all existing instances.
     */
    Set<Instance> getAllInstances();
}
