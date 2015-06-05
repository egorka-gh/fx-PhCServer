package com.photodispatcher.model.mysql.entities;

import javax.persistence.Transient;

public class HelloResponce extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
    @Transient
    private String responce;
    @Transient
    private String hostIP;
    @Transient
    private String hostName;
    
	public String getResponce() {
		return responce;
	}
	public void setResponce(String responce) {
		this.responce = responce;
	}
	public String getHostIP() {
		return hostIP;
	}
	public void setHostIP(String hostIP) {
		this.hostIP = hostIP;
	}
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
}
