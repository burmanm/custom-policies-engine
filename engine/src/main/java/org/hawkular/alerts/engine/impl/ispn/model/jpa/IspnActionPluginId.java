package org.hawkular.alerts.engine.impl.ispn.model.jpa;

import java.io.Serializable;
import java.util.Objects;

public class IspnActionPluginId implements Serializable {
    private String actionPlugin;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IspnActionPluginId that = (IspnActionPluginId) o;
        return Objects.equals(actionPlugin, that.actionPlugin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(actionPlugin);
    }

    // For Hibernate
    public IspnActionPluginId() {

    }

    public IspnActionPluginId(String id) {
        this.actionPlugin = id;
    }
}
