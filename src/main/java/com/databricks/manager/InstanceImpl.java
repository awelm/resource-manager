package com.databricks.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * You should NOT modify this file for the assignment.
 *
 * Implementation of the Instance API provided by an engineer on your team for an instance that is
 * given by a cloud provider. You may assume you do not need to care about any other exceptions
 * than the ones listed below for the purposes of this assignment. Note that there are other
 * failures such as allocation failures which may happen in real life.
 */
public class InstanceImpl implements Instance {

    // Used for instance id.
    private static AtomicLong instanceIdCount = new AtomicLong(1);
    private long instanceId;
    // Used for container id.
    private long containerIdCount;

    // Total memory available on this instance.
    private double memoryGB;
    // Current usage of memory on this instance.
    private double usedMemoryGB;

    // Current list of containers.
    private List<ContainerId> containers;
    // Current list of container sizes in GB. The size of this list should equal the
    // size of the list of containers.
    private List<Double> containerSizesGB;

    public InstanceImpl(double memoryGB) {
        this.instanceId = instanceIdCount.getAndIncrement();
        this.containerIdCount = 1;
        this.memoryGB = memoryGB;
        this.usedMemoryGB = 0;
        this.containers = new ArrayList<ContainerId>();
        this.containerSizesGB = new ArrayList<Double>();
    }

    /**
     * This method is thread-safe.
     *
     * @return remaining memory on this instance in GB.
     */
    public synchronized double getRemainingMemoryGB() {
        return memoryGB - usedMemoryGB;
    }

    /**
     * This method is thread-safe.
     *
     * @return a list of all containers currently placed on this instance.
     */
    public synchronized List<ContainerId> getAllContainers() {
        return new ArrayList<ContainerId>(containers);
    }

    /**
     * Places a container of size memoryGB on the instance. If this call fails, then the state
     * remains as if this request never happened and will still be in a valid state. This method is
     * thread-safe.
     *
     * @return containerId which is an ID that is globally unique across all instances.
     * @throws IllegalStateException if there is not enough memory left to place a container of this
     *                               size on the instance.
     */
    public synchronized ContainerId placeContainer(double memoryGB) {
        if (getRemainingMemoryGB() < memoryGB) {
            throw new IllegalStateException("Not enough memory to place container" +
                " of size " + memoryGB + " GB.");
        }

        ContainerId newContainerId = new ContainerId(instanceId + "-" + containerIdCount);
        containerIdCount++;
        usedMemoryGB += memoryGB;
        containers.add(newContainerId);
        containerSizesGB.add(memoryGB);

        return newContainerId;
    }

    /**
     * Unplaces a container with id containerId from the instance. If this call fails, then the
     * state remains as if this request never happened and will still be in a valid state. This
     * method is thread-safe.
     *
     * @throws IllegalArgumentException if this container is not placed on this instance.
     */
    public synchronized void unplaceContainer(ContainerId containerId) {
        if (!containers.contains(containerId)) {
            throw new IllegalArgumentException("No container with id " + containerId +
                " placed on the instance.");
        }

        int index = containers.indexOf(containerId);
        containers.remove(index);
        double removedMemoryGB = containerSizesGB.remove(index);
        usedMemoryGB -= removedMemoryGB;
    }
}
