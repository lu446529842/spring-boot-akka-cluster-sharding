package event;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.math.RandomUtils;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.Random;

/**
 * this is a placeholder for the actual event. this will likely reside in a place common to both assignment and edf-flow
 * @author adtaylor
 *
 */
public class AssignmentEvent  extends AppEDFEvent {

    public static enum EventType {
        ASSIGN,
        ACQUIRE,
        CLOSE
    }

	public AssignmentEvent() {

	}
	
	@NotBlank private String assigneeId;
	@NotBlank private String assignedById;
    @NotNull private EventType eventType;
	//@Pattern(regexp = "ASSIGNED|INPROGRESS|CLOSED") // TODO: should we do this?

	
	public String getAssigneeId() {
		return assigneeId;
	}
	public void setAssigneeId(String assigneeId) {
		this.assigneeId = assigneeId;
	}
	public String getAssignedById() {
		return assignedById;
	}
	public void setAssignedById(String assignedById) {
		this.assignedById = assignedById;
	}

    @Override
    public String getEventType() {
        return eventType.toString();
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

	@Override
	public int hashCode() {
		return new HashCodeBuilder(155, 23).
				append(eventType).
				append(getEventName()).
				append(getAssignedById()).
				toHashCode();
	}
}
