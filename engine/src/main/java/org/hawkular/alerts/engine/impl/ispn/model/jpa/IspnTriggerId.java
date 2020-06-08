package org.hawkular.alerts.engine.impl.ispn.model.jpa;

import java.io.Serializable;
import java.util.Objects;

public class IspnTriggerId implements Serializable {
    private String tenantId;
    private String triggerId;

    public IspnTriggerId(String tenantId, String triggerId) {
        this.tenantId = tenantId;
        this.triggerId = triggerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IspnTriggerId that = (IspnTriggerId) o;
        return Objects.equals(tenantId, that.tenantId) &&
                Objects.equals(triggerId, that.triggerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, triggerId);
    }

    public IspnTriggerId() {
    }
}
