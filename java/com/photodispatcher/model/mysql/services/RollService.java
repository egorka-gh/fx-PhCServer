package com.photodispatcher.model.mysql.services;

import java.util.List;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.DmlResult;
import com.photodispatcher.model.mysql.entities.LabRoll;
import com.photodispatcher.model.mysql.entities.Roll;
import com.photodispatcher.model.mysql.entities.SelectResult;

@RemoteDestination(id="rollService", source="rollService")
public interface RollService {

	public SelectResult<Roll> loadAll();
	public SelectResult<LabRoll> getByDevice(int device, boolean forEdit);
	public DmlResult fillByChannels(int device);
	public DmlResult persistBatch(List<LabRoll> items);
}
