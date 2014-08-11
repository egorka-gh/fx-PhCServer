package com.photodispatcher.model.mysql.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.entities.Layer;
import com.photodispatcher.model.mysql.entities.LayerSequence;
import com.photodispatcher.model.mysql.entities.Layerset;
import com.photodispatcher.model.mysql.entities.LayersetGroup;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;

@Service("techPickerService")
public class TechPickerServiceImpl extends AbstractDAO implements TechPickerService {
	public static final int LAYER_EMPTY=0;
	public static final int LAYER_SHEET=1;
	public static final int LAYER_ENDPAPER=2;
	public static final int LAYERSET_TYPE_TEMPLATE=0;
	public static final int LAYERSET_TYPE_INTERLAYER=1;
	public static final int LAYERSET_TYPE_ENDPAPER=2;
	public static final int SEQGROUP_START=0;
	public static final int SEQGROUP_MID=2;
	public static final int SEQGROUP_END=3;

	
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
		SelectResult<Layerset> result;
		String sql="SELECT s.*, bt.name book_type_name"+
					" FROM phcconfig.layerset s"+
					" INNER JOIN phcconfig.book_type bt ON bt.id=s.book_type"+
					" WHERE s.subset_type=?"+
				" ORDER BY s.passover DESC, s.name";
		result= runSelect(Layerset.class, sql, type);
		if (result.isComplete()){
			for(Layerset ls : result.getData()){
				SelectResult<LayerSequence> subRes=loadSequence(ls.getId());
				if(subRes.isComplete()){
					List<LayerSequence> seqStart=new ArrayList<LayerSequence>();
					List<LayerSequence> seqMid=new ArrayList<LayerSequence>();
					List<LayerSequence> seqEnd=new ArrayList<LayerSequence>();
					for(LayerSequence seq :subRes.getData()){
						if(seq.getLayer_group()==SEQGROUP_START){
							seqStart.add(seq);
						}else if(seq.getLayer_group()==SEQGROUP_MID){
							seqMid.add(seq);
						}else if(seq.getLayer_group()==SEQGROUP_END){
							seqEnd.add(seq);
						}
						if(seq.getSeqlayer()==LAYER_ENDPAPER) ls.setUsesEndPaper(true);
					}
					ls.setSequenceEnd(seqEnd);
					ls.setSequenceMiddle(seqMid);
					ls.setSequenceStart(seqStart);
				}else{
					result.cloneError(subRes);
				}
			}
			
		}
		return result;
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

	
	private SelectResult<LayerSequence> loadSequence(int layerset){
		SelectResult<LayerSequence> result;
		String sql="SELECT la.*, l.name  seqlayer_name"+
					" FROM phcconfig.layer_sequence la"+ 
					" INNER JOIN phcconfig.layer l ON l.id=la.seqlayer"+
					" WHERE la.layerset=?"+
					" ORDER BY la.layer_group, la.seqorder";
		result=runSelect(LayerSequence.class, sql, layerset);
		return result;
	}

	private SelectResult<LayerSequence> loadSequence(int layerset, int layergroup){
		SelectResult<LayerSequence> result;
		String sql="SELECT la.*, l.name  seqlayer_name"+
					" FROM phcconfig.layer_sequence la"+ 
					" INNER JOIN phcconfig.layer l ON l.id=la.seqlayer"+
					" WHERE la.layerset=? AND la.layer_group=?"+
					" ORDER BY la.layer_group, la.seqorder";
		result=runSelect(LayerSequence.class, sql, layerset, layergroup);
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
			sRes=loadSequence(layerset, layerGroup);
		}else{
			sRes.cloneError(result);
		}
		return sRes;
	}

	/*
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
*/
}
