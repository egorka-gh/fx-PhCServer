package com.photodispatcher.model.mysql.services;

import java.util.Date;
import java.util.List;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.PrintGroup;
import com.photodispatcher.model.mysql.entities.PrnQueue;
import com.photodispatcher.model.mysql.entities.PrnQueueTimetable;
import com.photodispatcher.model.mysql.entities.PrnStrategy;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;

@RemoteDestination(id="prnStrategyService", source="prnStrategyService")
public interface PrnStrategyService {

	SelectResult<PrnStrategy> loadStrategies();

	SelectResult<PrnStrategy> persistStrategies(List<PrnStrategy> items);

	SqlResult startStrategy2(int strategy);

	SelectResult<PrnQueue> loadQueues();

	SqlResult checkQueue2();

	SqlResult startQueue(int queue, int subQueue, int lab);

	SelectResult<PrnQueue> loadComplitedQueues(Date date);

	SqlResult checkQueues();

	SqlResult deleteQueue(int queue);

	SqlResult releaseQueue(int queue);

	SelectResult<PrintGroup> getQueueMarkPGs(int queue);

	SqlResult createQueue(int strategy, int lab, PrintGroup params);

	SelectResult<PrnQueue> loadQueue(int queue, int subQueue);

	SelectResult<PrnQueueTimetable> loadStartTimetable();

	SelectResult<PrnQueueTimetable> persistStartTimetable(
			List<PrnQueueTimetable> items);

	SelectResult<PrnQueueTimetable> deleteStartTimetable(int id);

	SqlResult createQueueBatch(List<PrnQueueTimetable> params);

	SelectResult<PrintGroup> loadQueueItemsByPG(String pgId);

}
