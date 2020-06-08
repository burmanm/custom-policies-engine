package org.hawkular.alerts.engine.impl.ispn.model.jpa;

import java.io.Serializable;
import java.util.Objects;

public class IspnActionId implements Serializable {
    private String tenantId;
    private String actionPlugin;
    private String actionId;
    private String eventId;
    private long ctime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IspnActionId that = (IspnActionId) o;
        return ctime == that.ctime &&
                Objects.equals(tenantId, that.tenantId) &&
                Objects.equals(actionPlugin, that.actionPlugin) &&
                Objects.equals(actionId, that.actionId) &&
                Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, actionPlugin, actionId, eventId, ctime);
    }

    public IspnActionId(String tenantId, String actionPlugin, String actionId, String eventId, String ctimeS) {
        this.tenantId = tenantId;
        this.actionPlugin = actionPlugin;
        this.actionId = actionId;
        this.eventId = eventId;
        this.ctime = Long.valueOf(ctimeS).longValue();
    }

    public IspnActionId() {
    }
}
