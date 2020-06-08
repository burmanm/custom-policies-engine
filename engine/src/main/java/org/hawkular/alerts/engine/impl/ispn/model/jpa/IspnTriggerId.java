package org.hawkular.alerts.engine.impl.ispn.model.jpa;

import java.io.Serializable;

public class IspnTriggerId implements Serializable {
    private String tenantId;
    private String triggerId;

    public IspnTriggerId(String tenantId, String triggerId) {
        this.tenantId = tenantId;
        this.triggerId = triggerId;
    }

    public IspnTriggerId() {
    }
}
