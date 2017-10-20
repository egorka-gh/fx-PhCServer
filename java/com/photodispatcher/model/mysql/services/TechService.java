package com.photodispatcher.model.mysql.services;

import java.util.List;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.DmlResult;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;
import com.photodispatcher.model.mysql.entities.TechLog;
import com.photodispatcher.model.mysql.entities.TechTimeline;

@RemoteDestination(id="techService", source="techService")
public interface TechService {

	SelectResult<TechLog> loadBooks4Otk(String id, String sub_id);
	DmlResult<TechLog> log(TechLog item);
	SelectResult<TechLog> loadTechPulse(int techPointType);
	boolean testMail();
	SqlResult forwardMeterByTechPoint(int techPoint, String printgroup);
	SqlResult calcByPg(String pgId, int techPointId);
	SqlResult logByPg(TechLog item, int calc);
	SelectResult<TechTimeline> loadTimeLine(int process);
	SqlResult persistTimeLineBatch(List<TechTimeline> items);
}
