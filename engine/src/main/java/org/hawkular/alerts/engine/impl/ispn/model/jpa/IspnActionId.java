package org.hawkular.alerts.engine.impl.ispn.model.jpa;

import java.io.Serializable;

public class IspnActionId implements Serializable {
    private String tenantId;
    private String actionPlugin;
    private String actionId;
    private String eventId;
    private long ctime;

    public IspnActionId(String tenantId, String actionPlugin, String actionId, String eventId, long ctime) {
        this.tenantId = tenantId;
        this.actionPlugin = actionPlugin;
        this.actionId = actionId;
        this.eventId = eventId;
        this.ctime = ctime;
    }

    public IspnActionId() {
    }
}
