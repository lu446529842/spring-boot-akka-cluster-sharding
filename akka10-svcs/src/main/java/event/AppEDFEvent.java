package event;

import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 *
 */
public abstract class AppEDFEvent extends BaseEDFEvent {


    @NotNull
    private UUID moduleId;
    @NotBlank
    private String moduleTypeOfTask;
    @NotBlank
    private String moduleType;
    @NotBlank
    private String moduleTaskDescription;

    public UUID getModuleId() {
        return moduleId;
    }

    public void setModuleId(UUID moduleId) {
        this.moduleId = moduleId;
    }

    public String getModuleTaskDescription() {
        return moduleTaskDescription;
    }

    public void setModuleTaskDescription(String moduleTaskDescription) {
        this.moduleTaskDescription = moduleTaskDescription;
    }

    public String getModuleType() {
        return moduleType;
    }

    public void setModuleType(String moduleType) {
        this.moduleType = moduleType;
    }

    public String getModuleTypeOfTask() {
        return moduleTypeOfTask;
    }

    public void setModuleTypeOfTask(String moduleTypeOfTask) {
        this.moduleTypeOfTask = moduleTypeOfTask;
    }
}
