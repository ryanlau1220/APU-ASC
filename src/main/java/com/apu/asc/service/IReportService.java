package com.apu.asc.service;

import java.util.List;
import java.util.Map;

import com.apu.asc.service.ReportService.TechnicianStats;

public interface IReportService {
  public List<TechnicianStats> technicianPerformance();
  public Map<String, Double> revenueByMonth();
}
