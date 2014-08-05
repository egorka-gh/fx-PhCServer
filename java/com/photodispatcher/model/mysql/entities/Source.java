package com.photodispatcher.model.mysql.entities;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "phcconfig.sources")
public class Source extends AbstractEntity {
	private static final long serialVersionUID = 1L;

	public static final int LOCATION_TYPE_SOURCE=1;
	public static final int LOCATION_TYPE_LAB=2;
	public static final int LOCATION_TYPE_TECH_POINT=3;

	/*
	public Source() {
		setHasAutoId(true);
	}
	*/

	//database props
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int id;
    @Column(name="name")
    private String name;
    @Column(name="type")
    private int type;
    @Column(name="online")
    private boolean online;
    @Column(name="code")
    private String code;
    
    //ref name
    @Column(name="type_name", updatable=false, insertable=false)
    private String type_name;
    @Column(name="sync", updatable=false, insertable=false)
    private int sync;
    @Column(name="loc_type", updatable=false, insertable=false)
    private int loc_type;

    //childs
    private SourceSvc ftpService;
    private SourceSvc webService;
    private SourceSvc fbookService;
    private SourceSvc hotFolder;

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public boolean isOnline() {
		return online;
	}
	public void setOnline(boolean online) {
		this.online = online;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getType_name() {
		return type_name;
	}
	public void setType_name(String type_name) {
		this.type_name = type_name;
	}
	public SourceSvc getFtpService() {
		return ftpService;
	}
	public void setFtpService(SourceSvc ftpService) {
		this.ftpService = ftpService;
	}
	public SourceSvc getWebService() {
		return webService;
	}
	public void setWebService(SourceSvc webService) {
		this.webService = webService;
	}
	public SourceSvc getFbookService() {
		return fbookService;
	}
	public void setFbookService(SourceSvc fbookService) {
		this.fbookService = fbookService;
	}
	public SourceSvc getHotFolder() {
		return hotFolder;
	}
	public void setHotFolder(SourceSvc hotFolder) {
		this.hotFolder = hotFolder;
	}
	public int getSync() {
		return sync;
	}
	public void setSync(int sync) {
		this.sync = sync;
	}
	public int getLoc_type() {
		return loc_type;
	}
	public void setLoc_type(int loc_type) {
		this.loc_type = loc_type;
	}

}