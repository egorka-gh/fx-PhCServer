package com.photodispatcher.model.mysql.services;

import java.util.List;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.DeliveryTypePrintForm;
import com.photodispatcher.model.mysql.entities.PrintFormFieldItem;
import com.photodispatcher.model.mysql.entities.PrintFormParametr;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.report.Parameter;
import com.photodispatcher.model.mysql.entities.report.Report;
import com.photodispatcher.model.mysql.entities.report.ReportGroup;
import com.photodispatcher.model.mysql.entities.report.ReportResult;
import com.photodispatcher.model.mysql.entities.report.ReportSource;
import com.photodispatcher.model.mysql.entities.report.ReportSourceType;



@RemoteDestination(id="xReportService", source="xReportService")
public interface XReportService {
	
	public List<ReportSourceType> getSourceTypes();
	public List<ReportSource> getSources();
	public List<ReportGroup> getGroups(int sourceType);
	public List<Report> getReports(final int sourceType);
	public List<Parameter> getReportParams(final String report);
	public ReportResult buildReport(final Report report, String source);
	SelectResult<PrintFormFieldItem> getPrintFormFieldItems();
	SelectResult<DeliveryTypePrintForm> getPrintForms();
	SelectResult<PrintFormParametr> getPrintFormParameters();
	
}
