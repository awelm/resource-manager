package com.databricks.manager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * This suite should test basic functionality of ResourceManagerImpl implementation.
 */

/*
 * Small implementation of CloudProvider:
 * Created in order to test the recovery-feature of the Resource Manager
 */
class CloudProviderReal implements CloudProvider {
    private double instanceGB;
    private HashSet<Instance> instances;

    public CloudProviderReal(double s) {
        instanceGB = s;
        instances = new HashSet<Instance>();
    }

    public int getInstanceCount() {
        return instances.size();
    }

    public synchronized Instance requestInstance() {
        Instance i = new InstanceImpl(instanceGB);
        instances.add(i);
        return i;
    }

    public synchronized HashSet<Instance> getAllInstances() {
        return instances;
    }
}

/*
 * Instance of a thread that calls placeContainer() on the Resource Manager
 * Used to simulate multiple threads calling placeContainer() at once
 */
class placeContainerThread implements Runnable {
    private ResourceManager rm;
    private int containerAmount;

    public placeContainerThread(ResourceManager rm, int containerAmount) {
        this.rm = rm;
        this.containerAmount = containerAmount;
    }

    @Override
        public void run() {
            rm.placeContainers(containerAmount);
        }
}


public class ResourceManagerImplTest {
    private static CloudProvider cp;
    private static double instanceSize;

    @Before
        public void initObjects() {
            cp = mock(CloudProvider.class);
        }

    @Test
        //Sanity test
        public void testBasic() {
            instanceSize = 3;
            when(cp.requestInstance()).thenReturn(new InstanceImpl(instanceSize));
            ResourceManagerImpl rm = new ResourceManagerImpl(cp, instanceSize, 1);
            rm.placeContainers(3);
            verify(cp, times(1)).requestInstance();
        }

    @Test
        //Make sure an instance can be filled with two seperate calls
        public void testBasicTwo() {
            instanceSize = 5;
            when(cp.requestInstance()).thenReturn(new InstanceImpl(instanceSize));
            ResourceManagerImpl rm = new ResourceManagerImpl(cp, instanceSize, 1);
            rm.placeContainers(3);
            rm.placeContainers(2);
            verify(cp, times(1)).requestInstance();
        }

    @Test
        //Check that two instances can be created when necessary
        public void testBasicThree() {
            instanceSize = 5;
            when(cp.requestInstance()).thenReturn(new InstanceImpl(instanceSize)).thenReturn(new InstanceImpl(instanceSize));
            ResourceManagerImpl rm = new ResourceManagerImpl(cp, instanceSize, 1);
            rm.placeContainers(3);
            rm.placeContainers(3);
            verify(cp, times(2)).requestInstance();
        }

    @Test
        //Check that the unplaceContainer functionality works
        public void testBasicFour() {
            instanceSize = 5;
            when(cp.requestInstance()).thenReturn(new InstanceImpl(instanceSize)).thenReturn(new InstanceImpl(instanceSize));
            ResourceManagerImpl rm = new ResourceManagerImpl(cp, instanceSize, 1);
            List<ContainerId> li = rm.placeContainers(3);
            rm.unplaceContainer(li.get(0));
            rm.placeContainers(3);
            verify(cp, times(1)).requestInstance();
        }

    @Test
        //fill and unfill a single instance a bunch of times, and ensure that the Resource Manager only allocated one instance
        public void testRepeatedly() {
            instanceSize = 5;
            when(cp.requestInstance()).thenReturn(new InstanceImpl(instanceSize)).thenReturn(new InstanceImpl(instanceSize));
            ResourceManagerImpl rm = new ResourceManagerImpl(cp, instanceSize, 1);
            List<ContainerId> li = rm.placeContainers(5);

            for (int i = 0; i < 10; i++) {
                for (int x = 0; x < 5; x++)
                    rm.unplaceContainer(li.get(x));
                li = rm.placeContainers(5);
            }
            verify(cp, times(1)).requestInstance();
        }

    @Test
        //Test the functionality of the Resource Manager recovery feature
        public void testResourceManagerRecovery() {
            instanceSize = 10;
            CloudProviderReal cp_real = new CloudProviderReal(instanceSize);
            ResourceManagerImpl rm = new ResourceManagerImpl(cp_real, instanceSize, 1);
            List<ContainerId> li = rm.placeContainers(40);
            assert(cp_real.getInstanceCount() == 4);
            HashMap<ContainerId, Instance> answers = new HashMap<ContainerId, Instance>();
            for (ContainerId curr : li) //keep track current resource manager state
                answers.put(curr, rm.getInstance(curr));

            //create new resource manager and check if it has the same state
            ResourceManagerImpl rm_new = new ResourceManagerImpl(cp_real, instanceSize, 1);
            for (ContainerId c : li) {
                assert(rm_new.getInstance(c) == answers.get(c));
            }
        }

    @Test
        //Test whether proper functionality holds with multiple overlapping calls to requestInstance()
        public void testOverlappingInstanceRequests() {
            System.out.println("Running test which sleeps for 5 seconds.");
            instanceSize = 5;
            //make the first request take 3 seconds, and the next 2 requests be instantaneous
            when(cp.requestInstance()).thenAnswer(new Answer<Instance>() {
                @Override
                public Instance answer(InvocationOnMock invocation){
                    try {
                        Thread.sleep(5000);
                    }
                    catch (Exception e) {
                        System.err.println(e);
                    }
                    return new InstanceImpl(instanceSize);
                }
            }).thenReturn(new InstanceImpl(instanceSize))
            .thenReturn(new InstanceImpl(instanceSize));

            ResourceManagerImpl rm = new ResourceManagerImpl(cp, instanceSize, 1);
            //run 3 placeContainer() requests in parallel
            ExecutorService executor = Executors.newFixedThreadPool(3);
            int[] containerRequestAmounts = {2,2,1};
            for (int i = 0; i < 3; i++) {
                Runnable worker = new placeContainerThread(rm, containerRequestAmounts[i]);
                executor.execute(worker);
            }
            executor.shutdown();
            //wait for threads to finish
            while (!executor.isTerminated())
                ;
            verify(cp, times(1)).requestInstance();
        }
}
