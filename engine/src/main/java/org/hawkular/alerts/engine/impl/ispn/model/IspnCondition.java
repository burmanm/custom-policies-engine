package org.hawkular.alerts.engine.impl.ispn.model;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hawkular.alerts.api.model.condition.AvailabilityCondition;
import org.hawkular.alerts.api.model.condition.CompareCondition;
import org.hawkular.alerts.api.model.condition.Condition;
import org.hawkular.alerts.api.model.condition.EventCondition;
import org.hawkular.alerts.api.model.condition.ExternalCondition;
import org.hawkular.alerts.api.model.condition.MissingCondition;
import org.hawkular.alerts.api.model.condition.NelsonCondition;
import org.hawkular.alerts.api.model.condition.RateCondition;
import org.hawkular.alerts.api.model.condition.StringCondition;
import org.hawkular.alerts.api.model.condition.ThresholdCondition;
import org.hawkular.alerts.api.model.condition.ThresholdRangeCondition;
import org.hawkular.alerts.api.model.trigger.Mode;
import org.hawkular.alerts.engine.impl.ispn.model.jpa.IspnConditionId;
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
@Indexed(index = "condition")
@Entity
@IdClass(IspnConditionId.class)
@TypeDefs({
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
public class IspnCondition implements Serializable {
    @Id
    private String conditionId;

    @Field(store = Store.YES, analyze = Analyze.NO)
    private String tenantId;

    @Field(store = Store.YES, analyze = Analyze.NO)
    private String triggerId;

    @Field(store = Store.YES, analyze = Analyze.NO)
    private Mode triggerMode;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Condition condition;

    public IspnCondition() {
    }

    public IspnCondition(Condition condition) {
        updateCondition(condition);
    }

    private void updateCondition(Condition condition) {
        if (null == condition) {
            throw new IllegalArgumentException("condition must be not null");
        }
        switch (condition.getType()) {
            case AVAILABILITY:
                this.condition = new AvailabilityCondition((AvailabilityCondition) condition);
                break;
            case COMPARE:
                this.condition = new CompareCondition((CompareCondition) condition);
                break;
            case EVENT:
                this.condition = new EventCondition((EventCondition) condition);
                break;
            case EXTERNAL:
                this.condition = new ExternalCondition((ExternalCondition) condition);
                break;
            case MISSING:
                this.condition = new MissingCondition((MissingCondition) condition);
                break;
            case NELSON:
                this.condition = new NelsonCondition((NelsonCondition) condition);
                break;
            case RANGE:
                this.condition = new ThresholdRangeCondition((ThresholdRangeCondition) condition);
                break;
            case RATE:
                this.condition = new RateCondition((RateCondition) condition);
                break;
            case STRING:
                this.condition = new StringCondition((StringCondition) condition);
                break;
            case THRESHOLD:
                this.condition = new ThresholdCondition((ThresholdCondition) condition);
                break;
            default:
                throw new IllegalArgumentException("Unknown condition type: " + condition.getType());
        }

        this.tenantId = condition.getTenantId();
        this.triggerId = condition.getTriggerId();
        this.triggerMode = condition.getTriggerMode();
        this.conditionId = condition.getConditionId();
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

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((condition == null) ? 0 : condition.hashCode());
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
        IspnCondition other = (IspnCondition) obj;
        if (condition == null) {
            if (other.condition != null)
                return false;
        } else if (!condition.equals(other.condition))
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
        return "IspnCondition [tenantId=" + tenantId + ", triggerId=" + triggerId + ", triggerMode=" + triggerMode
                + ", condition=" + condition + "]";
    }

}
