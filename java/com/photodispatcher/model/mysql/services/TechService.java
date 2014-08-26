package com.photodispatcher.model.mysql.services;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.SqlResult;
import com.photodispatcher.model.mysql.entities.TechLog;

@RemoteDestination(id="techService", source="techService")
public interface TechService {

	SqlResult logByPg(TechLog item);

}
