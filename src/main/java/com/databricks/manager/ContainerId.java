package com.databricks.manager;

/**
 * You should NOT modify this file for the assignment.
 *
 * API provided by an engineer on your team for a ContainerId. This is just a wrapper class for the
 * string representing a container ID.
 */
public class ContainerId {

    private String id;

    public ContainerId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ContainerId)) {
            return false;
        }

        ContainerId other = (ContainerId) object;
        return getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
