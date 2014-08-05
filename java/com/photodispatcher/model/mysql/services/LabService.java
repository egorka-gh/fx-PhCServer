package com.photodispatcher.model.mysql.services;

import java.util.List;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.DmlResult;
import com.photodispatcher.model.mysql.entities.Lab;
import com.photodispatcher.model.mysql.entities.LabDevice;
import com.photodispatcher.model.mysql.entities.LabPrintCode;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;

@RemoteDestination(id="labService", source="labService")
public interface LabService {

	public SelectResult<LabPrintCode> loadPrintCode(int src_type);
	public SqlResult persistPrintCodes(List<LabPrintCode> items);
	public SelectResult<Lab> loadAll(boolean forEdit);
	public DmlResult<Lab> loadLab(int id, boolean forEdit);
	public DmlResult<Lab> persistLab(Lab lab);
	public DmlResult<LabDevice> addDevice(LabDevice device);
	public SelectResult<LabDevice> delDevice(int deviceId, int sourceId);
}
