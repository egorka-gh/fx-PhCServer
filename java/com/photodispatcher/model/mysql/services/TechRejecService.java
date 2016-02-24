package com.photodispatcher.model.mysql.services;

import java.util.List;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;
import com.photodispatcher.model.mysql.entities.TechReject;

@RemoteDestination(id="techRejecService", source="techRejecService")
public interface TechRejecService {

	SqlResult create(TechReject reject);
	SelectResult<TechReject> loadReprintWaite();
	SelectResult<TechReject> captureState(List<TechReject> rejects);
}
