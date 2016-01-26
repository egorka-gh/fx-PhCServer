package com.photodispatcher.model.mysql.entities.report;

import com.photodispatcher.model.mysql.entities.AbstractEntity;

public class ReportResult extends AbstractEntity {
    private static final long serialVersionUID = 1L;

    private String id;
    private boolean hasError;
    private String url;
    private String error;
    private String messageId;
    
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public boolean isHasError() {
		return hasError;
	}
	public void setHasError(boolean hasError) {
		this.hasError = hasError;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}

	public void assignError(String error) {
		hasError=true;
		this.error = error;
	}
	public String getMessageId() {
		return messageId;
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

}
