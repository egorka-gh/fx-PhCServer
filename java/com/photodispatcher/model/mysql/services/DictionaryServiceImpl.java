package com.photodispatcher.model.mysql.services;

import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.entities.AttrJsonMap;
import com.photodispatcher.model.mysql.entities.AttrType;
import com.photodispatcher.model.mysql.entities.FieldValue;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SourceProperty;
import com.photodispatcher.model.mysql.entities.SubordersTemplate;

@Service("dictionaryService")
public class DictionaryServiceImpl extends AbstractDAO implements DictionaryService {

	@Override
	public SelectResult<AttrType> getPrintAttrs(){
		SelectResult<AttrType> result;
		String sql="SELECT at.* FROM attr_type at WHERE at.persist=1 AND at.attr_fml=1 AND at.list=1";
		result=runSelect(AttrType.class, sql);
		return result;
	}

	@Override
	public SelectResult<FieldValue> getFieldValueList(int fieldId, boolean includeNone){
		SelectResult<FieldValue> result;
		String sql="SELECT id value, value label FROM attr_value av WHERE av.attr_tp IN (0,?) AND id>=0";
		if(includeNone) sql="SELECT 0 value, ' ' label UNION "+sql;
		result=runSelect(FieldValue.class, sql, fieldId);
		return result;
	}

	@Override
	public SelectResult<FieldValue> getBookTypeValueList(boolean includeDefault){
		SelectResult<FieldValue> result;
		String sql="SELECT id value, name label FROM book_type";
		if(!includeDefault) sql+=" WHERE id!=0";
		result=runSelect(FieldValue.class, sql);
		return result;
	}
	
	@Override
	public SelectResult<FieldValue> getBookSynonimTypeValueList(){
		SelectResult<FieldValue> result;
		String sql="SELECT id value, name label FROM book_synonym_type";
		result=runSelect(FieldValue.class, sql);
		return result;
	}

	@Override
	public SelectResult<FieldValue> getStaffValueList(){
		SelectResult<FieldValue> result;
		String sql="SELECT id value, name label FROM staff s"+
					" WHERE s.id!=0"+
					" ORDER BY s.name";
		result=runSelect(FieldValue.class, sql);
		return result;
	}

	@Override
	public SelectResult<FieldValue> getStaffActivityGroupValueList(){
		SelectResult<FieldValue> result;
		String sql="SELECT id value, name label FROM staff_activity_group WHERE id!=0 ORDER BY name";
		result=runSelect(FieldValue.class, sql);
		return result;
	}


	@Override
	public SelectResult<FieldValue> getBookPartValueList(boolean includeDefault){
		SelectResult<FieldValue> result;
		String sql="SELECT id value, name label FROM book_part";
		if(!includeDefault) sql+=" WHERE id!=0";
		result=runSelect(FieldValue.class, sql);
		return result;
	}

	@Override
	public SelectResult<FieldValue> getSrcTypeValueList(int locType, boolean includeDefault){
		SelectResult<FieldValue> result;
		String sql="SELECT id value, name label FROM src_type WHERE loc_type = ?";
		if(includeDefault) sql+=" OR id = 0";
		result=runSelect(FieldValue.class, sql, locType);
		return result;
	}

	@Override
	public SelectResult<FieldValue> getWeekDaysValueList(boolean includeDefault){
		SelectResult<FieldValue> result;
		String sql="SELECT id value, name label FROM week_days ORDER BY 1";
		if(includeDefault) sql="SELECT 0 value, ' ' label UNION "+sql;
		result=runSelect(FieldValue.class, sql);
		return result;
	}

	@Override
	public SelectResult<FieldValue> getTechPointValueList(boolean includeDefault){
		SelectResult<FieldValue> result;
		String sql="SELECT id value, name label FROM tech_point ORDER BY 1";
		if(includeDefault) sql="SELECT null value, ' ' label UNION "+sql;
		result=runSelect(FieldValue.class, sql);
		return result;
	}

	@Override
	public SelectResult<FieldValue> getTechTypeValueList(){
		SelectResult<FieldValue> result;
		String sql="SELECT id value, name label FROM order_state WHERE tech=1 ORDER BY value";
		result=runSelect(FieldValue.class, sql);
		return result;
	}

	@Override
	public SelectResult<FieldValue> getTechLayerValueList(boolean includeDefault){
		SelectResult<FieldValue> result;
		String sql="SELECT id value, name label FROM layer ORDER BY 1";
		//if(includeDefault) sql="SELECT null value, " " label UNION "+sql;
		result=runSelect(FieldValue.class, sql);
		return result;
	}

	@Override
	public SelectResult<FieldValue> getLayerGroupValueList(boolean includeDefault){
		SelectResult<FieldValue> result;
		String sql="SELECT id value, name label FROM layer_group WHERE id!=2 ORDER BY 1";
		if(includeDefault) sql="SELECT null value, ' ' label UNION "+sql;
		result=runSelect(FieldValue.class, sql);
		return result;
	}

	@Override
	public SelectResult<FieldValue> getRollValueList(boolean includeDefault){
		SelectResult<FieldValue> result;
		String sql="SELECT width value, CAST(width AS CHAR) label FROM roll ORDER BY 1";
		if(includeDefault) sql="SELECT null value, ' ' label UNION "+sql;
		result=runSelect(FieldValue.class, sql);
		return result;
	}

	@Override
	public SelectResult<FieldValue> getFieldValueSynonims(){
		SelectResult<FieldValue> result;
		String sql="SELECT a.src_type, a.synonym, at.field, CAST((CASE at.list WHEN 1 THEN a.attr_val ELSE av.value END) AS SIGNED) value"+
					 " FROM attr_synonym a, attr_value av, attr_type at"+
					 " WHERE  a.attr_val = av.id AND av.attr_tp = at.id AND at.attr_fml = 1";
		result=runSelect(FieldValue.class, sql);
		return result;
	}

	@Override
	public SelectResult<AttrJsonMap> getOrderJsonAttr(int family){
		SelectResult<AttrJsonMap> result;
		String sql="SELECT jm.*, at.field, at.list, at.persist, at.name field_name"+
				" FROM attr_json_map jm"+
				" INNER JOIN attr_type at ON jm.attr_type=at.id"+
				" WHERE at.attr_fml=?"+
				" ORDER BY jm.src_type, at.field";
		result=runSelect(AttrJsonMap.class, sql, family);
		return result;
	}

	@Override
	public SelectResult<SourceProperty> loadSourceProperties(){
		String sql="SELECT stpv.*, stp.name"+
					" FROM src_type_prop_val stpv"+
					" INNER JOIN src_type_prop stp ON stpv.src_type_prop = stp.id"+
					" ORDER BY stpv.src_type, stp.name";
		return runSelect(SourceProperty.class, sql);
	}

	@Override
	public SelectResult<SubordersTemplate> loadSubordersTemplate(){
		String sql="SELECT * FROM suborders_template";
		return runSelect(SubordersTemplate.class, sql);
	}

}
