package com.photodispatcher.model.mysql.entities.messenger;

import com.photodispatcher.model.mysql.entities.AbstractEntity;
import com.photodispatcher.model.mysql.entities.PrintGroup;

public class CycleMessage extends AbstractEntity{
    private static final long serialVersionUID = 1L;

    private String topic;
    private CycleStation sender;
    private String recipient;
    private int command;
    private String message;
    private PrintGroup pgroup_info;

    public CycleStation getSender() {
		return sender;
	}
	public void setSender(CycleStation sender) {
		this.sender = sender;
	}
	public String getRecipient() {
		return recipient;
	}
	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}
	public int getCommand() {
		return command;
	}
	public void setCommand(int command) {
		this.command = command;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public PrintGroup getPgroup_info() {
		return pgroup_info;
	}
	public void setPgroup_info(PrintGroup pgroup_info) {
		this.pgroup_info = pgroup_info;
	}
	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
}
