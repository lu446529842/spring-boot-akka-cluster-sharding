package event;

import java.util.Date;
import java.util.UUID;

/**
 * Base event class for EDF.
 */
public abstract class BaseEDFEvent implements MessageEvent {

    private String flowDefName;
    private UUID eventId;
    private String eventName; //the next step value, this could be current step or next step.
    private String tenantId;
    private Date created;
    private String createdBy;

    @Override
    public String getFlowDefName() {
        return flowDefName;
    }

    @Override
    public void setFlowDefName(String flowDefName) {
        this.flowDefName = flowDefName;
    }

    @Override
    public Date getCreated() {
        return created;
    }

    @Override
    public void setCreated(Date created) {
        this.created = created;
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    @Override
    public String getEventName() {
        return eventName;
    }

    @Override
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    @Override
    public String getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
