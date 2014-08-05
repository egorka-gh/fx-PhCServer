package com.photodispatcher.model.mysql.services;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.DmlResult;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.Source;

@RemoteDestination(id="sourceService", source="sourceService")
public interface SourceService{

	SelectResult<Source> loadAll(int locationType);
	public DmlResult<Source> persist(Source source);
}
