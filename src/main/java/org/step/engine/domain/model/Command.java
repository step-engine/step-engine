package org.step.engine.domain.model;

import org.step.engine.domain.command.CommandStatus;

public class Command<T> {
	private final T workflowId;
	private final String applicationId;
	private final int commandType;
	private CommandStatus commandStatus;
	private final String commandName;
	private final String command;
	private String note;
	private final long occurredOn;

	public Command(
			T workflowId,
			String applicationId,
			int commandType,
			CommandStatus commandStatus,
			String commandName,
			String command,
			String note,
			long occurredOn) {
		this.workflowId = workflowId;
		this.applicationId = applicationId;
		this.commandType = commandType;
		this.commandStatus = commandStatus;
		this.commandName = commandName;
		this.command = command;
		this.note = note;
		this.occurredOn = occurredOn;
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
	public long getOccurredOn() { return occurredOn; }
}