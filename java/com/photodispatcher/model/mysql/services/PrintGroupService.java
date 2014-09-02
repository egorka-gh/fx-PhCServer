package com.photodispatcher.model.mysql.services;

import java.util.Date;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.PrintGroup;
import com.photodispatcher.model.mysql.entities.SelectResult;

@RemoteDestination(id="printGroupService", source="printGroupService")
public interface PrintGroupService {

	SelectResult<PrintGroup> loadByState(int stateFrom, int stateTo);
	SelectResult<PrintGroup> loadByOrderState(int stateFrom, int stateTo);
	SelectResult<PrintGroup> loadInPrint(int lab);
	SelectResult<PrintGroup> loadPrinted(Date after);

}