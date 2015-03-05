package com.photodispatcher.model.mysql.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.entities.AliasForward;
import com.photodispatcher.model.mysql.entities.AppConfig;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;
import com.photodispatcher.model.mysql.entities.Staff;

@Service("configService")
public class ConfigServiceImpl extends AbstractDAO implements ConfigService {

	@Override
	public SelectResult<AppConfig> loadConfig(){
		String sql="SELECT c.*, p.name production_name"+
					 " FROM app_config c"+
					   " LEFT OUTER JOIN production p ON p.id = production"+
					" LIMIT 1";
		return runSelect(AppConfig.class, sql);
	}

	@Override
	public SelectResult<Staff> loadStaff(){
		String sql="SELECT s.*, g.name staff_group_name"+
					" FROM staff s"+
					" INNER JOIN staff_group g ON s.staff_group = g.id"+
					" WHERE s.id!=0"+
					" ORDER BY s.name";
		return runSelect(Staff.class, sql);
	}
	
	@Override
	public SqlResult persistStaff(List<Staff> items){
		SqlResult result=new SqlResult();
		List<Staff> insertList=new ArrayList<Staff>();
		List<Staff> updateList=new ArrayList<Staff>();

		for(Staff item : items){
			if(item.getPersistState()==0){
				insertList.add(item);
			}else if(item.getPersistState()==-1){
				updateList.add(item);
			}
		}
		if(!insertList.isEmpty()){
			result=runInsertBatch(insertList);
		}
		if(result.isComplete() && !updateList.isEmpty()){
			result=runUpdateBatch(updateList);
		}
		return result;
	}

	@Override
	public SelectResult<AliasForward> loadAliasForward(){
		String sql="SELECT af.*, os.name state_name"+
					 " FROM alias_forward af"+
					 " LEFT OUTER JOIN order_state os ON os.id=af.state"+
					 " ORDER BY af.alias";
		return runSelect(AliasForward.class, sql);
	}

	@Override
	public SqlResult persistAliasForward(List<AliasForward> items){
		SqlResult result=new SqlResult();
		List<AliasForward> insertList=new ArrayList<AliasForward>();
		List<AliasForward> updateList=new ArrayList<AliasForward>();
		List<AliasForward> delList=new ArrayList<AliasForward>();

		for(AliasForward item : items){
			if(item.getAlias()==null || item.getAlias().isEmpty()){
				if(item.getPersistState()!=0) delList.add(item);
			}else if(item.getPersistState()==0){
				insertList.add(item);
			}else if(item.getPersistState()==-1){
				updateList.add(item);
			}
		}
		if(!insertList.isEmpty()){
			result=runInsertBatch(insertList);
		}
		if(result.isComplete() && !updateList.isEmpty()){
			result=runUpdateBatch(updateList);
		}
		if(result.isComplete() && !delList.isEmpty()){
			result=runDeleteBatch(delList);
		}
		return result;
	}

}
