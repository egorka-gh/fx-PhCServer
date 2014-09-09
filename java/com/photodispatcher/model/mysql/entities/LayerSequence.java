package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "layer_sequence")
public class LayerSequence extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
    @Id
    @Column(name="layerset")
    private int layerset;
    @Id
    @Column(name="layer_group")
    private int layer_group;
    @Id
    @Column(name="seqorder")
    private int seqorder;
    @Column(name="seqlayer")
    private int seqlayer;
    
	//db drived
    @Column(name="seqlayer_name", updatable=false, insertable=false)
    private String seqlayer_name;

	public int getLayerset() {
		return layerset;
	}

	public void setLayerset(int layerset) {
		this.layerset = layerset;
	}

	public int getLayer_group() {
		return layer_group;
	}

	public void setLayer_group(int layer_group) {
		this.layer_group = layer_group;
	}

	public int getSeqorder() {
		return seqorder;
	}

	public void setSeqorder(int seqorder) {
		this.seqorder = seqorder;
	}

	public int getSeqlayer() {
		return seqlayer;
	}

	public void setSeqlayer(int seqlayer) {
		this.seqlayer = seqlayer;
	}

	public String getSeqlayer_name() {
		return seqlayer_name;
	}

	public void setSeqlayer_name(String seqlayer_name) {
		this.seqlayer_name = seqlayer_name;
	}


}
