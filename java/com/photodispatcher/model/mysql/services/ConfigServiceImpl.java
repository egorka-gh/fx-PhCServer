package com.photodispatcher.model.mysql.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.entities.AliasForward;
import com.photodispatcher.model.mysql.entities.AppConfig;
import com.photodispatcher.model.mysql.entities.Rack;
import com.photodispatcher.model.mysql.entities.RackSpace;
import com.photodispatcher.model.mysql.entities.RackTechPoint;
import com.photodispatcher.model.mysql.entities.RackType;
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

	@Override
	public SelectResult<RackType> loadRackTypes(){
		String sql="SELECT * FROM rack_type rt WHERE rt.id != 0 ORDER BY rt.name";
		return runSelect(RackType.class, sql);
	}
	@Override
	public SqlResult persistRackTypes(List<RackType> items){
		SqlResult result=new SqlResult();
		List<RackType> insertList=new ArrayList<RackType>();
		List<RackType> updateList=new ArrayList<RackType>();
		List<RackType> delList=new ArrayList<RackType>();

		for(RackType item : items){
			if(item.getName()==null || item.getName().isEmpty()){
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

	@Override
	public SelectResult<Rack> loadRacks(){
		String sql="SELECT *, rt.name rack_type_name" +
					 " FROM rack r" +
					   " INNER JOIN rack_type rt ON r.rack_type = rt.id" +
					 " WHERE r.id != 0" +
					 " ORDER BY r.name";
		return runSelect(Rack.class, sql);
	}

	@Override
	public SqlResult persistRacks(List<Rack> items){
		SqlResult result=new SqlResult();
		List<Rack> insertList=new ArrayList<Rack>();
		List<Rack> updateList=new ArrayList<Rack>();
		List<Rack> delList=new ArrayList<Rack>();

		for(Rack item : items){
			if(item.getName()==null || item.getName().isEmpty()){
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

	@Override
	public SelectResult<RackSpace> loadRackSpace(){
		String sql="SELECT rs.*, r.name rack_name" +
				  " FROM rack_space rs"+
				   " INNER JOIN rack r ON rs.rack = r.id"+
				 " WHERE rs.rack != 0"+
				 " ORDER BY rs.name";
		return runSelect(RackSpace.class, sql);
	}

	@Override
	public SqlResult persistRackSpace(List<RackSpace> items){
		SqlResult result=new SqlResult();
		List<RackSpace> insertList=new ArrayList<RackSpace>();
		List<RackSpace> updateList=new ArrayList<RackSpace>();
		List<RackSpace> delList=new ArrayList<RackSpace>();

		for(RackSpace item : items){
			if(item.getName()==null || item.getName().isEmpty()){
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
	
	@Override
	public SelectResult<RackTechPoint> loadRackTPoint(List<Integer> tpTypes){
		//build sql
		StringBuilder sb= new StringBuilder("");
		sb.append("SELECT r.id rack, r.name rack_name, tp.id tech_point, tp.name tech_point_name, IF(rtp.rack IS NULL, 0, 1) inuse");
		sb.append(" FROM rack r");
		sb.append(" INNER JOIN tech_point tp ON tp.tech_type IN (");
			for(Integer id : tpTypes){
				sb.append(id).append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append(")");
		sb.append(" LEFT OUTER JOIN rack_tech_point rtp ON rtp.rack = r.id AND rtp.tech_point = tp.id");
		sb.append(" WHERE r.id != 0");
		sb.append(" ORDER BY r.id, r.name");

		String sql=sb.toString();
		return runSelect(RackTechPoint.class, sql);
	}

	@Override
	public SqlResult persistRackTPoint(List<RackTechPoint> items){
		SqlResult result=new SqlResult();
		List<RackTechPoint> insertList=new ArrayList<RackTechPoint>();
		List<RackTechPoint> delList=new ArrayList<RackTechPoint>();

		for(RackTechPoint item : items){
			if(!item.isInuse()){
				delList.add(item);
			}else{
				insertList.add(item);
			}
		}
		if(!insertList.isEmpty()){
			result=runInsertBatch(insertList);
		}
		if(result.isComplete() && !delList.isEmpty()){
			result=runDeleteBatch(delList);
		}
		return result;
	}

}
