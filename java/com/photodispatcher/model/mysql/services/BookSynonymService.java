package com.photodispatcher.model.mysql.services;

import java.util.List;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.BookPgTemplate;
import com.photodispatcher.model.mysql.entities.BookSynonym;
import com.photodispatcher.model.mysql.entities.BookSynonymGlue;
import com.photodispatcher.model.mysql.entities.GlueCommand;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;

@RemoteDestination(id="bookSynonymService", source="bookSynonymService")
public interface BookSynonymService {

	public SelectResult<BookSynonym> loadFull();
	public SqlResult persistBatch(List<BookSynonym> items);
	public SelectResult<BookSynonym> loadAll(int src_type, int contentFilter);
	public SelectResult<BookPgTemplate> loadTemplates(int book);
	public SqlResult clone(int pId);
	SelectResult<GlueCommand> loadGlueCommandAll();
	SelectResult<GlueCommand> persistGlueCommandBatch(List<GlueCommand> items);
	SelectResult<BookSynonymGlue> loadBookGlueEdit(int book);
	SqlResult persistBookGlue(List<BookSynonymGlue> items);
}
