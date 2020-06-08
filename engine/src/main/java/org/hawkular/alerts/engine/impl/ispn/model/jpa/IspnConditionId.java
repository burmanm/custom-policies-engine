package org.hawkular.alerts.engine.impl.ispn.model.jpa;

import java.io.Serializable;
import java.util.Objects;

public class IspnConditionId implements Serializable {
    private String conditionId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IspnConditionId that = (IspnConditionId) o;
        return Objects.equals(conditionId, that.conditionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(conditionId);
    }

    public IspnConditionId() {
    }

    public IspnConditionId(String id) {
        this.conditionId = id;
    }
}
