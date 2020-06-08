package org.hawkular.alerts.engine.impl.ispn.model.jpa;

import java.io.Serializable;

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
}
