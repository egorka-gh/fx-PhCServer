package com.photodispatcher.model.mysql.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.entities.DmlResult;
import com.photodispatcher.model.mysql.entities.Lab;
import com.photodispatcher.model.mysql.entities.LabDevice;
import com.photodispatcher.model.mysql.entities.LabPrintCode;
import com.photodispatcher.model.mysql.entities.LabRoll;
import com.photodispatcher.model.mysql.entities.LabStopLog;
import com.photodispatcher.model.mysql.entities.LabTimetable;
import com.photodispatcher.model.mysql.entities.PrintGroup;
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
					" FROM lab_print_code l"+
					" INNER JOIN attr_value p ON l.paper = p.id"+
					" INNER JOIN attr_value fr ON l.frame = fr.id"+
					" INNER JOIN attr_value cr ON l.correction = cr.id"+
					" INNER JOIN attr_value cu ON l.cutting = cu.id"+
					" WHERE l.src_type = ?"+
					" ORDER BY l.prt_code";
			result=runSelect(LabPrintCode.class, sql, src_type);
		}else{
			sql="SELECT l.*, p.value paper_name, fr.value frame_name, cr.value correction_name, cu.value cutting_name"+
				" FROM lab_print_code l"+
				" INNER JOIN src_type st ON st.id = l.src_type AND st.loc_type = 2"+
				" INNER JOIN attr_value p ON l.paper = p.id"+
				" INNER JOIN attr_value fr ON l.frame = fr.id"+
				" INNER JOIN attr_value cr ON l.correction = cr.id"+
				" INNER JOIN attr_value cu ON l.cutting = cu.id"+
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
	public SelectResult<Lab> loadList(){
		String sql="SELECT s.*, st.name src_type_name"+
					" FROM lab s" +
					" INNER JOIN src_type st ON st.id = s.src_type"+
					" ORDER BY s.name";
		
		return runSelect(Lab.class, sql);
	}

	@Override
	public SelectResult<Lab> loadAll(boolean forEdit){
		SelectResult<Lab> result;
		String sql="SELECT s.*, st.name src_type_name"+
					" FROM lab s" +
					" INNER JOIN src_type st ON st.id = s.src_type";
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
					" FROM lab_device s" +
					" LEFT OUTER JOIN tech_point tp ON tp.id = s.tech_point"+
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
						" CAST(IFNULL(ltt.time_from,'2000-01-01 08:00:00') AS DATETIME) time_from,"+
						" CAST(IFNULL(ltt.time_to,  '2000-01-01 18:00:00') AS DATETIME) time_to,"+
						" IFNULL(ltt.is_online,0) is_online"+ 
					" FROM week_days wd"+
					" LEFT OUTER JOIN lab_timetable ltt ON  wd.id=ltt.day_id and ltt.lab_device=?"+
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
					" FROM lab s" +
					" INNER JOIN src_type st ON st.id = s.src_type"+
					" WHERE s.id=?";
		sResult=runSelect(Lab.class, sql, id);
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
							dev.setLab(lab.getId());
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
		String sql="DELETE FROM lab_device WHERE id=?";
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

	@Override
	public SelectResult<PrintGroup> getLastPGroupByTPoint(int techPontId){
		String sql="SELECT pg.* FROM tech_log tl INNER JOIN print_group pg ON pg.id = tl.print_group" +
					" WHERE tl.src_id = ? AND tl.log_date ="+
						" (SELECT MAX(tl.log_date) FROM tech_log tl1 WHERE tl1.src_id = ?)";
		return runSelect(PrintGroup.class, sql, techPontId, techPontId);
	}
	
	@Override
	public DmlResult<LabStopLog> logLabStop(LabStopLog log) {
		
		log.setTime_created(new Date());
		DmlResult<LabStopLog> result = runInsert(log);
		return result;
		
	}
	
	@Override
	public DmlResult<LabStopLog> updateLabStop(LabStopLog log) {
		
		return runUpdate(log);
		
	}
	
	@Override
	public SelectResult<LabStopLog> getLabStops(Date timeGapStart, Date timeGapEnd){
		String sql = "SELECT ls.*, st.name lab_stop_type_name FROM lab_stop_log ls " +
						"LEFT OUTER JOIN lab_stop_type st ON st.id = ls.lab_stop_type " +
						"WHERE " +
						"(time_to BETWEEN ? AND ?) OR " +
						"(time_from BETWEEN ? AND ?) OR " +
						"(time_from < ? AND time_to IS NULL) OR " +
						"(time_from < ? AND time_to > ?)";
		return runSelect(LabStopLog.class, sql, timeGapStart, timeGapEnd, timeGapStart, timeGapEnd, timeGapStart, timeGapStart, timeGapEnd);
	}
	
}
