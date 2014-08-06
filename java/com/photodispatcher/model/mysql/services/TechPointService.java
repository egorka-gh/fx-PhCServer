package com.photodispatcher.model.mysql.services;

import java.util.List;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;
import com.photodispatcher.model.mysql.entities.TechPoint;

@RemoteDestination(id="techPointService", source="techPointService")
public interface TechPointService {

	public SelectResult<TechPoint> loadAll(int type);
	public SqlResult persistBatch(List<TechPoint> items);

}
