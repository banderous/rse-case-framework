package uk.gov.hmcts.ccf;

import static org.jooq.generated.Tables.CASES;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.jodah.typetools.TypeResolver;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DefaultDSLContext;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseUpdateViewEvent;

public class StateMachine<StateT, EventT extends Enum<EventT>, R extends Record> {

    public final String id;
    private final Table<R> eventTable;
    private final EventT initialEvent;
    private final TableField<R, Long> entityField;
    private final TableField<R, StateT> stateField;
    private final TableField<R, EventT> eventField;
    private final TableField<R, String> userField;
    private final TableField<R, Long> sequenceField;
    private final Class<EventT> enumClass;
    private final Supplier<Long> entityCreator;
    private StateT state;
    private Multimap<String, TransitionRecord> transitions = HashMultimap.create();
    private Collection<TransitionRecord> universalEvents = Lists.newArrayList();
    private Map<EventT, EventBuilder> events = Maps.newHashMap();
    private Map<EventT, TransitionRecord> dynamicEvents = Maps.newHashMap();
    private Class clazz;
    private BiConsumer initialHandler;
    private DefaultDSLContext jooq;

    private StateT initialState;

    public StateMachine(String id,
                        Class<EventT> enumClass,
                        DefaultDSLContext jooq,
                        Supplier<Long> creator,
                        EventT initialEvent,
                        Table<R> eventTable,
                        TableField<R, Long> entityField,
                        TableField<R, StateT> stateField,
                        TableField<R, EventT> eventField,
                        TableField<R, String> userField,
                        TableField<R, Long> sequenceField) {
        this.id = id;
        this.enumClass = enumClass;
        this.jooq = jooq;
        this.initialEvent = initialEvent;
        this.entityCreator = creator;
        this.eventTable = eventTable;
        this.entityField = entityField;
        this.stateField = stateField;
        this.eventField = eventField;
        this.userField = userField;
        this.sequenceField = sequenceField;
    }

    public CaseUpdateViewEvent getEvent(String userId, Long caseId, String event, String param) {
        return getEvent(userId, caseId, Enum.valueOf(enumClass, event), param);
    }

    public CaseUpdateViewEvent getEvent(String userId, Long caseId, EventT event, String param) {
        if (dynamicEvents.containsKey(event)) {
            TransitionRecord e = dynamicEvents.get(event);
            EventBuilder builder = new EventBuilder(e.clazz, event.toString(), event.toString());
            e.consumer.accept(new TransitionContext(userId, caseId, param), builder);
            return builder.build();
        }
        if (events.containsKey(event)) {
            return events.get(event).build();
        }
        throw new RuntimeException("Unknown event: " + event);
    }

    public <T> StateMachine<StateT, EventT, R> initialState(StateT state, BiConsumer<TransitionContext, T> c) {
        this.initialState = state;
        this.state = initialState;
        Class<?>[] typeArgs = TypeResolver.resolveRawArguments(BiConsumer.class, c.getClass());
        this.clazz = typeArgs[1];
        this.initialHandler = c;
        return this;
    }

    @SneakyThrows
    public long onCreated(String userId, JsonNode data) {
        state = initialState;
        Long id = entityCreator.get();
        saveEvent(initialEvent, new TransitionContext(userId, id));
        Object instance = new ObjectMapper().treeToValue(data, clazz);
        initialHandler.accept(new TransitionContext(userId, id), instance);
        return id;
    }

    public void handleEvent(String userId, Long entityId, EventT event, Object data) {
        handleEvent(userId, entityId, event, new ObjectMapper().valueToTree(data));
    }

    public void handleEvent(String userId, Long entityId, EventT event, JsonNode data) {
        handleEvent(new TransitionContext(userId, entityId), event, data);
    }

    public void handleEvent(TransitionContext context, String event, JsonNode data) {
        EventT e = Enum.valueOf(enumClass, event);
        handleEvent(context, e, data);
    }

