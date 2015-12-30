package com.photodispatcher.model.mysql.services;

import java.util.List;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.PrnStrategy;
import com.photodispatcher.model.mysql.entities.SelectResult;

@RemoteDestination(id="prnStrategyService", source="prnStrategyService")
public interface PrnStrategyService {

	SelectResult<PrnStrategy> loadStrategies();

	SelectResult<PrnStrategy> persistStrategies(List<PrnStrategy> items);

}
