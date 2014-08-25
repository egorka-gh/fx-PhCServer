package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "phcconfig.suborders_template")
public class SubordersTemplate extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
	//database props
    @Id
    @GeneratedValue
    @Column(name="id")
    private int id;
    @Column(name="src_type")
    private int src_type;
    @Column(name="sub_src_type")
    private int sub_src_type;
    @Column(name="folder")
    private int folder;

    public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getSrc_type() {
		return src_type;
	}
	public void setSrc_type(int src_type) {
		this.src_type = src_type;
	}
	public int getSub_src_type() {
		return sub_src_type;
	}
	public void setSub_src_type(int sub_src_type) {
		this.sub_src_type = sub_src_type;
	}
	public int getFolder() {
		return folder;
	}
	public void setFolder(int folder) {
		this.folder = folder;
	}
	
}
