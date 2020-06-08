package org.hawkular.alerts.engine.impl.ispn.model;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hawkular.alerts.api.model.dampening.Dampening;
import org.hawkular.alerts.api.model.trigger.Mode;
import org.hawkular.alerts.engine.impl.ispn.model.jpa.IspnDampeningId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.io.Serializable;

/**
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
@Indexed(index = "dampening")
@Entity
@IdClass(IspnDampeningId.class)
@TypeDefs({
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
public class IspnDampening implements Serializable {
    @Id
    private String dampeningId;

    @Field(store = Store.YES, analyze = Analyze.NO)
    private String tenantId;

    @Field(store = Store.YES, analyze = Analyze.NO)
    private String triggerId;

    @Field(store = Store.YES, analyze = Analyze.NO)
    private Mode triggerMode;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Dampening dampening;

    public IspnDampening() {
    }

    public IspnDampening(Dampening dampening) {
        updateDampening(dampening);
    }

    private void updateDampening(Dampening dampening) {
        if (null == dampening) {
            throw new IllegalArgumentException("dampening must be not null");
        }

        this.dampening = new Dampening(dampening);

        this.tenantId = dampening.getTenantId();
        this.triggerId = dampening.getTriggerId();
        this.triggerMode = dampening.getTriggerMode();
        this.dampeningId = dampening.getDampeningId();
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(String triggerId) {
        this.triggerId = triggerId;
    }

    public Mode getTriggerMode() {
        return triggerMode;
    }

    public void setTriggerMode(Mode triggerMode) {
        this.triggerMode = triggerMode;
    }

    public Dampening getDampening() {
        return dampening;
    }

    public void setDampening(Dampening dampening) {
        this.dampening = dampening;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dampening == null) ? 0 : dampening.hashCode());
        result = prime * result + ((tenantId == null) ? 0 : tenantId.hashCode());
        result = prime * result + ((triggerId == null) ? 0 : triggerId.hashCode());
        result = prime * result + ((triggerMode == null) ? 0 : triggerMode.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IspnDampening other = (IspnDampening) obj;
        if (dampening == null) {
            if (other.dampening != null)
                return false;
        } else if (!dampening.equals(other.dampening))
            return false;
        if (tenantId == null) {
            if (other.tenantId != null)
                return false;
        } else if (!tenantId.equals(other.tenantId))
            return false;
        if (triggerId == null) {
            if (other.triggerId != null)
                return false;
        } else if (!triggerId.equals(other.triggerId))
            return false;
        if (triggerMode != other.triggerMode)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "IspnDampening [tenantId=" + tenantId + ", triggerId=" + triggerId + ", triggerMode=" + triggerMode
                + ", dampeningn=" + dampening + "]";
    }

}
