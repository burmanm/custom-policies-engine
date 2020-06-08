package org.hawkular.alerts.engine.impl.ispn.model;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hawkular.alerts.api.model.action.Action;
import org.hawkular.alerts.engine.impl.ispn.model.jpa.IspnActionId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.SortableField;
import org.hibernate.search.annotations.Store;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.io.Serializable;

/**
 * An Action records the result of an ActionPlugin operating on an Event.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
@Indexed(index = "action")
@Entity
@IdClass(IspnActionId.class)
@TypeDefs({
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
public class IspnAction implements Serializable {
    @Id
    @Field(store = Store.YES, analyze = Analyze.NO)
    private String tenantId;

    @Id
    @Field(store = Store.YES, analyze = Analyze.NO)
    private String actionPlugin;

    @Id
    @Field(store = Store.YES, analyze = Analyze.NO)
    private String actionId;

    @Id
    @Field(store = Store.YES, analyze = Analyze.NO)
    private String eventId;

    @Field(store = Store.YES, analyze = Analyze.NO)
    private String result;

    @Id
    @Field(store = Store.YES, analyze = Analyze.NO)
    @SortableField
    private long ctime;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Action action;

    public IspnAction() {
    }

    public IspnAction(Action action) {
        updateAction(action);
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getActionPlugin() {
        return actionPlugin;
    }

    public void setActionPlugin(String actionPlugin) {
        this.actionPlugin = actionPlugin;
    }

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public long getCtime() {
        return ctime;
    }

    public void setCtime(long ctime) {
        this.ctime = ctime;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        updateAction(action);
    }

    private void updateAction(Action action) {
        if (action == null) {
            throw new IllegalArgumentException("action must be not null");
        }
        this.tenantId = action.getTenantId();
        this.actionPlugin = action.getActionPlugin();
        this.actionId = action.getActionId();
        this.eventId = action.getEventId();
        this.result = action.getResult();
        this.ctime = action.getCtime();
        this.action = new Action(action);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((action == null) ? 0 : action.hashCode());
        result = prime * result + ((actionId == null) ? 0 : actionId.hashCode());
        result = prime * result + ((actionPlugin == null) ? 0 : actionPlugin.hashCode());
        result = prime * result + (int) (ctime ^ (ctime >>> 32));
        result = prime * result + ((eventId == null) ? 0 : eventId.hashCode());
        result = prime * result + ((this.result == null) ? 0 : this.result.hashCode());
        result = prime * result + ((tenantId == null) ? 0 : tenantId.hashCode());
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
        IspnAction other = (IspnAction) obj;
        if (action == null) {
            if (other.action != null)
                return false;
        } else if (!action.equals(other.action))
            return false;
        if (actionId == null) {
            if (other.actionId != null)
                return false;
        } else if (!actionId.equals(other.actionId))
            return false;
        if (actionPlugin == null) {
            if (other.actionPlugin != null)
                return false;
        } else if (!actionPlugin.equals(other.actionPlugin))
            return false;
        if (ctime != other.ctime)
            return false;
        if (eventId == null) {
            if (other.eventId != null)
                return false;
        } else if (!eventId.equals(other.eventId))
            return false;
        if (result == null) {
            if (other.result != null)
                return false;
        } else if (!result.equals(other.result))
            return false;
        if (tenantId == null) {
            if (other.tenantId != null)
                return false;
        } else if (!tenantId.equals(other.tenantId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "IspnAction [tenantId=" + tenantId + ", actionPlugin=" + actionPlugin + ", actionId=" + actionId
                + ", eventId=" + eventId + ", result=" + result + ", ctime=" + ctime + ", action=" + action + "]";
    }

}
