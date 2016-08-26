Databricks Coding Assignment: Resource Manager

Instructions

Supposed you want to offer a service which sets up Spark clusters (a set of machines that communicate with one another) for users. One thing you need is a resource manager, a service which manages relationships between Spark workers and instances (servers). Based on the size of the requested cluster, the resource manager should request a set of instances from a cloud provider and place containers (lightweight virtual machines) on these instances (where each container becomes a Spark worker). When there is a request to shutdown a cluster, the resource manager should unplace containers from instances. For the purposes of this coding assignment, we keep instances we have obtained forever.

The goal of this assignment is to implement a resource manager. The resource manager should take in three parameters: the cloud provider, the memory size in GB of the instances that the cloud provider provides, and the memory size in GB of the containers you want to place. Also, your teammate has already implemented an interface for placing and unplacing containers on a single instance so your goal for implementing the resource manager is to manage the relationship between containers and multiple instances.

Requirements

1. The resource manager should create the minimal number of instances when placing multiple containers. For example, if the instance size if 60 GB and the container size is 30 GB, placing 2 containers should result in a single new instance.
  * The resource manager should NOT make any instance requests when there is space available to place containers on an existing instance. For example, supposed 2 containers can fit on a single instance. If a suer makes a call to place a single container and the container is placed, then another call to place a single container should place this container on the same instance.
  * The resource manager should NOT make instance requests when there is space available on an existing instance request. For example, suppose 2 containers can fit on a single instance. If a user makes a call to place a single container, then we will make a request for an instance. If there is another call to place a single container before the existing instance request completes, this new container should be placed on the same instance.
2. Instance creation may take a long time. However, the time it takes to place 2 containers should be approximately the same as the time it takes to place 20 containers, assuming that no free resources are available prior to placement (assuming that 2 containers fit on a single instance).
3. The resource manager must be able to recover state when restarting on failure.
4. Your implementation should be production-ready and should include unit-tests testing all functionality of your code (i.e., in a suite called ResourceManagerImplTest.java). We recommend using JUnit testing framework to utilize mocks for these tests.
5. Your unit-tests should run very quickly (on the order of seconds).
6. Your implementation must be in Java.

Assumptions

1. There will be no failures when making requests to the cloud provider for instances.
2. The resource manager will never be responsible for managing more than around 10,000 instances and/or containers.
3. You may assume that there will only be a single instance of the Resource Manager object.
# resource-manager