    @SneakyThrows
    public void handleEvent(TransitionContext context, EventT event, JsonNode data) {
        ObjectMapper o = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule());
        for (TransitionRecord transitionRecord : transitions.get(state.toString())) {
            if (transitionRecord.event.equals(event)) {
                Object instance = o.treeToValue(data, transitionRecord.clazz);
                transitionRecord.consumer.accept(context, instance);
                state = transitionRecord.destination;
                saveEvent(event, context);
                return;
            }
        }

        for (TransitionRecord universalEvent : universalEvents) {
            if (universalEvent.event.equals(event)) {
                Object instance = o.treeToValue(data, universalEvent.clazz);
                universalEvent.consumer.accept(context, instance);
                saveEvent(event, context);
                return;
            }
        }
        throw new RuntimeException("Unhandled event:" + event);
    }

    void saveEvent(EventT event, TransitionContext context) {
        jooq.insertInto(eventTable)
            .columns(eventField, entityField, stateField, userField)
            .values(event, context.entityId, state, context.userId)
            .execute();
    }

    public StateT getState() {
        return state;
    }

    public <T> StateMachine<StateT, EventT, R> addUniversalEvent(EventT event,
                                                                 BiConsumer<TransitionContext, T> consumer) {
        Class<?>[] typeArgs = TypeResolver.resolveRawArguments(BiConsumer.class, consumer.getClass());
        universalEvents.add(new TransitionRecord(null, event, typeArgs[1], consumer));
        return this;
    }

    public <T> EventBuilder<T> addTransition(StateT from, StateT to, EventT event,
                                             BiConsumer<TransitionContext, T> consumer) {
        Class<?>[] typeArgs = TypeResolver.resolveRawArguments(BiConsumer.class, consumer.getClass());
        EventBuilder<T> result = new EventBuilder<T>((Class<T>) typeArgs[1], event.toString(), event.toString());
        transitions.put(from.toString(), new TransitionRecord(to, event, typeArgs[1], consumer));
        result.name("Confirm service");
        events.put(event, result);
        return result;
    }

    @SuppressWarnings("unchecked")
    public <T> EventBuilder<T> addEvent(StateT state, EventT event,
                                                     BiConsumer<TransitionContext, T> consumer) {
        Class<?>[] typeArgs = TypeResolver.resolveRawArguments(BiConsumer.class, consumer.getClass());
        EventBuilder<T> result = new EventBuilder<T>((Class<T>) typeArgs[1], event.toString(), event.toString());
        transitions.put(state.toString(), new TransitionRecord(state, event, typeArgs[1], consumer));
        events.put(event, result);
        return result;
    }

    public <T> StateMachine<StateT, EventT, R> dynamicEvent(StateT state, EventT event,
                                                         BiConsumer<TransitionContext, T> consumer,
                                                         BiConsumer<TransitionContext, EventBuilder<T>> builder) {
        Class<?>[] typeArgs = TypeResolver.resolveRawArguments(BiConsumer.class, consumer.getClass());
        dynamicEvents.put(event, new TransitionRecord(state, event, typeArgs[1], builder));
        transitions.put(state.toString(), new TransitionRecord(state, event, typeArgs[1], consumer));
        return this;
    }

    public Set<EventT> getAvailableActions(StateT state) {
        return getAvailableActions(state.toString());
    }

    public Set<EventT> getAvailableActions(String state) {
        Set<EventT> result = Sets.newHashSet();
        for (TransitionRecord transitionRecord : transitions.get(state)) {
            result.add(transitionRecord.getEvent());
        }
        for (TransitionRecord universalEvent : universalEvents) {
            result.add(universalEvent.getEvent());
        }

        return result;
    }

    public void rehydrate(Long id) {
        state = jooq.select(stateField)
            .from(eventTable)
            .where(entityField.eq(id))
            .orderBy(sequenceField.desc())
            .limit(1)
            .fetchSingle(stateField);
    }

    public void rehydrate(StateT state) {
        this.state = state;
    }

    @Data
    @AllArgsConstructor
    @RequiredArgsConstructor
    private class TransitionRecord {
        private final StateT destination;
        private final EventT event;
        private Class clazz;
        private BiConsumer consumer;
    }

    @Data
    @RequiredArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TransitionContext {
        private final String userId;
        private final Long entityId;
        private String param;
    }
}
