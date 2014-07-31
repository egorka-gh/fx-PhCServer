package com.photodispatcher.model.mysql.services;

import java.util.List;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.BookPgTemplate;
import com.photodispatcher.model.mysql.entities.BookSynonym;
import com.photodispatcher.model.mysql.entities.DmlResult;
import com.photodispatcher.model.mysql.entities.SelectResult;

@RemoteDestination(id="bookSynonymService", source="bookSynonymService")
public interface BookSynonymService {

	public SelectResult<BookSynonym> loadFull();
	public DmlResult persistBatch(List<BookSynonym> items);
	public SelectResult<BookSynonym> loadAll(int src_type, int contentFilter);
	public SelectResult<BookPgTemplate> loadTemplates(int book);
}
