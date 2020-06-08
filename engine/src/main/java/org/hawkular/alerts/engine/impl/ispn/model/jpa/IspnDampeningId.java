package org.hawkular.alerts.engine.impl.ispn.model.jpa;

import java.io.Serializable;
import java.util.Objects;

public class IspnDampeningId implements Serializable {
    private String dampeningId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IspnDampeningId that = (IspnDampeningId) o;
        return Objects.equals(dampeningId, that.dampeningId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dampeningId);
    }

    public IspnDampeningId(String id) {
        this.dampeningId = id;
    }

    public IspnDampeningId() {
    }
}
