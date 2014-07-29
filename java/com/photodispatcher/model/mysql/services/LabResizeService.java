package com.photodispatcher.model.mysql.services;

import java.util.List;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.DmlResult;
import com.photodispatcher.model.mysql.entities.LabResize;
import com.photodispatcher.model.mysql.entities.SelectResult;

@RemoteDestination(id="labResizeService", source="labResizeService")
public interface LabResizeService{
	public SelectResult<LabResize> loadAll();
	public DmlResult persist(LabResize item);
	public DmlResult persistBatch(List<LabResize> items);
}
