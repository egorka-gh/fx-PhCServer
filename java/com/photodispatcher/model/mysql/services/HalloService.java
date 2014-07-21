package com.photodispatcher.model.mysql.services;

import org.granite.messaging.service.annotations.RemoteDestination;

@RemoteDestination(id="halloService", source="halloService")
public interface HalloService {
	
	public String ping();
}
