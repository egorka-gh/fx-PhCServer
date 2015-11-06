package com.photodispatcher.model.mysql.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.photodispatcher.mail.SendMailByGoogle;
import com.photodispatcher.model.mysql.entities.DmlResult;
import com.photodispatcher.model.mysql.entities.Lab;
import com.photodispatcher.model.mysql.entities.LabDevice;
import com.photodispatcher.model.mysql.entities.LabMeter;
import com.photodispatcher.model.mysql.entities.LabPrintCode;
import com.photodispatcher.model.mysql.entities.LabProfile;
import com.photodispatcher.model.mysql.entities.LabRoll;
import com.photodispatcher.model.mysql.entities.LabStopLog;
import com.photodispatcher.model.mysql.entities.LabStopType;
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
				//load profiles
				SelectResult<LabProfile> pres=loadLabProfiles(item.getId(), forEdit);
				if(!pres.isComplete()){
					result.cloneError(pres);
					break;
				}
				item.setProfiles(pres.getData());
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
		if(!rollres.isComplete()) return rollres;
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
		if(!ttres.isComplete()) return ttres;
		device.setTimetable(ttres.getData());
		
		return ttres;
	}

	private SelectResult<LabProfile> loadLabProfiles(int lab, boolean forEdit){
		String sql;
		if(forEdit){
			sql="SELECT ? lab, av.id paper, av.value paper_name, lp.profile_file"+
				  " FROM attr_value av"+
				  " LEFT OUTER JOIN lab_profile lp ON lp.lab=? AND lp.paper=av.id"+ 
				" WHERE av.attr_tp=2";
		}else{
			sql="SELECT ? lab, av.id paper, av.value paper_name, lp.profile_file"+
					  " FROM attr_value av"+
					  " INNER JOIN lab_profile lp ON lp.lab=? AND lp.paper=av.id"+ 
					" WHERE av.attr_tp=2";
		}
		
		return runSelect(LabProfile.class, sql, lab, lab);
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
				
				//load profiles
				SelectResult<LabProfile> pres=loadLabProfiles(result.getItem().getId(), forEdit);
				if(!pres.isComplete()){
					result.cloneError(pres);
					return result;
				}
				result.getItem().setProfiles(pres.getData());

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
				List<LabProfile>   addProfileList=new ArrayList<LabProfile>();
				List<LabProfile>   delProfileList=new ArrayList<LabProfile>();

				//profiles
				if(lab.getProfiles()!=null){
					for(LabProfile p :lab.getProfiles()){
						if(p.getProfile_file()!=null && !p.getProfile_file().isEmpty()){
							addProfileList.add(p);
						}else{
							delProfileList.add(p);
						}
					}
				}

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
				if(subResult.isComplete() && !addProfileList.isEmpty()){
					subResult=runInesrtUpdateBatch(addProfileList);
				}
				if(subResult.isComplete() && !delProfileList.isEmpty()){
					subResult=runDeleteBatch(delProfileList);
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
	
	/*
	@Override
	public DmlResult<LabStopLog> logLabStop(LabStopLog log) {
		// нужно проверить time_from на максимум time_to, для того чтобы интервалы не пересекались
		SelectResult<LabStopLog> sResult;
		
		// TODO отправить email в зависимости от типа простоя, SendMailByGoogle.send("Valik <akmeful@gmail.com>", "Valik <akme@tut.by>", "Сообщение о простое2", "Это сообщение о простое2");
		
		String sql = "SELECT MAX(sl.time_to) as time_to FROM lab_stop_log sl WHERE sl.lab_device=? AND sl.time_to>?";
		//String sql2 = "SELECT sl.* FROM lab_stop_log sl WHERE sl.time_to=(SELECT MAX(sl1.time_to) FROM lab_stop_log sl1 WHERE sl1.lab_device=?) AND sl.lab_device=?";
		
		sResult=runSelect(LabStopLog.class, sql, log.getLab_device(), log.getTime_from());
		
		if (sResult.isComplete()){
			if(sResult.getData()!=null && !sResult.getData().isEmpty()){
				LabStopLog sl = sResult.getData().get(0);
				
				if(sl.getTime_to() != null && sl.getTime_to().after(log.getTime_from())){
					log.setTime_from(sl.getTime_to());
				}
				
			}
		}
		
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
	*/

	@Override
	public SelectResult<LabStopType> loadLabStopType(){
		String sql = "SELECT * FROM lab_stop_type";
		return runSelect(LabStopType.class, sql);
	}

	@Override
	public SelectResult<LabStopLog> loadLabStops(Date timeGapStart, Date timeGapEnd, List<Integer> labIds){
		StringBuilder sb=new StringBuilder("");
		String sIn="";
		if(labIds!=null && !labIds.isEmpty()){
			sb.append("IN(");
			for(Integer id : labIds){
				sb.append(id).append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append(")");
			sIn=sb.toString();
		}
		sb=new StringBuilder("");
		sb.append("SELECT ls.id, ls.lab, ls.lab_device, ls.lab_stop_type, ls.time_from, ls.time_to, ls.log_comment, ls.time_created, ls.time_updated, st.name lab_stop_type_name, ld.name device_name, l.name lab_name");
		sb.append(" FROM lab_stop_log ls");
		sb.append(" INNER JOIN lab l ON l.id=ls.lab");
		sb.append(" LEFT OUTER JOIN lab_stop_type st ON st.id = ls.lab_stop_type");
		sb.append(" LEFT OUTER JOIN lab_device ld ON ld.id= ls.lab_device");
		sb.append(" WHERE time_from <= ? AND ls.time_to >= ?");
		if(!sIn.isEmpty()) sb.append(" AND ls.lab ").append(sIn);
		sb.append(" UNION ALL");
		sb.append(" SELECT 0 id, lm.lab, lm.lab_device, lm.state, lm.start_time, NULL time_to, '' log_comment, NULL time_created, NULL time_updated, st.name lab_stop_type_name, ld.name device_name, l.name lab_name");
		sb.append(" FROM lab_meter lm");
		sb.append(" INNER JOIN lab l ON l.id=lm.lab");
		sb.append(" LEFT OUTER JOIN lab_stop_type st ON st.id = lm.state");
		sb.append(" LEFT OUTER JOIN lab_device ld ON ld.id= lm.lab_device");
		sb.append(" WHERE lm.meter_type=10 AND lm.start_time BETWEEN ? AND ?");
		if(!sIn.isEmpty()) sb.append(" AND lm.lab ").append(sIn);
		/*
		String sql = "SELECT ls.id, ls.lab, ls.lab_device, ls.lab_stop_type, ls.time_from, ls.time_to, ls.log_comment, ls.time_created, ls.time_updated, st.name lab_stop_type_name, ld.name device_name"+
					  " FROM lab_stop_log ls"+
					   " LEFT OUTER JOIN lab_stop_type st ON st.id = ls.lab_stop_type"+
					   " LEFT OUTER JOIN lab_device ld ON ld.id= ls.lab_device"+
					  " WHERE time_from <= ? AND ls.time_to >= ?"+
					" UNION ALL"+
					 " SELECT 0 id, lm.lab, lm.lab_device, lm.state, lm.start_time, NULL time_to, '' log_comment, NULL time_created, NULL time_updated, st.name lab_stop_type_name, ld.name device_name"+
					 " FROM lab_meter lm"+
					   " LEFT OUTER JOIN lab_stop_type st ON st.id = lm.state"+
					   " LEFT OUTER JOIN lab_device ld ON ld.id= lm.lab_device"+
					  " WHERE lm.meter_type=10 AND lm.start_time BETWEEN ? AND ?";
					  */
		String sql =sb.toString();
		return runSelect(LabStopLog.class, sql, timeGapEnd, timeGapStart,  timeGapStart, timeGapEnd);
	}

	@Override
	public SelectResult<LabMeter> loadLabMeters(){
		String sql = "SELECT NOW() server_time, lm.*"+
					  " FROM lab l"+
					   " INNER JOIN lab_meter lm ON lm.lab = l.id"+
					  " WHERE l.is_active = 1";
		return runSelect(LabMeter.class, sql);
	}

	@Override
	public SelectResult<LabMeter> showLabMeters(){
		String sql = "SELECT l.name lab_name, ld.name device_name, lmt.name type_name, IF(lm.meter_type=10,lst.name, os.name) state_name, NOW() server_time, lm.*"+
					  " FROM lab_meter lm"+
					  " INNER JOIN lab_meter_type lmt ON lm.meter_type=lmt.id"+
					  " INNER JOIN lab l ON l.id=lm.lab"+
					  " LEFT OUTER JOIN lab_device ld ON ld.id= lm.lab_device"+
					  " LEFT OUTER JOIN order_state os ON lm.state=os.id"+
					  " LEFT OUTER JOIN lab_stop_type lst ON lm.state= lst.id"+
					  " ORDER BY lm.lab, lm.lab_device, lm.meter_type";
		return runSelect(LabMeter.class, sql);
	}

	@Override
	public SelectResult<LabRoll> loadLastRolls(){
		String sql = "SELECT lm.lab, lm.lab_device, pg.width, pg.paper"+
					   " FROM lab_meter lm"+
					   " INNER JOIN print_group pg ON pg.id=lm.print_group"+
					 " WHERE lm.meter_type=1 ";
		return runSelect(LabRoll.class, sql);
	}

	@Override
	public SelectResult<LabRoll> loadOnlineRolls(){
		String sql = "SELECT * FROM lab_rolls lr WHERE lr.is_online=1";
		return runSelect(LabRoll.class, sql);
	}

	@Override
	public SqlResult forwardLabMeter(int lab, int state, String printgroup){
		//PROCEDURE lab_meter_forward_lab(IN plab int(5), IN pstate int(5), IN pprintgroup varchar(50))
		String sql= "{CALL lab_meter_forward_lab(?, ?, ?)}";
		return runCall(sql, lab, state, printgroup);
	}

	@Override
	public SqlResult fixStopMeter(LabMeter meter){
		//PROCEDURE lab_meter_fix_stop(IN plab int(5), IN pdevice int(7), IN pstoptype int(7), IN ptime datetime)
		String sql= "{CALL lab_meter_fix_stop(?, ?, ?, ?)}";
		return runCall(sql, meter.getLab(), meter.getLab_device(), meter.getState(), meter.getStart_time());
	}

	@Override
	public SqlResult endStopMeter(LabMeter meter){
		//PROCEDURE lab_meter_end_stop(IN plab int(5), IN pdevice int(7), IN ptime datetime)
		String sql= "{CALL lab_meter_end_stop(?, ?, ?)}";
		return runCall(sql, meter.getLab(), meter.getLab_device(), meter.getLast_time());
	}

	@Override
	public SelectResult<LabRoll> loadQueueByDevice(int device){
		String sql= "{CALL printLoadQueueByDev(?)}";
		return runCallSelect(LabRoll.class, sql, device);
	}

	@Override
	public SelectResult<LabRoll> loadQueueByLab(int lab){
		String sql= "{CALL printLoadQueueByLab(?)}";
		return runCallSelect(LabRoll.class, sql, lab);
	}

	@Override
	public SqlResult setRollOnline(LabRoll labRoll, boolean isOn){
		int on=0;
		if(isOn) on=1;
		String sql= "UPDATE lab_rolls SET is_online = ? WHERE lab_device = ? AND width = ? AND paper = ?";
		return runDML(sql, on, labRoll.getLab_device(), labRoll.getWidth(), labRoll.getPaper());
	}

	@Override
	public SelectResult<LabRoll> loadInPrintQueueByLab(int lab){
		String sql= "{CALL printLoadInPrintQueueByLab(?)}";
		return runCallSelect(LabRoll.class, sql, lab);
	}


	/*
	@Override
	public SqlResult forwardMeter(int device, int state, String printgroup){
		//PROCEDURE lab_meter_forward(IN pdevice int(7), IN pstate int(5), IN pprintgroup varchar(50))
		String sql= "{CALL lab_meter_forward(?, ?, ?)}";
		return runCall(sql, device, state, printgroup);
	}

	@Override
	public SqlResult forwardMeterByTechPoint(int techPoint, String printgroup){
		//PROCEDURE lab_meter_forward_tp(IN ptechpoint int(7), IN pprintgroup varchar(50))
		String sql= "{CALL lab_meter_forward_tp(?, ?)}";
		return runCall(sql, techPoint, printgroup);
	}
	*/

}
