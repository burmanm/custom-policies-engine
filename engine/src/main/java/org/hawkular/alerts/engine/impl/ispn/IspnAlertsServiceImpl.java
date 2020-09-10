package org.hawkular.alerts.engine.impl.ispn;

import org.eclipse.microprofile.config.ConfigProvider;
import org.hawkular.alerts.api.model.Note;
import org.hawkular.alerts.api.model.condition.ConditionEval;
import org.hawkular.alerts.api.model.data.Data;
import org.hawkular.alerts.api.model.event.Alert;
import org.hawkular.alerts.api.model.event.Alert.Status;
import org.hawkular.alerts.api.model.event.Event;
import org.hawkular.alerts.api.model.event.EventType;
import org.hawkular.alerts.api.model.paging.AlertComparator;
import org.hawkular.alerts.api.model.paging.EventComparator;
import org.hawkular.alerts.api.model.paging.Order;
import org.hawkular.alerts.api.model.paging.Page;
import org.hawkular.alerts.api.model.paging.PageContext;
import org.hawkular.alerts.api.model.paging.Pager;
import org.hawkular.alerts.api.model.trigger.Mode;
import org.hawkular.alerts.api.model.trigger.Trigger;
import org.hawkular.alerts.api.services.ActionsService;
import org.hawkular.alerts.api.services.AlertsCriteria;
import org.hawkular.alerts.api.services.AlertsService;
import org.hawkular.alerts.api.services.DefinitionsService;
import org.hawkular.alerts.api.services.EventsCriteria;
import org.hawkular.alerts.engine.cache.IspnCacheManager;
import org.hawkular.alerts.engine.impl.IncomingDataManagerImpl;
import org.hawkular.alerts.engine.impl.hibernate.HibernateSearchQueryCreator;
import org.hawkular.alerts.engine.impl.ispn.model.IspnEvent;
import org.hawkular.alerts.engine.service.AlertsEngine;
import org.hawkular.alerts.engine.service.IncomingDataManager;
import org.hawkular.alerts.log.AlertingLogger;
import org.hawkular.alerts.log.MsgLogging;
import org.hibernate.search.query.dsl.MustJunction;
import org.infinispan.Cache;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.Search;
import org.infinispan.query.SearchManager;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.hawkular.alerts.api.util.Util.isEmpty;
import static org.hawkular.alerts.engine.impl.ispn.IspnPk.pk;
import static org.hawkular.alerts.engine.impl.ispn.IspnPk.pkFromEventId;
import static org.hawkular.alerts.engine.util.Utils.extractCategories;
import static org.hawkular.alerts.engine.util.Utils.extractEventIds;
import static org.hawkular.alerts.engine.util.Utils.extractTriggerIds;

