package com.photodispatcher.model.mysql.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.entities.DmlResult;
import com.photodispatcher.model.mysql.entities.Lab;
import com.photodispatcher.model.mysql.entities.LabDevice;
import com.photodispatcher.model.mysql.entities.LabPrintCode;
import com.photodispatcher.model.mysql.entities.LabRoll;
import com.photodispatcher.model.mysql.entities.LabTimetable;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;

@Service("labService")
public class LabServiceImpl extends AbstractDAO implements LabService {
	
	@Override
	public SelectResult<LabPrintCode> loadPrintCode(int src_type){
		SelectResult<LabPrintCode> result=null;
		String sql;
		if(src_type>0){
			sql="SELECT l.*, p.value paper_name, fr.value frame_name, cr.value correction_name, cu.value cutting_name"+
					" FROM phcconfig.lab_print_code l"+
					" INNER JOIN phcconfig.attr_value p ON l.paper = p.id"+
					" INNER JOIN phcconfig.attr_value fr ON l.frame = fr.id"+
					" INNER JOIN phcconfig.attr_value cr ON l.correction = cr.id"+
					" INNER JOIN phcconfig.attr_value cu ON l.cutting = cu.id"+
					" WHERE l.src_type = ?"+
					" ORDER BY l.prt_code";
			result=runSelect(LabPrintCode.class, sql, src_type);
		}else{
			sql="SELECT l.*, p.value paper_name, fr.value frame_name, cr.value correction_name, cu.value cutting_name"+
				" FROM phcconfig.lab_print_code l"+
				" INNER JOIN phcconfig.src_type st ON st.id = l.src_type AND st.loc_type = 2"+
				" INNER JOIN phcconfig.attr_value p ON l.paper = p.id"+
				" INNER JOIN phcconfig.attr_value fr ON l.frame = fr.id"+
				" INNER JOIN phcconfig.attr_value cr ON l.correction = cr.id"+
				" INNER JOIN phcconfig.attr_value cu ON l.cutting = cu.id"+
				" ORDER BY l.src_type";
			result=runSelect(LabPrintCode.class, sql);
		}
		return result;
	}
	
