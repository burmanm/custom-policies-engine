package org.hawkular.alerts.engine.impl.ispn.model.jpa;

import java.io.Serializable;

public class IspnActionPluginId implements Serializable {
    private String actionPlugin;

    /*
    TODO
2020-06-05 23:12:14,903 WARN  [org.hib.map.RootClass] (Quarkus Main Thread) HHH000038: Composite-id class does not override equals(): org.hawkular.alerts.engine.impl.ispn.model.jpa.IspnActionDefinitionId
2020-06-05 23:12:14,903 WARN  [org.hib.map.RootClass] (Quarkus Main Thread) HHH000039: Composite-id class does not override hashCode(): org.hawkular.alerts.engine.impl.ispn.model.jpa.IspnActionDefinitionId
2020-06-05 23:12:14,905 WARN  [org.hib.map.RootClass] (Quarkus Main Thread) HHH000038: Composite-id class does not override equals(): org.hawkular.alerts.engine.impl.ispn.model.jpa.IspnActionId
2020-06-05 23:12:14,905 WARN  [org.hib.map.RootClass] (Quarkus Main Thread) HHH000039: Composite-id class does not override hashCode(): org.hawkular.alerts.engine.impl.ispn.model.jpa.IspnActionId
2020-06-05 23:12:14,906 WARN  [org.hib.map.RootClass] (Quarkus Main Thread) HHH000038: Composite-id class does not override equals(): org.hawkular.alerts.engine.impl.ispn.model.jpa.IspnActionPluginId
2020-06-05 23:12:14,906 WARN  [org.hib.map.RootClass] (Quarkus Main Thread) HHH000039: Composite-id class does not override hashCode(): org.hawkular.alerts.engine.impl.ispn.model.jpa.IspnActionPluginId
2020-06-05 23:12:14,910 WARN  [org.hib.map.RootClass] (Quarkus Main Thread) HHH000038: Composite-id class does not override equals(): org.hawkular.alerts.engine.impl.ispn.model.jpa.IspnConditionId
2020-06-05 23:12:14,910 WARN  [org.hib.map.RootClass] (Quarkus Main Thread) HHH000039: Composite-id class does not override hashCode(): org.hawkular.alerts.engine.impl.ispn.model.jpa.IspnConditionId


     */

    // For Hibernate
    public IspnActionPluginId() {

    }

    public IspnActionPluginId(String id) {
        this.actionPlugin = id;
    }
}
