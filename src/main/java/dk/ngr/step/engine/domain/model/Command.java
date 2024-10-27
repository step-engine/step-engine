package dk.ngr.step.engine.domain.model;

import dk.ngr.step.engine.domain.command.CommandStatus;

public class Command<T> {
	private T workflowId;
	private String applicationId;
	private int commandType;
	private CommandStatus commandStatus;
	private String commandName;
	private String command;
	private String note;
	private long occuredOn;

	public Command(
			T workflowId,
			String applicationId,
			int commandType,
			CommandStatus commandStatus,
			String commandName,
			String command,
			String note,
			long occuredOn) {
		this.workflowId = workflowId;
		this.applicationId = applicationId;
		this.commandType = commandType;
		this.commandStatus = commandStatus;
		this.commandName = commandName;
		this.command = command;
		this.note = note;
		this.occuredOn = occuredOn;
	}

	public T getWorkflowId() { return workflowId; }
	public String getApplicationId() { return applicationId; }
	public int getCommandType() { return commandType; }
	public CommandStatus getCommandStatus() { return commandStatus; }
	public void setCommandStatus(CommandStatus commandStatus) { this.commandStatus = commandStatus; }
	public String getCommandName() { return commandName; }
	public String getCommand() { return command; }
	public String getComment() { return note; }
	public void setComment(String comment) { this.note = comment; }
	public long getOccuredOn() { return occuredOn; }
}