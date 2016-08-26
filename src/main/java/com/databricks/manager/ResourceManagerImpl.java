package com.databricks.manager;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.*;

/*class getInstanceTask implements Runnable {
    private CloudProvider provider;

    public getInstanceTask(CloudProvider provider) {
        this.provider = provider;
    }

    @Override
    public void run() {
        provider.getInstance();
    }
}*/

/**
 * The resource manager should place and unplace containers onto instances. The resource manager
 * should take in a cloud provider, a fixed instance size, and a fixed container size.
 */
public class ResourceManagerImpl implements ResourceManager {

    private CloudProvider provider;
    private double instanceSizeGB;
    private double containerSizeGB;
    private HashMap<ContainerId, Instance> containerInstance;
    private HashSet<Instance> freeInstances;

    /**
     * Constructor for the resource manager.
     *
     * @param provider the cloud provider that we request instances from.
     * @param instanceSizeGB the size of all instances that is provided by the cloud provider.
     * @param containerSizeGB the size of all containers we want to place on instances.
     */
    public ResourceManagerImpl(CloudProvider provider, double instanceSizeGB, double containerSizeGB) {
        this.provider = provider;
        this.instanceSizeGB = instanceSizeGB;
        this.containerSizeGB = containerSizeGB;
        this.containerInstance = new HashMap<ContainerId, Instance>();
        this.freeInstances = new HashSet<Instance>();
    }

    public List<Instance> allocateInstances(int num) {
        //allocate Instances in parallel
        Executor executor = Executors.newFixedThreadPool(num);
        CompletionService<Instance> completionService = new ExecutorCompletionService<Instance>(executor);
        for(int i =0; i<num; i++) {
            completionService.submit(new Callable<Instance>() {
                public Instance call() {
                    return provider.requestInstance();
                }
            });
         }

        int received = 0;
        List<Instance> instanceList = new ArrayList<Instance>();
        while (received < num){
            try{
                Future<Instance> instanceFuture = completionService.take();
                instanceList.add(instanceFuture.get());
            }
            catch(Exception e) {
                // Do some logging
                System.out.println("ERROR OCCURRED WITH INSTANCE FUTURE");
            }
            received++;
        }
        return instanceList;
    }

    /**
     * Places numContainers containers onto instances. If there are not enough resources with the
     * instances we currently have, request from the cloud provider for more instances. This method
     * should only return once all containers have been successfully placed. This method is
     * thread-safe.
     *
     * @param numContainers number of containers to place.
     * @return a list of containerIds which are IDs that are globally unique across all instances.
     */
    public synchronized List<ContainerId> placeContainers(int numContainers) {
        List<ContainerId> containerIds = new ArrayList<ContainerId>();

        //iterate through free instances and allocate containers
        Iterator<Instance> it = freeInstances.iterator();
        while(it.hasNext() && containerIds.size() < numContainers) {
            Instance i = it.next();
            //add as much as possible to current free instance
            while (i.getRemainingMemoryGB() >= containerSizeGB && containerIds.size() < numContainers) {
                ContainerId newContainer = i.placeContainer(containerSizeGB);
                containerIds.add(newContainer);
                containerInstance.put(newContainer, i);
            }

            //remove instance if it's full
            if (i.getRemainingMemoryGB() < containerSizeGB) {
                it.remove();
            }
        }

        if (containerIds.size() == numContainers)
            return containerIds;

        
        int instancesNeeded = (int) Math.ceil((numContainers - containerIds.size()) * (this.containerSizeGB / this.instanceSizeGB));
        List<Instance> instanceList = allocateInstances(instancesNeeded);

        for(Instance i : instanceList) {
            //add as many containers as possible to new instance
            while (i.getRemainingMemoryGB() >= containerSizeGB && containerIds.size() < numContainers) {
                ContainerId newContainer = i.placeContainer(containerSizeGB);
                containerIds.add(newContainer);
                containerInstance.put(newContainer, i);
            }

            if (i.getRemainingMemoryGB() >= containerSizeGB) {
                freeInstances.add(i);
            }
        }

        return containerIds;
    }

    /**
     * Get the instance that the input container is placed on. Note that this instance returned
     * should be the same Instance object that is returned by the cloud provider.
     *
     * @param containerId the container id.
     * @return this instance should be referencing the same object that is returned by the
     *         instance request from the cloud provider.
     * @throws IllegalArgumentException if this container does not exist (i.e., is not placed on an
     *                                  instance).
     */
    public Instance getInstance(ContainerId containerId) {
        Instance i = containerInstance.get(containerId);
        if (i != null) {
            return i;
        }
        else {
            throw new IllegalArgumentException("Container " + containerId + " does not exist");
        }
    }

    /**
     * Unplace a container from its host instance. This method is thread-safe.
     *
     * @param containerId the container id.
     * @throws IllegalArgumentException if this container does not exist (i.e., is not placed on an
     *                                  instance).
     */
    public synchronized void unplaceContainer(ContainerId containerId) {
        Instance i = containerInstance.get(containerId);
        if (i != null) {
            i.unplaceContainer(containerId);
            freeInstances.add(i);
        }
        else {
            throw new IllegalArgumentException("Container " + containerId + " does not exist");
        }
    }
}
