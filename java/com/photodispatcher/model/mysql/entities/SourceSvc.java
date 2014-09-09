package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "services")
public class SourceSvc extends AbstractEntity {
	public static final int WEB_SERVICE=1;
	public static final int FTP_SERVICE=4;
	public static final int FBOOK_SERVICE=5;
	public static final int HOT_FOLDER=10;

	private static final long serialVersionUID = 1L;

	/*
	public SourceSvc() {
		setHasAutoId(true);
	}
	*/

	//database props
    @Id
	@Column(name="src_id")
    private int src_id;
    @Id
	@Column(name="srvc_id")
    private int srvc_id;
    @Column(name="url")
    private String url;
    @Column(name="user")
    private String user;
    @Column(name="pass")
    private String pass;
	@Column(name="connections")
    private int connections;

    //ref name
    @Column(name="type_name", updatable=false, insertable=false)
    private String type_name;
    @Column(name="loc_type", updatable=false, insertable=false)
    private int loc_type;

	public int getSrc_id() {
		return src_id;
	}

	public void setSrc_id(int src_id) {
		this.src_id = src_id;
	}

	public int getSrvc_id() {
		return srvc_id;
	}

	public void setSrvc_id(int srvc_id) {
		this.srvc_id = srvc_id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public int getConnections() {
		return connections;
	}

	public void setConnections(int connections) {
		this.connections = connections;
	}

	public String getType_name() {
		return type_name;
	}

	public void setType_name(String type_name) {
		this.type_name = type_name;
	}

	public int getLoc_type() {
		return loc_type;
	}

	public void setLoc_type(int loc_type) {
		this.loc_type = loc_type;
	}

}
