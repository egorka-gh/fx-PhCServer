package com.photodispatcher.model.mysql.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.entities.Endpaper;
import com.photodispatcher.model.mysql.entities.Layer;
import com.photodispatcher.model.mysql.entities.LayerSequence;
import com.photodispatcher.model.mysql.entities.Layerset;
import com.photodispatcher.model.mysql.entities.LayersetGroup;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;

@Service("techPickerService")
public class TechPickerServiceImpl extends AbstractDAO implements TechPickerService {

	
	@Override
	public SelectResult<LayersetGroup> loadLayersetGroups(){
		SelectResult<LayersetGroup> result;
		String sql="SELECT s.* FROM phcconfig.layerset_group s";
		result=runSelect(LayersetGroup.class, sql);
		return result;
	}

	@Override
	public SqlResult persistLayersetGroups(List<LayersetGroup> items){
		return runPersistBatch(items);
	}

	@Override
	public SelectResult<Layerset> loadLayersets(int type){
		String sql="SELECT s.*, bt.name book_type_name"+
					" FROM phcconfig.layerset s"+
					" INNER JOIN phcconfig.book_type bt ON bt.id=s.book_type"+
					" WHERE s.subset_type=?"+
				" ORDER BY s.is_passover DESC, s.name";
		return runSelect(Layerset.class, sql, type);
	}

	@Override
	public SqlResult persistLayersets(List<Layerset> items){
		return runPersistBatch(items);
	}

	@Override
	public SelectResult<Layer> loadLayers(){
		SelectResult<Layer> result;
		String sql="SELECT s.id, s.name FROM phcconfig.layer s";
		result=runSelect(Layer.class, sql);
		return result;
	}

	@Override
	public SelectResult<Layer> persistLayers(List<Layer> items){
		SelectResult<Layer> result= new SelectResult<Layer>(); 
		SqlResult res=runPersistBatch(items);
		if(!res.isComplete()){
			result.cloneError(res);
			return result;
		}
		return loadLayers();
	}

	@Override
	public SelectResult<LayerSequence> loadtSequence(int layerset){
		SelectResult<LayerSequence> result;
		String sql="SELECT la.layerset, la.layer_group, la.seqorder, la.seqlayer, l.name  seqlayer_name"+
					" FROM phcconfig.layer_sequence la"+ 
					" INNER JOIN phcconfig.layer l ON l.id=la.seqlayer"+
					" WHERE la.layerset=?"+
					" ORDER BY la.layer_group, la.seqorder";
		result=runSelect(LayerSequence.class, sql, layerset);
		return result;
	}

	@Override
	public SelectResult<LayerSequence> persistSequence(List<LayerSequence> items, int layerset, int layerGroup){
		SelectResult<LayerSequence> sRes= new SelectResult<LayerSequence>(); 
		SqlResult result= new SqlResult();
		if(items==null) return sRes;
		
		List<LayerSequence> updateList=new ArrayList<LayerSequence>();
		int seq=0;
		for(LayerSequence item : items){
			if(item.getSeqlayer()>1){
				//renum
				seq++;
				item.setSeqorder(seq);
				updateList.add(item);
			}
		}
		//save
		if(!updateList.isEmpty()){
			result=runUpdateBatch(updateList);
			if(result.isComplete()) result=runInsertBatch(updateList);
		}
		//del unused
		if(result.isComplete()){
			String sql="DELETE FROM phcconfig.layer_sequence WHERE layerset=? AND layer_group=? AND seqorder>?";
			result=runDML(sql, layerset, layerGroup, seq);
		}
		if(result.isComplete()){
			sRes=loadtSequence(layerset);
		}else{
			sRes.cloneError(result);
		}
		return sRes;
	}

	@Override
	public SelectResult<Endpaper> loadEndpapers(){
		SelectResult<Endpaper> result;
		String sql="SELECT s.id, s.name FROM phcconfig.endpaper s";
		result=runSelect(Endpaper.class, sql);
		return result;
	}

	@Override
	public SqlResult persistEndpapers(List<Endpaper> items){
		for(Endpaper item : items){
			if(item.getId()==0){
				//predefined
				items.remove(item);
				break;
			}
		}
		return runPersistBatch(items);
	}

}
