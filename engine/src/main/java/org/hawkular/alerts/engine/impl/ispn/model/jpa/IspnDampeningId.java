package org.hawkular.alerts.engine.impl.ispn.model.jpa;

import java.io.Serializable;

public class IspnDampeningId implements Serializable {
    private String dampeningId;

    public IspnDampeningId(String id) {
        this.dampeningId = id;
    }

    public IspnDampeningId() {
    }
}
