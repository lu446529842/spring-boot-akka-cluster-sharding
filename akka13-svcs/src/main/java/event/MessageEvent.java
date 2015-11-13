package event;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Created by davenkat on 9/24/2015.
 */
public interface MessageEvent extends Serializable {
    public String getEventType();

    public String getFlowDefName();

    public void setFlowDefName(String flowDefName);

    public Date getCreated();

    public void setCreated(Date created);

    public String getCreatedBy();

    public void setCreatedBy(String createdBy);

    public UUID getEventId();

    public void setEventId(UUID eventId);

    public String getEventName();

    public void setEventName(String eventName);

    public String getTenantId();

    public void setTenantId(String tenantId);

    public UUID getModuleId();

    public void setModuleId(UUID moduleId);

    public String getModuleTaskDescription();

    public void setModuleTaskDescription(String moduleTaskDescription);

    public String getModuleType();

    public void setModuleType(String moduleType);

    public String getModuleTypeOfTask();

    public void setModuleTypeOfTask(String moduleTypeOfTask);

}
