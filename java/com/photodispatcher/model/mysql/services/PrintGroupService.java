package com.photodispatcher.model.mysql.services;

import java.util.Date;
import java.util.List;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.DmlResult;
import com.photodispatcher.model.mysql.entities.PrintGroup;
import com.photodispatcher.model.mysql.entities.SelectResult;

@RemoteDestination(id="printGroupService", source="printGroupService")
public interface PrintGroupService {

	SelectResult<PrintGroup> loadByState(int stateFrom, int stateTo);
	SelectResult<PrintGroup> loadByOrderState(int stateFrom, int stateTo);
	SelectResult<PrintGroup> loadPrinted(Date after);
	SelectResult<PrintGroup> loadPrintPost(List<String> ids);
	SelectResult<PrintGroup> capturePrintState(List<PrintGroup> printGroups, boolean loadFiles);
	SelectResult<PrintGroup> loadPrintPostByDev(List<Integer> devices, int loadPhoto);
	DmlResult<PrintGroup> fillCaptured(String id);
	SelectResult<PrintGroup> loadReady4Print(int limit, boolean onlyBook);
	SelectResult<PrintGroup> loadInPrintPost(int lab);
	
}
