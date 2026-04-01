package com.apu.asc.service;

import com.apu.asc.dao.AppointmentDAO;
import com.apu.asc.dao.FeedbackDAO;
import com.apu.asc.dao.PaymentDAO;
import com.apu.asc.dao.UserDAO;
import com.apu.asc.model.Appointment;
import com.apu.asc.model.ApptStatus;
import com.apu.asc.model.Feedback;
import com.apu.asc.model.Role;
import com.apu.asc.model.User;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.apu.asc.service.ReportService.TechnicianStats;

public interface IReportService {
  public List<TechnicianStats> technicianPerformance();
}
