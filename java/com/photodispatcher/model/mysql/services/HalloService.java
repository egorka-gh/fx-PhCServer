package com.photodispatcher.model.mysql.services;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.HelloResponce;

@RemoteDestination(id="halloService", source="halloService")
public interface HalloService {
	
	public HelloResponce ping();
}
