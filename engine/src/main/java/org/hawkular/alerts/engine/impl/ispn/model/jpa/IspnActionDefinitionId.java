package org.hawkular.alerts.engine.impl.ispn.model.jpa;

import java.io.Serializable;
import java.util.Objects;

public class IspnActionDefinitionId implements Serializable {
    private String tenantId;
    private String actionPlugin;
    private String actionId;

    public IspnActionDefinitionId() {
    }

    public IspnActionDefinitionId(String tenantId, String actionPlugin, String actionId) {
        this.tenantId = tenantId;
        this.actionPlugin = actionPlugin;
        this.actionId = actionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IspnActionDefinitionId that = (IspnActionDefinitionId) o;
        return Objects.equals(tenantId, that.tenantId) &&
                Objects.equals(actionPlugin, that.actionPlugin) &&
                Objects.equals(actionId, that.actionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, actionPlugin, actionId);
    }
}
