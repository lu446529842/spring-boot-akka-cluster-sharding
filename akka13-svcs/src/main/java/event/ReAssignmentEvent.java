package event;

import org.hibernate.validator.constraints.NotBlank;

/**
 * Event used for Re-Assignments
 * @author adtaylor
 *
 */
public class ReAssignmentEvent extends AssignmentEvent {

	/**
	 * 
	 * the user who was previously assigned to task
	 */
	@NotBlank private String previousAssigneeId;

	/**
	 * @return the previousAssigneeId
	 */
	public String getPreviousAssigneeId() {
		return previousAssigneeId;
	}

	/**
	 * @param previousAssigneeId the previousAssigneeId to set
	 */
	public void setPreviousAssigneeId(String previousAssigneeId) {
		this.previousAssigneeId = previousAssigneeId;
	}
	
}