/**
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public class IspnAlertsServiceImpl implements AlertsService {
    private static final AlertingLogger log = MsgLogging.getMsgLogger(AlertingLogger.class, IspnAlertsServiceImpl.class);

    AlertsEngine alertsEngine;

    DefinitionsService definitionsService;

    ActionsService actionsService;

    IncomingDataManager incomingDataManager;

    Cache<String, Object> backend;

    QueryFactory queryFactory;

    SearchManager searchManager;

    long eventLifespanInHours;
    long alertsLifespanInHours;

    boolean saveThinAlerts = false;

    public void init() {
        alertsLifespanInHours = ConfigProvider.getConfig().getValue("engine.backend.ispn.alerts-lifespan", Long.class);
        eventLifespanInHours = ConfigProvider.getConfig().getValue("engine.backend.ispn.events-lifespan", Long.class);
        saveThinAlerts = ConfigProvider.getConfig().getValue("engine.backend.ispn.alerts-thin", Boolean.class);
        backend = IspnCacheManager.getCacheManager().getCache("backend");
        if (backend == null) {
            log.error("Ispn backend cache not found. Check configuration.");
            throw new RuntimeException("backend cache not found");
        }
        queryFactory = Search.getQueryFactory(backend);
        searchManager = Search.getSearchManager(backend);
    }

    public void setAlertsEngine(AlertsEngine alertsEngine) {
        this.alertsEngine = alertsEngine;
    }

    public void setDefinitionsService(DefinitionsService definitionsService) {
        this.definitionsService = definitionsService;
    }

    public void setActionsService(ActionsService actionsService) {
        this.actionsService = actionsService;
    }

    public void setIncomingDataManager(IncomingDataManager incomingDataManager) {
        this.incomingDataManager = incomingDataManager;
    }

    private void store(Event event) {
        long ttl = eventLifespanInHours;
        if(event instanceof Alert) {
            ttl = alertsLifespanInHours;
        }
        if(ttl < 0) {
            backend.put(pk(event), new IspnEvent(event));
        } else {
            backend.put(pk(event), new IspnEvent(event), ttl, TimeUnit.HOURS);
        }
    }

    @Override
    public void ackAlerts(String tenantId, Collection<String> alertIds, String ackBy, String ackNotes) throws Exception {
        if (isEmpty(tenantId)) {
            throw new IllegalArgumentException("TenantId must be not null");
        }
        if (isEmpty(alertIds)) {
            return;
        }

        AlertsCriteria criteria = new AlertsCriteria();
        criteria.setAlertIds(alertIds);
        List<Alert> alertsToAck = getAlerts(tenantId, criteria, null);

        long timestamp = System.currentTimeMillis();
        for (Alert alert : alertsToAck) {
            List<Note> notes = null;
            if(!isEmpty(ackBy)) {
                notes = List.of(new Note(ackBy, ackNotes));
            }
            alert.addLifecycle(Status.ACKNOWLEDGED, timestamp, notes);
            store(alert);
            sendAction(alert);
        }
    }

    @Override
    public void addAlerts(Collection<Alert> alerts) throws Exception {
        if (alerts == null) {
            throw new IllegalArgumentException("Alerts must be not null");
        }
        if (alerts.isEmpty()) {
            return;
        }
        log.debugf("Adding %s alerts", alerts.size());
        for (Alert alert : alerts) {
            if(saveThinAlerts) {
                // This reduces the storage requirements by not storing runtime evaluation information
                alert.setDampening(null);
                alert.setEvalSets(null);
                alert.setResolvedEvalSets(null);
            }
            store(alert);
        }
    }

    @Override
    public void addAlertTags(String tenantId, Collection<String> alertIds, Map<String, String> tags) throws Exception {
        if (isEmpty(tenantId)) {
            throw new IllegalArgumentException("TenantId must be not null");
        }
        if (isEmpty(alertIds)) {
            throw new IllegalArgumentException("AlertIds must be not null");
        }
        if (isEmpty(tags)) {
            throw new IllegalArgumentException("Tags must be not null");
        }

        AlertsCriteria criteria = new AlertsCriteria();
        criteria.setAlertIds(alertIds);
        Page<Alert> existingAlerts = getAlerts(tenantId, criteria, null);

        for (Alert alert : existingAlerts) {
            tags.entrySet().stream().forEach(tag -> alert.addTag(tag.getKey(), tag.getValue()));
            store(alert);
        }
    }

    @Override
    public void addEvents(Collection<Event> events) throws Exception {
        if (null == events || events.isEmpty()) {
            return;
        }
        persistEvents(events);
        sendEvents(events);
    }

    @Override
    public void addEventTags(String tenantId, Collection<String> eventIds, Map<String, String> tags) throws Exception {
        if (isEmpty(tenantId)) {
            throw new IllegalArgumentException("TenantId must be not null");
        }
        if (isEmpty(eventIds)) {
            throw new IllegalArgumentException("AlertIds must be not null");
        }
        if (isEmpty(tags)) {
            throw new IllegalArgumentException("Tags must be not null");
        }

        EventsCriteria criteria = new EventsCriteria();
        criteria.setEventIds(eventIds);
        Page<Event> existingEvents = getEvents(tenantId, criteria, null);

        for (Event event : existingEvents) {
            tags.entrySet().stream().forEach(tag -> event.addTag(tag.getKey(), tag.getValue()));
            store(event);
        }
    }

    @Override
    public void persistEvents(Collection<Event> events) throws Exception {
        if (events == null) {
            throw new IllegalArgumentException("Events must be not null");
        }
        if (events.isEmpty()) {
            return;
        }
        log.debugf("Adding %s events", events.size());
        for (Event event : events) {
            store(event);
        }
    }

    @Override
    public void addNote(String tenantId, String alertId, String user, String text) throws Exception {
        if (isEmpty(tenantId)) {
            throw new IllegalArgumentException("TenantId must be not null");
        }
        if (isEmpty(alertId)) {
            throw new IllegalArgumentException("AlertId must be not null");
        }
        if (isEmpty(user) || isEmpty(text)) {
            throw new IllegalArgumentException("user or text must be not null");
        }

        Alert alert = getAlert(tenantId, alertId, false);
        if (alert == null) {
            return;
        }

        alert.addNote(new Note(user, System.currentTimeMillis(), text));

        store(alert);
    }

    @Override
    public int deleteAlerts(String tenantId, AlertsCriteria criteria) throws Exception {
        if (isEmpty(tenantId)) {
            throw new IllegalArgumentException("TenantId must be not null");
        }
        if (null == criteria) {
            throw new IllegalArgumentException("Criteria must be not null");
        }
        // no need to fetch the evalSets to perform the necessary deletes
        criteria.setThin(true);
        List<Alert> alertsToDelete = getAlerts(tenantId, criteria, null);

        if (alertsToDelete.isEmpty()) {
            return 0;
        }
        try {
            backend.startBatch();
            for (Alert alert : alertsToDelete) {
                backend.remove(pk(alert));
            }
            backend.endBatch(true);
        } catch (Exception e) {
            backend.endBatch(false);
            throw e;
        }
        return alertsToDelete.size();
    }

    @Override
    public int deleteEvents(String tenantId, EventsCriteria criteria) throws Exception {
        if (isEmpty(tenantId)) {
            throw new IllegalArgumentException("TenantId must be not null");
        }
        if (null == criteria) {
            throw new IllegalArgumentException("Criteria must be not null");
        }
        // no need to fetch the evalSets to perform the necessary deletes
        criteria.setThin(true);
        List<Event> eventsToDelete = getEvents(tenantId, criteria, null);

        if (eventsToDelete.isEmpty()) {
            return 0;
        }
        try {
            backend.startBatch();
            for (Event event : eventsToDelete) {
                backend.remove(pk(event));
            }
            backend.endBatch(true);
        } catch (Exception e) {
            backend.endBatch(false);
            throw e;
        }
        return eventsToDelete.size();
    }

    @Override
    public Alert getAlert(String tenantId, String alertId, boolean thin) throws Exception {
        if (isEmpty(tenantId)) {
            throw new IllegalArgumentException("TenantId must be not null");
        }
        if (isEmpty(alertId)) {
            throw new IllegalArgumentException("AlertId must be not null");
        }

        String pk = pkFromEventId(tenantId, alertId);
        IspnEvent ispnEvent = (IspnEvent) backend.get(pk);
        return ispnEvent != null && ispnEvent.getEvent() instanceof Alert ? (Alert) ispnEvent.getEvent() : null;
    }

    @Override
    public Page<Alert> getAlerts(String tenantId, AlertsCriteria criteria, Pager pager) throws Exception {
        return getAlerts(Collections.singleton(tenantId), criteria, pager);
    }

    @Override
    public Page<Alert> getAlerts(Set<String> tenantIds, AlertsCriteria criteria, Pager pager) throws Exception {
        if (isEmpty(tenantIds)) {
            throw new IllegalArgumentException("TenantIds must be not null");
        }

        //////
//        List<IspnTrigger> triggers;
//        if (criteria != null && criteria.hasCriteria()) {
//            org.hibernate.search.query.dsl.QueryBuilder queryBuilder = searchManager.buildQueryBuilderForClass(IspnTrigger.class).get();
//            org.apache.lucene.search.Query tenantQuery = queryBuilder.keyword().onField("tenantId").matching(tenantId).createQuery();
//
//            org.apache.lucene.search.Query values = HibernateSearchQueryCreator.evaluate(queryBuilder, criteria.getQuery());
//
//            org.apache.lucene.search.Query finalQuery = queryBuilder.bool().must(tenantQuery).must(values).createQuery();
//
//            CacheQuery<IspnTrigger> query = searchManager.getQuery(finalQuery, IspnTrigger.class);
//            triggers = query.list();
//        } else {
//            triggers = queryFactory.from(IspnTrigger.class)
//                    .having("tenantId")
//                    .eq(tenantId)
//                    .build()
//                    .list();
//        }
        //////

        if(criteria == null) {
            criteria = new AlertsCriteria();
        }

        // Set the query starting point to the earliest retention time to prevent incorrect
        // return of the Query maxResults
        long earliestRetentionTime = Instant.now().minus(alertsLifespanInHours, ChronoUnit.HOURS).toEpochMilli();

        if(criteria.getStartTime() == null || criteria.getStartTime() < earliestRetentionTime) {
            criteria.setStartTime(earliestRetentionTime);
        }

        org.hibernate.search.query.dsl.QueryBuilder queryBuilder = searchManager.buildQueryBuilderForClass(IspnEvent.class).get();
        // TODO Remove multi-tenant fetching from function
        org.apache.lucene.search.Query tenantQuery = queryBuilder.keyword().onField("tenantId").matching(tenantIds.iterator().next()).createQuery();
        // TODO Add alerts only searching
        org.apache.lucene.search.Query typeQuery = queryBuilder.keyword().onField("eventType").matching("ALERT").createQuery();
        org.apache.lucene.search.Query criteriaQuery = HibernateSearchQueryCreator.evaluate(queryBuilder, criteria.getQuery());
        MustJunction rulesPart = queryBuilder.bool().must(tenantQuery).must(typeQuery).must(criteriaQuery);
        if(criteria.hasTagQueryCriteria()) {
            org.apache.lucene.search.Query tagsQuery = HibernateSearchQueryCreator.evaluate(queryBuilder, criteria.getTagQuery());
            rulesPart = rulesPart.must(tagsQuery);
        }
        org.apache.lucene.search.Query finalQuery = rulesPart.createQuery();

       CacheQuery<IspnEvent> query = searchManager.getQuery(finalQuery, IspnEvent.class);
       // Add paging support here

        List<IspnEvent> ispnEvents = query.list();
//        return new Page(query.list(), pager, 1000);
/*
        Page<IspnEvent> list = query.list();

        Page<IspnEvent> ispnEvents = getEventItems(query, pager);
*/
        List<Alert> alerts = ispnEvents.stream().map(ispnEvent -> {
//            if (criteria != null && criteria.isThin()) {
//                Alert alert = new Alert((Alert) ispnEvent.getEvent());
//                alert.setDampening(null);
//                alert.setEvalSets(null);
//                alert.setResolvedEvalSets(null);
//                return alert;
//            }
            return (Alert) ispnEvent.getEvent();
        }).collect(Collectors.toList());

        return preparePage(alerts, pager, ispnEvents.size());
    }

    private Page<IspnEvent> getEventItems(StringBuilder builder, Pager pager) {
        // Parse and do the first ordering at the Infinispan level (for @SortableFields)
        if (pager != null && pager.getOrder() != null && !pager.getOrder().isEmpty() && pager.getOrder().get(0).isSpecific()) {
            log.debugf("Pager: %s", pager.toString());
            builder.append("ORDER BY ");

            if (AlertComparator.Field.ALERT_ID.getText().equals(pager.getOrder().get(0).getField())) {
                builder.append("id ");
            } else if (AlertComparator.Field.CTIME.getText().equals(pager.getOrder().get(0).getField())) {
                // The generated alert ids include ctime, so this should sort them correctly
                builder.append("ctime ");
            } else {
                builder.append("id ");
            }

            if (pager.getOrder().get(0).getDirection() == Order.Direction.DESCENDING) {
                builder.append("DESC");
            } else {
                builder.append("ASC");
            }
        } else {
            // Force id sorting by DESCENDING (newest events / alerts first) to be the natural order
            builder.append("ORDER BY ctime DESC");
        }

        Query parsedQuery = queryFactory.create(builder.toString());
        long totalSize = parsedQuery.getResultSize();

        // Do limitations at Infinispan level if possible
        if(pager != null) {
//            // If we sort outside the ISPN, we need to also filter result set outside
            if(isServerSideSorted(pager)) {
                // We need to create a new one, as ISPN would otherwise just reuse the previous results,
                // not applying the paging
                parsedQuery = queryFactory.create(builder.toString());
                if (pager.getStart() > 0) {
                    parsedQuery.startOffset(pager.getStart());
                }
                if (pager.getPageSize() != PageContext.UNLIMITED_PAGE_SIZE) {
                    parsedQuery.maxResults(pager.getPageSize());
                }
            }
        }
        if(log.isDebugEnabled()) {
            log.debugf("ParsedQuery: %s, maxResults: %d, startOffset: %d", parsedQuery.getQueryString(), parsedQuery.getMaxResults(), parsedQuery.getStartOffset());
        }

        return new Page(parsedQuery.list(), pager, totalSize);
    }

    @Override
    public Event getEvent(String tenantId, String eventId, boolean thin) throws Exception {
        if (isEmpty(tenantId)) {
            throw new IllegalArgumentException("TenantId must be not null");
        }
        if (isEmpty(eventId)) {
            throw new IllegalArgumentException("EventId must be not null");
        }

        String pk = pkFromEventId(tenantId, eventId);
        IspnEvent ispnEvent = (IspnEvent) backend.get(pk);
        return ispnEvent != null ? ispnEvent.getEvent() : null;
    }

    @Override
    public Page<Event> getEvents(String tenantId, EventsCriteria criteria, Pager pager) throws Exception {
        return getEvents(Collections.singleton(tenantId), criteria, pager);
    }

    @Override
    public Page<Event> getEvents(Set<String> tenantIds, EventsCriteria criteria, Pager pager) throws Exception {
        if (isEmpty(tenantIds)) {
            throw new IllegalArgumentException("TenantIds must be not null");
        }
        boolean filter = (null != criteria && criteria.hasCriteria());
        if (filter && log.isDebugEnabled()) {
            log.debugf("getEvents criteria: %s", criteria.toString());
        }

        StringBuilder query = new StringBuilder("from org.hawkular.alerts.engine.impl.ispn.model.IspnEvent where ");
        query.append("(");
        Iterator<String> iter = tenantIds.iterator();
        while (iter.hasNext()) {
            String tenantId = iter.next();
            query.append("tenantId = '").append(tenantId).append("' ");
            if (iter.hasNext()) {
                query.append("or ");
            }
        }
        query.append(") ");

        if (filter) {
            if (criteria.hasEventTypeCriteria()) {
                try {
                    EventType eventType = EventType.valueOf(criteria.getEventType());
                    query.append("and eventType = '").append(eventType.name()).append("' ");
                } catch (Exception e) {
                    log.debugf("EventType [%s] is not valid, ignoring this criteria", criteria.getEventType());
                }
            }
            if (criteria.hasEventIdCriteria()) {
                query.append("and (");
                iter = extractEventIds(criteria).iterator();
                while (iter.hasNext()) {
                    String eventId = iter.next();
                    query.append("id = '").append(eventId).append("' ");
                    if (iter.hasNext()) {
                        query.append("or ");
                    }
                }
                query.append(") ");
            }
//            if (criteria.hasTagQueryCriteria()) {
//                query.append("and (tags : ");
////                parseTagQuery(criteria.getTagQuery(), query);
//                query.append(") ");
//            }
            if (criteria.hasTriggerIdCriteria()) {
                query.append("and (");
                iter = extractTriggerIds(criteria).iterator();
                while (iter.hasNext()) {
                    String triggerId = iter.next();
                    query.append("triggerId = '").append(triggerId).append("' ");
                    if (iter.hasNext()) {
                        query.append("or ");
                    }
                }
                query.append(") ");
            }

            // Alerts are also events, so we need to pick the largest one. This does not prevent the error with maxResults sadly
            long largestRetention = Math.max(alertsLifespanInHours, eventLifespanInHours);
            long earliestRetentionTime = Instant.now().minus(largestRetention, ChronoUnit.HOURS).toEpochMilli();

            if(criteria.getStartTime() == null || criteria.getStartTime() < earliestRetentionTime) {
                criteria.setStartTime(earliestRetentionTime);
            }

            if (criteria.hasCTimeCriteria()) {
                query.append("and (");
                if (criteria.getStartTime() != null) {
                    query.append("ctime >= ").append(criteria.getStartTime()).append(" ");
                }
                if (criteria.getEndTime() != null) {
                    if (criteria.getStartTime() != null) {
                        query.append("and ");
                    }
                    query.append("ctime <= ").append(criteria.getEndTime()).append(" ");
                }
                query.append(") ");
            }
            if (criteria.hasCategoryCriteria()) {
                query.append("and (");
                iter = extractCategories(criteria).iterator();
                while (iter.hasNext()) {
                    String category = iter.next();
                    query.append("category = '").append(category).append("' ");
                    if (iter.hasNext()) {
                        query.append(" or ");
                    }
                }
                query.append(") ");
            }
        }

        Page<IspnEvent> ispnEvents = getEventItems(query, pager);
        List<Event> events = ispnEvents.stream().map(e -> e.getEvent()).collect(Collectors.toList());
        return prepareEventsPage(events, pager, ispnEvents.getTotalSize());
    }

    @Override
    public void removeAlertTags(String tenantId, Collection<String> alertIds, Collection<String> tags) throws Exception {
        if (isEmpty(tenantId)) {
            throw new IllegalArgumentException("TenantId must be not null");
        }
        if (isEmpty(alertIds)) {
            throw new IllegalArgumentException("AlertIds must be not null");
        }
        if (isEmpty(tags)) {
            throw new IllegalArgumentException("Tags must be not null");
        }

        // Only untag existing alerts
        AlertsCriteria criteria = new AlertsCriteria();
        criteria.setAlertIds(alertIds);
        Page<Alert> existingAlerts = getAlerts(tenantId, criteria, null);

        for (Alert alert : existingAlerts) {
            boolean modified = false;
            for (String tag : tags) {
                if (alert.getTags().containsKey(tag)) {
                    alert.removeTag(tag);
                    modified = true;
                }
            }
            if (modified) {
                store(alert);
            }
        }
    }

    @Override
    public void removeEventTags(String tenantId, Collection<String> eventIds, Collection<String> tags) throws Exception {
        if (isEmpty(tenantId)) {
            throw new IllegalArgumentException("TenantId must be not null");
        }
        if (isEmpty(eventIds)) {
            throw new IllegalArgumentException("EventIds must be not null");
        }
        if (isEmpty(tags)) {
            throw new IllegalArgumentException("Tags must be not null");
        }

        // Only untag existing events
        EventsCriteria criteria = new EventsCriteria();
        criteria.setEventIds(eventIds);
        Page<Event> existingEvents = getEvents(tenantId, criteria, null);

        for (Event event : existingEvents) {
            boolean modified = false;
            for (String tag : tags) {
                if (event.getTags().containsKey(tag)) {
                    event.removeTag(tag);
                    modified = true;
                }
            }
            if (modified) {
                store(event);
            }
        }
    }

    @Override
    public void resolveAlerts(String tenantId, Collection<String> alertIds, String resolvedBy, String resolvedNotes, List<Set<ConditionEval>> resolvedEvalSets) throws Exception {
        if (isEmpty(tenantId)) {
            throw new IllegalArgumentException("TenantId must be not null");
        }
        if (isEmpty(alertIds)) {
            return;
        }

        if (isEmpty(resolvedBy)) {
            resolvedBy = "unknown";
        }
        if (isEmpty(resolvedNotes)) {
            resolvedNotes = "none";
        }

        AlertsCriteria criteria = new AlertsCriteria();
        criteria.setAlertIds(alertIds);
        List<Alert> alertsToResolve = getAlerts(tenantId, criteria, null);

        // resolve the alerts
        long timestamp = System.currentTimeMillis();
        for (Alert alert : alertsToResolve) {
            List<Note> notes = List.of(new Note(resolvedBy, timestamp, resolvedNotes));
            alert.setResolvedEvalSets(resolvedEvalSets);
            alert.addLifecycle(Status.RESOLVED, timestamp, notes);
            store(alert);
            sendAction(alert);
        }

        // gather the triggerIds of the triggers we need to check for resolve options
        Set<String> triggerIds = alertsToResolve.stream().map(Alert::getTriggerId).collect(Collectors.toSet());

        // handle resolve options
        triggerIds.forEach(tid -> handleResolveOptions(tenantId, tid, true));

    }

    @Override
    public void resolveAlertsForTrigger(String tenantId, String triggerId, String resolvedBy, String resolvedNotes, List<Set<ConditionEval>> resolvedEvalSets) throws Exception {
        if (isEmpty(tenantId)) {
            throw new IllegalArgumentException("TenantId must be not null");
        }
        if (isEmpty(triggerId)) {
            throw new IllegalArgumentException("TriggerId must be not null");
        }

        AlertsCriteria criteria = new AlertsCriteria();
        criteria.setTriggerId(triggerId);
        criteria.setStatusSet(EnumSet.complementOf(EnumSet.of(Status.RESOLVED)));
        List<Alert> alertsToResolve = getAlerts(tenantId, criteria, null);

        long timestamp = System.currentTimeMillis();
        for (Alert alert : alertsToResolve) {
            List<Note> notes = null;
            if(!isEmpty(resolvedBy)) {
                notes = List.of(new Note(resolvedBy, timestamp, resolvedNotes));
            }
            alert.setResolvedEvalSets(resolvedEvalSets);
            alert.addLifecycle(Status.RESOLVED, timestamp, notes);
            store(alert);
            sendAction(alert);
        }

        handleResolveOptions(tenantId, triggerId, false);
    }

    @Override
    public void sendData(Collection<Data> data) throws Exception {
        sendData(data, false);
    }

    @Override
    public void sendData(Collection<Data> data, boolean ignoreFiltering) throws Exception {
        if (isEmpty(data)) {
            return;
        }

        if (incomingDataManager == null) {
            log.debug("incomingDataManager is not defined. Only valid for testing.");
            return;
        }

        incomingDataManager.bufferData(new IncomingDataManagerImpl.IncomingData(data, !ignoreFiltering));
    }

    @Override
    public void sendEvents(Collection<Event> events) throws Exception {
        sendEvents(events, false);
    }

    @Override
    public void sendEvents(Collection<Event> events, boolean ignoreFiltering) throws Exception {
        if (isEmpty(events)) {
            return;
        }

        if (incomingDataManager == null) {
            log.debug("incomingDataManager is not defined. Only valid for testing.");
            return;
        }

        incomingDataManager.bufferEvents(new IncomingDataManagerImpl.IncomingEvents(events, !ignoreFiltering));
    }

    private boolean isServerSideSorted(Pager pager) {
        if(pager == null || pager.getOrder() == null || pager.getOrder().isEmpty()) {
            return true;
        }
        String field = pager.getOrder().get(0).getField();
        return field == null || AlertComparator.Field.ALERT_ID.getText().equals(field)
                || AlertComparator.Field.CTIME.getText().equals(field);
    }

    // Private methods
    // TODO Merge preparePage and prepareEventsPage, EventComparator and AlertsComparator

    private Page<Alert> preparePage(List<Alert> alerts, Pager pager, long totalSize) {
        if (pager != null) {
            if (pager.getOrder() != null
                    && !pager.getOrder().isEmpty()
                    && pager.getOrder().get(0).getField() == null) {
                pager = Pager.builder()
                        .withPageSize(pager.getPageSize())
                        .withStartPage(pager.getPageNumber())
                        .orderBy(AlertComparator.Field.ALERT_ID.getText(), Order.Direction.ASCENDING).build();
            }
            if (pager.getOrder() != null) {
                pager.getOrder().stream()
                        .filter(o -> o.getField() != null && o.getDirection() != null)
                        .forEach(o -> {
                            AlertComparator comparator = new AlertComparator(o.getField(), o.getDirection());
                            alerts.sort(comparator);
                        });
            }
            if(!isServerSideSorted(pager) && pager.isLimited()) {
                // We need to filter the amounts here
                if(pager.getEnd() >= alerts.size()) {
                    return new Page<>(alerts.subList(pager.getStart(), alerts.size()), pager, alerts.size());
                }
                return new Page<>(alerts.subList(pager.getStart(), pager.getEnd()), pager, alerts.size());
            }
        } else {
            AlertComparator.Field defaultField = AlertComparator.Field.ALERT_ID;
            Order.Direction defaultDirection = Order.Direction.ASCENDING;
            AlertComparator comparator = new AlertComparator(defaultField.getText(), defaultDirection);
            pager = Pager.builder().withPageSize(alerts.size()).orderBy(defaultField.getText(), defaultDirection)
                    .build();
            alerts.sort(comparator);
        }

        return new Page<>(alerts, pager, totalSize);
    }

    private void sendAction(Alert a) {
        if (actionsService != null && a != null && a.getTrigger() != null) {
            actionsService.send(a.getTrigger(), a);
        }
    }

    private void handleResolveOptions(String tenantId, String triggerId, boolean checkIfAllResolved) {

        if (definitionsService == null || alertsEngine == null) {
            log.debug("definitionsService or alertsEngine are not defined. Only valid for testing.");
            return;
        }

        try {
            Trigger trigger = definitionsService.getTrigger(tenantId, triggerId);
            if (null == trigger) {
                return;
            }

            boolean setEnabled = trigger.isAutoEnable() && !trigger.isEnabled();
            boolean setFiring = trigger.isAutoResolve();

            // Only reload the trigger if it is not already in firing mode, otherwise we could lose partial matching.
            // This is a rare case because a trigger with autoResolve=true will not be in firing mode with an
            // unresolved trigger. But it is possible, either by mistake, or timing,  for a client to try and
            // resolve an already-resolved alert.
            if (setFiring) {
                Trigger loadedTrigger = alertsEngine.getLoadedTrigger(trigger);
                if (null != loadedTrigger && Mode.FIRING == loadedTrigger.getMode()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Ignoring setFiring, loaded Trigger already in firing mode " +
                                loadedTrigger.toString());
                    }
                    setFiring = false;
                }
            }

            if (!(setEnabled || setFiring)) {
                return;
            }

            boolean allResolved = true;
            if (checkIfAllResolved) {
                AlertsCriteria ac = new AlertsCriteria();
                ac.setTriggerId(triggerId);
                ac.setStatusSet(EnumSet.complementOf(EnumSet.of(Status.RESOLVED)));
                Page<Alert> unresolvedAlerts = getAlerts(tenantId, ac, new Pager(0, 1, Order.unspecified()));
                allResolved = unresolvedAlerts.isEmpty();
            }

            if (!allResolved) {
                log.debugf("Ignoring resolveOptions, not all Alerts for Trigger %s are resolved", trigger.toString());
                return;
            }

            // Either update the trigger, which implicitly reloads the trigger (and as such resets to firing mode)
            // or perform an explicit reload to reset to firing mode.
            if (setEnabled) {
                trigger.setEnabled(true);
                definitionsService.updateTrigger(tenantId, trigger, true);
            } else {
                alertsEngine.reloadTrigger(tenantId, triggerId);
            }
        } catch (Exception e) {
            log.errorDatabaseException(e.getMessage());
        }
    }

    private Page<Event> prepareEventsPage(List<Event> events, Pager pager, long totalSize) {
        if (pager != null) {
            if (pager.getOrder() != null
                    && !pager.getOrder().isEmpty()
                    && pager.getOrder().get(0).getField() == null) {
                pager = Pager.builder()
                        .withPageSize(pager.getPageSize())
                        .withStartPage(pager.getPageNumber())
                        .orderBy(EventComparator.Field.ID.getName(), Order.Direction.ASCENDING).build();
            }
            if (pager.getOrder() != null) {
                pager.getOrder()
                        .stream()
                        .filter(o -> o.getField() != null && o.getDirection() != null)
                        .forEach(o -> {
                            EventComparator comparator = new EventComparator(o.getField(), o.getDirection());
                            events.sort(comparator);
                        });
            }
            if(!isServerSideSorted(pager) && pager.isLimited()) {
                /*
            if (!pager.isLimited() || ordered.size() < pager.getStart()) {
                pager = new Pager(0, ordered.size(), pager.getOrder());
                return new Page<>(ordered, pager, ordered.size());
            }
            if (pager.getEnd() >= ordered.size()) {
                return new Page<>(ordered.subList(pager.getStart(), ordered.size()), pager, ordered.size());
            }
            return new Page<>(ordered.subList(pager.getStart(), pager.getEnd()), pager, ordered.size());
                 */

                // TODO What if the start is larger than the amount? Will it crash?

                // We need to filter the amounts here
                if(pager.getEnd() >= events.size()) {
                    return new Page<>(events.subList(pager.getStart(), events.size()), pager, events.size());
                }
                return new Page<>(events.subList(pager.getStart(), pager.getEnd()), pager, events.size());
            }
        } else {
            EventComparator.Field defaultField = EventComparator.Field.ID;
            Order.Direction defaultDirection = Order.Direction.ASCENDING;
            pager = Pager.builder().withPageSize(events.size()).orderBy(defaultField.getName(),
                    defaultDirection).build();
            EventComparator comparator = new EventComparator(defaultField.getName(), defaultDirection);
            Collections.sort(events, comparator);
        }
        return new Page<>(events, pager, totalSize);
    }

}
