package com.databricks.manager;

import java.util.List;

/**
 * You should NOT modify this file for the assignment.
 *
 * API provided by an engineer on your team for an instance that is given by a cloud provider. You
 * may assume you do not need to care about any other exceptions than the ones listed below for the
 * purposes of this assignment. Note that there are other failures such as allocation failures
 * which may happen in real life.
 */
public interface Instance {

    /**
     * The implementation of this method must be thread-safe.
     *
     * @return remaining memory on this instance in GB.
     */
    double getRemainingMemoryGB();

    /**
     * The implementation of this method must be thread-safe.
     *
     * @return a list of all containers currently placed on this instance.
     */
    List<ContainerId> getAllContainers();

    /**
     * Places a container of size memoryGB on the instance. If this call fails, then the state
     * remains as if this request never happened and will still be in a valid state. The
     * implementation of this method must be thread-safe.
     *
     * @return containerId which is an ID that is globally unique across all instances.
     * @throws IllegalStateException if there is not enough memory left to place a container of this
     *                               size on the instance.
     */
    ContainerId placeContainer(double memoryGB);

    /**
     * Unplaces a container with id containerId from the instance. If this call fails, then the
     * state remains as if this request never happened and will still be in a valid state. The
     * implementation of this method must be thread-safe.
     *
     * @throws IllegalArgumentException if this container is not placed on this instance.
     */
    void unplaceContainer(ContainerId containerId);
}
