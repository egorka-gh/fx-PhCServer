package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "package_message")
public class MailPackageMessage extends AbstractEntity {
	private static final long serialVersionUID = 1L;

    @Id
    @Column(name="source")
    private int source;
    @Id
    @Column(name="id")
    private int id;
    @Id
    @Column(name="log_key")
    private String log_key;
    @Column(name="log_user")
    private String log_user;
    @Column(name="message")
    private String message;
	public int getSource() {
		return source;
	}
	public void setSource(int source) {
		this.source = source;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getLog_key() {
		return log_key;
	}
	public void setLog_key(String log_key) {
		this.log_key = log_key;
	}
	public String getLog_user() {
		return log_user;
	}
	public void setLog_user(String log_user) {
		this.log_user = log_user;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

    
}
