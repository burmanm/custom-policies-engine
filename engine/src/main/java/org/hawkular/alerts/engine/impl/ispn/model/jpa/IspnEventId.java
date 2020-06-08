package org.hawkular.alerts.engine.impl.ispn.model.jpa;

import java.io.Serializable;

public class IspnEventId implements Serializable {
    private String tenantId;
    private String id;

    public IspnEventId(String tenantId, String id) {
        this.tenantId = tenantId;
        this.id = id;
    }

    public IspnEventId() {
    }
}
