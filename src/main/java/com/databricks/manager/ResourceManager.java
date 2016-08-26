package com.databricks.manager;

import java.util.List;

/**
 * You should NOT modify this file for the assignment.
 *
 * This is the interface you want to implement. The resource manager should place and unplace
 * containers onto instances. The resource manager should take in a cloud provider, a fixed instance
 * size, and a fixed container size.
 */
public interface ResourceManager {

    /**
     * Places numContainers containers onto instances. If there are not enough resources with the
     * instances we currently have, request from the cloud provider for more instances. This method
     * should only return once all containers have been successfully placed. The implementation
     * of this method must be thread-safe.
     *
     * @param numContainers number of containers to place.
     * @return a list of containerIds which are IDs that are globally unique across all instances.
     */
    List<ContainerId> placeContainers(int numContainers);

    /**
     * Get the instance that the input container is placed on. Note that this instance returned
     * should be the same Instance object that is returned by the cloud provider. The implementation
     * of this method must be thread-safe.
     *
     * @param containerId the container id.
     * @return this instance should be referencing the same object that is returned by the
     *         instance request from the cloud provider.
     * @throws IllegalArgumentException if this container does not exist (i.e., is not placed on an
     *                                  instance).
     */
    Instance getInstance(ContainerId containerId);

    /**
     * Unplace a container from its host instance. The implementation of this method must be
     * thread-safe.
     *
     * @param containerId the container id.
     * @throws IllegalArgumentException if this container does not exist (i.e., is not placed on an
     *                                  instance).
     */
    void unplaceContainer(ContainerId containerId);
}
