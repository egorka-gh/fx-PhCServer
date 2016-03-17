package com.photodispatcher.model.mysql.services;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.DmlResult;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;
import com.photodispatcher.model.mysql.entities.TechLog;

@RemoteDestination(id="techService", source="techService")
public interface TechService {

	SelectResult<TechLog> loadBooks4Otk(String id, String sub_id);
	DmlResult<TechLog> log(TechLog item);
	SelectResult<TechLog> loadTechPulse(int techPointType);
	boolean testMail();
	SqlResult forwardMeterByTechPoint(int techPoint, String printgroup);
	SqlResult calcByPg(String pgId, int techPointId);
	SqlResult logByPg(TechLog item, int calc);
}