	@Override
	public SqlResult persistPrintCodes(List<LabPrintCode> items){
		SqlResult result=new SqlResult();
		List<LabPrintCode> insertList=new ArrayList<LabPrintCode>();
		List<LabPrintCode> updateList=new ArrayList<LabPrintCode>();

		for(LabPrintCode item : items){
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
	public SelectResult<Lab> loadAll(boolean forEdit){
		SelectResult<Lab> result;
		String sql="SELECT s.*, st.name src_type_name"+
					" FROM phcconfig.lab s" +
					" INNER JOIN phcconfig.src_type st ON st.id = s.src_type";
		if(!forEdit) sql+=" WHERE s.is_active=1";
		sql+=" ORDER BY s.name";
		
		result=runSelect(Lab.class, sql);
		if (result.isComplete()){
			for (Lab item : result.getData()){
				//load childs
				SqlResult subResult=loadDevices(item, forEdit);
				if(!subResult.isComplete()){
					result.cloneError(subResult);
					break;
				}
			}
		}
		return result;
	}
	
	private SqlResult loadDevices(Lab lab, boolean forEdit){
		String sql="SELECT s.*, tp.name tech_point_name"+
					" FROM phcconfig.lab_device s" +
					" LEFT OUTER JOIN phcconfig.tech_point tp ON tp.id = s.tech_point"+
					" WHERE s.lab=?";
		SelectResult<LabDevice> childs=runSelect(LabDevice.class, sql, lab.getId());
		if(childs.isComplete()){
			lab.setDevices(childs.getData());
			if(childs.getData()!=null){
				for (LabDevice dev : childs.getData()){
					SqlResult res=fillDevice(dev, forEdit);
					if(!res.isComplete()){
						return res;
					}
				}
			}
		}
		return childs;
	}
	
	private SqlResult fillDevice(LabDevice device, boolean forEdit){
		//rolls
		RollServiceImpl rollsvc= new RollServiceImpl();
		SelectResult<LabRoll> rollres= rollsvc.getByDevice(device.getId(), forEdit);
		if(!rollres.isComplete()){
			return rollres;
		}
		device.setRolls(rollres.getData());
		//timetable
		String sql="SELECT wd.id day_id, wd.name day_id_name, ? lab_device,"+
						" IFNULL(ltt.time_from,'2000-01-01 08:00:00') time_from,"+
						" IFNULL(ltt.time_to,  '2000-01-01 18:00:00') time_to,"+
						" IFNULL(ltt.is_online,0) is_online"+ 
					" FROM phcconfig.week_days wd"+
					" LEFT OUTER JOIN phcconfig.lab_timetable ltt ON  wd.id=ltt.day_id and ltt.lab_device=?"+
					" ORDER BY wd.id";
		SelectResult<LabTimetable> ttres= runSelect(LabTimetable.class, sql, device.getId(), device.getId());
		device.setTimetable(ttres.getData());
		return ttres;
	}

	@Override
	public DmlResult<Lab> loadLab(int id, boolean forEdit){
		SelectResult<Lab> sResult;
		DmlResult<Lab> result=new DmlResult<Lab>();
		String sql="SELECT s.*, st.name src_type_name"+
					" FROM phcconfig.lab s" +
					" INNER JOIN phcconfig.src_type st ON st.id = s.src_type"+
					" WHERE s.id=?";
		sResult=runSelect(Lab.class, sql);
		if (sResult.isComplete()){
			if(sResult.getData()!=null && !sResult.getData().isEmpty()){
				result.setItem(sResult.getData().get(0));
				SqlResult subResult=loadDevices(result.getItem(), forEdit);
				if(!subResult.isComplete()){
					result.cloneError(subResult);
				}
			}
		}else{
			result.cloneError(sResult);
		}
		return result;
	}

	@Override
	public DmlResult<Lab> persistLab(Lab lab){
		DmlResult<Lab> result=new DmlResult<Lab>();
		SqlResult subResult= new SqlResult();
		if(lab.getPersistState()==0){
			//insert 
			result=runInsert(lab);
		}else{
			if(lab.getPersistState()==-1){
				result=runUpdate(lab);
			}
			if(result.isComplete()){
				List<LabDevice> insertDev=new ArrayList<LabDevice>();
				List<LabDevice> updateDev=new ArrayList<LabDevice>();
				List<LabRoll>   rollList=new ArrayList<LabRoll>();
				List<LabTimetable>   ttList=new ArrayList<LabTimetable>();
				
				if(lab.getDevices()!=null){
					for(LabDevice dev : lab.getDevices()){
						if(dev.getPersistState()==0){
							insertDev.add(dev);
						}else if(dev.getPersistState()==-1){
							updateDev.add(dev);
						}
						//rolls
						if(dev.getRolls()!=null) rollList.addAll(dev.getRolls());
						//timetable
						if(dev.getTimetable()!=null) ttList.addAll(dev.getTimetable());
					}
				}
				
				if(!insertDev.isEmpty()){
					subResult=runInsertBatch(insertDev);
				}
				if(subResult.isComplete() && !updateDev.isEmpty()){
					subResult=runUpdateBatch(updateDev);
				}
				if(subResult.isComplete() && !rollList.isEmpty()){
					RollServiceImpl rsvc= new RollServiceImpl();
					subResult=rsvc.persistBatch(rollList);
				}
				if(subResult.isComplete() && !ttList.isEmpty()){
					subResult=runUpdateBatch(ttList);
					if(subResult.isComplete()) subResult=runInsertBatch(ttList);
				}
			}
		}
		if(!subResult.isComplete()) result.cloneError(subResult);
		if(result.isComplete()) result=loadLab(lab.getId(), true);
		return result;
	}

	@Override
	public DmlResult<LabDevice> addDevice(LabDevice device){
		DmlResult<LabDevice> result= runInsert(device);
		if(result.isComplete()){
			SqlResult subResult= fillDevice(device, true);
			if(!subResult.isComplete()) result.cloneError(subResult);
		}
		return result;
	}

	@Override
	public SelectResult<LabDevice> delDevice(int deviceId, int labId){
		SelectResult<LabDevice> result= new SelectResult<LabDevice>();
		String sql="DELETE FROM phcconfig.lab_device s WHERE s.id=?";
		SqlResult delRes= runDML(sql, deviceId);
		if(!delRes.isComplete()){
			result.cloneError(delRes);
			return result;
		}
		DmlResult<Lab> labRes=loadLab(labId, true);
		if(!labRes.isComplete()){
			result.cloneError(labRes);
			return result;
		}
		if(labRes.getItem()!=null) result.setData(labRes.getItem().getDevices());
		return result;
	}
}
