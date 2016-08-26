package com.databricks.manager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.List;
import java.util.ArrayList;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * This suite should test basic functionality of ResourceManagerImpl implementation.
 */

abstract class CloudProviderFake implements CloudProvider {
    private double instanceGB;

    public void setSize(double s) {
        instanceGB = s;
    }

    public Instance requestInstance() {
        return new InstanceImpl(instanceGB);
    }
}


public class ResourceManagerImplTest {
    private static CloudProvider cp;
    private static double instanceSize;

    @Before
    public void initObjects() {
        cp = mock(CloudProvider.class);
        //cp.setSize(instanceSize);
    }

    @Test
    public void testBasic() {
        instanceSize = 3;
        when(cp.requestInstance()).thenReturn(new InstanceImpl(instanceSize));
        ResourceManagerImpl rm = new ResourceManagerImpl(cp, instanceSize, 1);
        rm.placeContainers(3);
        verify(cp, times(1)).requestInstance();
    }

    @Test
    public void testBasicTwo() {
        instanceSize = 5;
        when(cp.requestInstance()).thenReturn(new InstanceImpl(instanceSize));
        ResourceManagerImpl rm = new ResourceManagerImpl(cp, instanceSize, 1);
        rm.placeContainers(3);
        rm.placeContainers(2);
        verify(cp, times(1)).requestInstance();
    }
    
    @Test
    public void testBasicThree() {
        instanceSize = 5;
        when(cp.requestInstance()).thenReturn(new InstanceImpl(5)).thenReturn(new InstanceImpl(5));
        ResourceManagerImpl rm = new ResourceManagerImpl(cp, instanceSize, 1);
        rm.placeContainers(3);
        rm.placeContainers(3);
        verify(cp, times(2)).requestInstance();
    }

    @Test
    public void testBasicFour() {
        instanceSize = 5;
        when(cp.requestInstance()).thenReturn(new InstanceImpl(5)).thenReturn(new InstanceImpl(5));
        ResourceManagerImpl rm = new ResourceManagerImpl(cp, instanceSize, 1);
        List<ContainerId> li = rm.placeContainers(3);
        rm.unplaceContainer(li.get(0));
        rm.placeContainers(3);
        verify(cp, times(1)).requestInstance();
    }

}
