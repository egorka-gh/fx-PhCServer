package com.photodispatcher.model.mysql.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.entities.DmlResult;
import com.photodispatcher.model.mysql.entities.LabRoll;
import com.photodispatcher.model.mysql.entities.Roll;
import com.photodispatcher.model.mysql.entities.SelectResult;

@Service("rollService")
public class RollServiceImpl extends AbstractDAO implements RollService {

	@Override
	public SelectResult<Roll> loadAll(){
		SelectResult<Roll> result;
		String sql="SELECT l.* FROM phcconfig.roll l ORDER BY l.width";
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
				" FROM phcconfig.roll r"+
				" INNER JOIN phcconfig.lab_rolls lr ON r.width=lr.width"+
				" INNER JOIN phcconfig.attr_value av ON lr.paper=av.id AND av.attr_tp=2"+
				" WHERE lr.lab_device=?"+
				" ORDER BY r.width";
		}else{
			sql="SELECT r.width, r.pixels, ? lab_device, av.id paper, av.value paper_name,"+
					" lr.len_std, lr.len, lr.is_online, ifnull(lr.width,0) is_used"+  
				" FROM phcconfig.roll r"+
				" INNER JOIN phcconfig.attr_value av ON av.attr_tp=2"+
				" LEFT OUTER JOIN phcconfig.lab_rolls lr ON lr.paper=av.id AND r.width=lr.width AND lr.lab_device=?"+
				" ORDER BY r.width";
		}
		result=runSelect(LabRoll.class, sql, device, device);
		return result;
	}

	@Override
	public DmlResult fillByChannels(int device) {
		String sql="INSERT INTO phcconfig.lab_rolls (lab_device, width, paper)"+
					" SELECT d.id, lr.roll, lr.paper"+
					" FROM phcconfig.lab_device d"+
					" INNER JOIN phcconfig.lab l ON d.lab=l.id"+
					" INNER JOIN (SELECT DISTINCT lpc.src_type, lpc.roll, lpc.paper FROM phcconfig.lab_print_code lpc"+ 
					                  " WHERE lpc.roll IS NOT NULL AND lpc.roll!=0) lr ON lr.src_type=l.src_type"+                   
					" WHERE d.id=?"+
					   " AND NOT EXISTS(SELECT 1 FROM phcconfig.lab_rolls dr WHERE dr.lab_device=d.id AND dr.width=lr.roll AND dr.paper=lr.paper)";
		DmlResult result= runDML(sql, true, device);
		return result;
	}

	@Override
	public DmlResult persistBatch(List<LabRoll> items){
		DmlResult result= new DmlResult();
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
			if(result.isComplete()) runUpdateBatch(insertList,null);
			///insert new
			if(result.isComplete()) runInsertBatch(insertList,null);
		}
		return result;
	}

	
	
}
