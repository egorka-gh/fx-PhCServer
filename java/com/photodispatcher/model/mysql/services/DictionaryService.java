package com.photodispatcher.model.mysql.services;

import java.util.List;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.AttrType;
import com.photodispatcher.model.mysql.entities.FieldValue;
import com.photodispatcher.model.mysql.entities.LayersetSynonym;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;

@RemoteDestination(id="dictionaryService", source="dictionaryService")
public interface DictionaryService {

	public SelectResult<AttrType> getPrintAttrs();
	public SelectResult<FieldValue> getFieldValueList(int fieldId, boolean includeNone);
	public SelectResult<FieldValue> getBookTypeValueList(boolean includeDefault);
	public SelectResult<FieldValue> getSrcTypeValueList(int locType, boolean includeDefault);
	public SelectResult<FieldValue> getWeekDaysValueList(boolean includeDefault);
	public SelectResult<FieldValue> getTechPointValueList(boolean includeDefault);
	public SelectResult<FieldValue> getTechLayerValueList(boolean includeDefault);
	public SelectResult<FieldValue> getLayerGroupValueList(boolean includeDefault);
	public SelectResult<FieldValue> getRollValueList(boolean includeDefault);
	public SelectResult<FieldValue> getBookPartValueList(boolean includeDefault);
	public SelectResult<FieldValue> getBookSynonimTypeValueList();
	public SelectResult<FieldValue> getFieldValueSynonims();
	public SelectResult<FieldValue> getTechTypeValueList();
	public SelectResult<LayersetSynonym> loadLayersetSynonyms(int itemId);
	public SqlResult persistsLayersetSynonyms(List<LayersetSynonym> targetList);

}
