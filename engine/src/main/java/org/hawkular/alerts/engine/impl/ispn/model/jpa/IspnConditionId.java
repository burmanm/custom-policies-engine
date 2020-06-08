package org.hawkular.alerts.engine.impl.ispn.model.jpa;

import java.io.Serializable;

public class IspnConditionId implements Serializable {
    private String conditionId;

    public IspnConditionId() {
    }

    public IspnConditionId(String id) {
        this.conditionId = id;
    }
}
