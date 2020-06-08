package org.hawkular.alerts.engine.impl.ispn.model.jpa;

import java.io.Serializable;
import java.util.Objects;

public class IspnEventId implements Serializable {
    private String tenantId;
    private String id;

    public IspnEventId(String tenantId, String id) {
        this.tenantId = tenantId;
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IspnEventId that = (IspnEventId) o;
        return Objects.equals(tenantId, that.tenantId) &&
                Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, id);
    }

    public IspnEventId() {
    }
}
