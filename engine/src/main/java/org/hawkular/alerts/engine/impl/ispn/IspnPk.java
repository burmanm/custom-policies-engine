package org.hawkular.alerts.engine.impl.ispn;

import org.hawkular.alerts.api.model.action.Action;
import org.hawkular.alerts.api.model.action.ActionDefinition;
import org.hawkular.alerts.api.model.condition.Condition;
import org.hawkular.alerts.api.model.dampening.Dampening;
import org.hawkular.alerts.api.model.event.Event;
import org.hawkular.alerts.api.model.trigger.Trigger;

/**
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public class IspnPk {

    public final static String SEPARATOR = Character.toString(Character.PARAGRAPH_SEPARATOR);

    public static String pk(Action action) {
        if (action == null) {
            return null;
        }
        return new StringBuilder("Action")
                .append(SEPARATOR)
                .append(action.getTenantId())
                .append(SEPARATOR)
                .append(action.getActionPlugin())
                .append(SEPARATOR)
                .append(action.getActionId())
                .append(SEPARATOR)
                .append(action.getEventId())
                .append(SEPARATOR)
                .append(action.getCtime())
                .toString();
    }

    public static String pk(String actionPlugin) {
        if (actionPlugin == null) {
            return null;
        }
        return new StringBuilder("ActionPlugin").append(SEPARATOR).append(actionPlugin).toString();
    }

    public static String pk(ActionDefinition actionDefinition) {
        if (actionDefinition == null) {
            return null;
        }
        return new StringBuilder("ActionDefinition")
                .append(SEPARATOR)
                .append(actionDefinition.getTenantId())
                .append(SEPARATOR)
                .append(actionDefinition.getActionPlugin())
                .append(SEPARATOR)
                .append(actionDefinition.getActionId())
                .toString();
    }

    public static String pk(String tenantId, String actionPlugin, String actionId) {
        if (tenantId == null || actionPlugin == null || actionId == null) {
            return null;
        }
        return new StringBuilder("ActionDefinition")
                .append(SEPARATOR)
                .append(tenantId)
                .append(SEPARATOR)
                .append(actionPlugin)
                .append(SEPARATOR)
                .append(actionId)
                .toString();
    }

    public static String pk(Condition condition) {
        if (condition == null) {
            return null;
        }
        return new StringBuilder("Condition")
                .append(SEPARATOR)
                .append(condition.getConditionId())
                .toString();
    }

    public static String pk(Dampening dampening) {
        if (dampening == null) {
            return null;
        }
        return pkFromDampeningId(dampening.getDampeningId());
    }

    public static String pkFromDampeningId(String dampeningId) {
        if (dampeningId == null) {
            return null;
        }
        return new StringBuilder("Dampening")
                .append(SEPARATOR)
                .append(dampeningId)
                .toString();
    }

    public static String pk(Trigger trigger) {
        if (trigger == null) {
            return null;
        }
        return pkFromTriggerId(trigger.getTenantId(), trigger.getId());
    }

    public static String pkFromTriggerId(String tenantId, String triggerId) {
        if (tenantId == null || triggerId == null) {
            return null;
        }
        return new StringBuilder("Trigger")
                .append(SEPARATOR)
                .append(tenantId)
                .append(SEPARATOR)
                .append(triggerId)
                .toString();
    }

    public static String pk(Event event) {
        if (event == null) {
            return null;
        }
        return new StringBuilder("Event")
                .append(SEPARATOR)
                .append(event.getTenantId())
                .append(SEPARATOR)
                .append(event.getId())
                .toString();
    }

    public static String pkFromEventId(String tenantId, String eventId) {
        if (tenantId == null || eventId == null) {
            return null;
        }
        return new StringBuilder("Event")
                .append(SEPARATOR)
                .append(tenantId)
                .append(SEPARATOR)
                .append(eventId)
                .toString();
    }
}
