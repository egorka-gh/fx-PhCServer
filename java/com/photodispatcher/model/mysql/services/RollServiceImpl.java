package com.photodispatcher.model.mysql.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.entities.LabRoll;
import com.photodispatcher.model.mysql.entities.Roll;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;

@Service("rollService")
public class RollServiceImpl extends AbstractDAO implements RollService {

	@Override
	public SelectResult<Roll> loadAll(){
		SelectResult<Roll> result;
		String sql="SELECT l.* FROM roll l ORDER BY l.width";
		result=runSelect(Roll.class, sql);
		return result;
	}

	@Override
	public SelectResult<LabRoll> getByDevice(int device, boolean forEdit){
		SelectResult<LabRoll> result;
		String sql;
		if(!forEdit){
			sql="SELECT r.width, r.pixels, ? lab_device, av.id paper, av.value paper_name,"+
					" lr.len_std, lr.len, lr.is_online, 1 is_used"+  
				" FROM roll r"+
				" INNER JOIN lab_rolls lr ON r.width=lr.width"+
				" INNER JOIN attr_value av ON lr.paper=av.id AND av.attr_tp=2"+
				" WHERE lr.lab_device=?"+
				" ORDER BY r.width";
		}else{
			sql="SELECT r.width, r.pixels, ? lab_device, av.id paper, av.value paper_name,"+
					" lr.len_std, lr.len, lr.is_online, ifnull(lr.width,0) is_used"+  
				" FROM roll r"+
				" INNER JOIN attr_value av ON av.attr_tp=2"+
				" LEFT OUTER JOIN lab_rolls lr ON lr.paper=av.id AND r.width=lr.width AND lr.lab_device=?"+
				" ORDER BY r.width";
		}
		result=runSelect(LabRoll.class, sql, device, device);
		return result;
	}

	@Override
	public SelectResult<LabRoll> fillByChannels(int device) {
		String sql="INSERT INTO lab_rolls (lab_device, width, paper)"+
					" SELECT d.id, lr.roll, lr.paper"+
					" FROM lab_device d"+
					" INNER JOIN lab l ON d.lab=l.id"+
					" INNER JOIN (SELECT DISTINCT lpc.src_type, lpc.roll, lpc.paper FROM lab_print_code lpc"+ 
					                  " WHERE lpc.roll IS NOT NULL AND lpc.roll!=0) lr ON lr.src_type=l.src_type"+                   
					" WHERE d.id=?"+
					   " AND NOT EXISTS(SELECT 1 FROM lab_rolls dr WHERE dr.lab_device=d.id AND dr.width=lr.roll AND dr.paper=lr.paper)";
		SqlResult result= runDML(sql, device);
		if(!result.isComplete()){
			SelectResult<LabRoll> err= new SelectResult<LabRoll>();
			err.cloneError(result);
			return err;
		}
		return getByDevice(device, true);
	}

	@Override
	public SqlResult persistBatch(List<LabRoll> items){
		SqlResult result= new SqlResult();
		List<LabRoll> deleteList=new ArrayList<LabRoll>();
		List<LabRoll> insertList=new ArrayList<LabRoll>();
		for(LabRoll item : items){
			if(!item.isIs_used()){
				deleteList.add(item);
			}else{
				insertList.add(item);
			}
		}
		if(!deleteList.isEmpty()) result= runDeleteBatch(deleteList);
		if(!insertList.isEmpty()){
			///update persisted
			if(result.isComplete()) runUpdateBatch(insertList);
			///insert new
			if(result.isComplete()) runInsertBatch(insertList);
		}
		return result;
	}

	@Override
	public SqlResult persistRoll(LabRoll roll){
		SqlResult result= new SqlResult();
		if(roll.getPersistState() == 0){
			// insert
			result=runInsert(roll);
		} else if(roll.getPersistState()==-1){
			// update
			result=runUpdate(roll);
		}
		return result;
	}
	
}
