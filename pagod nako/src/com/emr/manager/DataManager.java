package com.emr.manager;

import com.emr.dao.*;
import com.emr.models.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.Period;

public class DataManager {
    private final UserDAO userDAO;
    private final PatientDAO patientDAO;
    private final MedicalRecordDAO medicalRecordDAO;
    private final MedicalStaffDAO medicalStaffDAO;
    private final StaffScheduleDAO staffScheduleDAO;
    
    private MedicalStaff currentStaff;
    
    public DataManager() {
        this.userDAO = new UserDAO();
        this.patientDAO = new PatientDAO();
        this.medicalRecordDAO = new MedicalRecordDAO();
        this.medicalStaffDAO = new MedicalStaffDAO();
        this.staffScheduleDAO = new StaffScheduleDAO();
    }
    
    // ==================== AUTHENTICATION & USER MANAGEMENT ====================
    
    public User authenticateUser(String username, String password) {
        try {
            User user = userDAO.authenticate(username, password);
            if (user != null && (user.getRole() == User.UserRole.DOCTOR || user.getRole() == User.UserRole.NURSE)) {
                this.currentStaff = medicalStaffDAO.getStaffByUserId(user.getId());
            }
            return user;
        } catch (Exception e) {
            handleException("Authentication error", e);
            return null;
        }
    }
    
    public boolean createUser(User user) {
        return executeOperation(() -> userDAO.createUser(user), "creating user");
    }
    
    public List<User> getAllUsers() {
        return executeQuery(userDAO::getAllUsers, "fetching users");
    }
    
    // ==================== PATIENT MANAGEMENT ====================
    
    public boolean createPatient(Patient patient) {
        return executeOperation(() -> patientDAO.createPatient(patient), "creating patient");
    }
    
    public List<Patient> getAllPatients() {
        return executeQuery(patientDAO::getAllPatients, "fetching patients");
    }
    
    public boolean updatePatient(Patient patient) {
        return executeOperation(() -> patientDAO.updatePatient(patient), "updating patient");
    }
    
    public boolean deletePatient(int patientId) {
        return executeOperation(() -> patientDAO.deletePatient(patientId), "deleting patient");
    }
    
    public Patient getPatientById(int patientId) {
        return executeQuery(() -> patientDAO.getPatientById(patientId), "fetching patient by ID");
    }
    
    public Patient findPatientByUserId(int userId) {
        return executeQuery(() -> 
            getAllPatients().stream()
                .filter(patient -> patient.getUserId() != null && patient.getUserId() == userId)
                .findFirst()
                .orElse(null),
            "finding patient by user ID"
        );
    }
    
    // ==================== MEDICAL RECORDS MANAGEMENT ====================
    
    public boolean createMedicalRecord(MedicalRecord record) {
        return executeOperation(() -> medicalRecordDAO.createMedicalRecord(record), "creating medical record");
    }
    
    public List<MedicalRecord> getPatientRecords(int patientId) {
        return executeQuery(() -> medicalRecordDAO.getRecordsByPatientId(patientId), "fetching patient records");
    }
    
    public boolean deleteMedicalRecord(int recordId) {
        return executeOperation(() -> medicalRecordDAO.deleteMedicalRecord(recordId), "deleting medical record");
    }
    
    // ==================== MEDICAL STAFF MANAGEMENT ====================
    
    public List<MedicalStaff> getAllMedicalStaff() {
        return executeQuery(medicalStaffDAO::getAllMedicalStaff, "fetching medical staff");
    }
    
    public MedicalStaff getStaffByUserId(int userId) {
        return executeQuery(() -> medicalStaffDAO.getStaffByUserId(userId), "fetching staff by user ID");
    }
    
    public MedicalStaff getCurrentStaff() {
        return currentStaff;
    }
    
    // ==================== SCHEDULE MANAGEMENT ====================
    
    public boolean createSchedule(StaffSchedule schedule) {
        return executeOperation(() -> staffScheduleDAO.createSchedule(schedule), "creating schedule");
    }
    
    public boolean updateSchedule(StaffSchedule schedule) {
        return executeOperation(() -> staffScheduleDAO.updateSchedule(schedule), "updating schedule");
    }
    
    public List<StaffSchedule> getStaffSchedules(int staffId) {
        return executeQuery(() -> staffScheduleDAO.getSchedulesByStaffId(staffId), "fetching staff schedules");
    }
    
    public List<StaffSchedule> getAllSchedules() {
        return executeQuery(staffScheduleDAO::getAllSchedules, "fetching all schedules");
    }
    
    // ==================== STATISTICS METHODS (For StatisticsChart) ====================
    
    public Map<String, Long> getGenderDistribution() {
        return executeQuery(() -> {
            List<Patient> patients = getAllPatients();
            return patients.stream()
                .filter(patient -> patient.getGender() != null)
                .collect(Collectors.groupingBy(
                    Patient::getGender,
                    Collectors.counting()
                ));
        }, "calculating gender distribution");
    }
    
    public Map<String, Long> getAgeGroupDistribution() {
        return executeQuery(() -> {
            List<Patient> patients = getAllPatients();
            return patients.stream()
                .collect(Collectors.groupingBy(
                    patient -> getAgeGroup(calculateAge(patient)),
                    Collectors.counting()
                ));
        }, "calculating age group distribution");
    }
    
    public Map<String, Long> getPriorityDistribution() {
        return executeQuery(() -> {
            List<Patient> patients = getAllPatients();
            return patients.stream()
                .collect(Collectors.groupingBy(
                    patient -> "Level " + patient.getPriorityLevel(),
                    Collectors.counting()
                ));
        }, "calculating priority distribution");
    }
    
    public Map<String, Long> getVisitStatusDistribution() {
        return executeQuery(() -> {
            List<Patient> patients = getAllPatients();
            return patients.stream()
                .collect(Collectors.groupingBy(
                    patient -> patient.isVisited() ? "Visited" : "Not Visited",
                    Collectors.counting()
                ));
        }, "calculating visit status distribution");
    }
    
    public Map<String, Long> getActivityTypeDistribution() {
        return executeQuery(() -> {
            List<StaffSchedule> schedules = getAllSchedules();
            return schedules.stream()
                .collect(Collectors.groupingBy(
                    schedule -> schedule.getActivityType().toString(),
                    Collectors.counting()
                ));
        }, "calculating activity type distribution");
    }
    
    public Map<String, Long> getScheduleStatusDistribution() {
        return executeQuery(() -> {
            List<StaffSchedule> schedules = getAllSchedules();
            return schedules.stream()
                .collect(Collectors.groupingBy(
                    schedule -> schedule.getStatus().toString(),
                    Collectors.counting()
                ));
        }, "calculating schedule status distribution");
    }
    
    // ==================== HELPER METHODS FOR STATISTICS ====================
    
    private int calculateAge(Patient patient) {
        if (patient.getDateOfBirth() == null) {
            return 0;
        }
        try {
            LocalDate birthDate = patient.getDateOfBirth();
            LocalDate currentDate = LocalDate.now();
            return Period.between(birthDate, currentDate).getYears();
        } catch (Exception e) {
            return 0;
        }
    }
    
    private String getAgeGroup(int age) {
        if (age <= 18) return "0-18";
        else if (age <= 35) return "19-35";
        else if (age <= 50) return "36-50";
        else if (age <= 65) return "51-65";
        else return "65+";
    }
    
    // ==================== DASHBOARD METRICS ====================
    
    public int getTotalPatients() {
        return executeQuery(() -> getAllPatients().size(), "counting total patients");
    }
    
    public long getVisitedPatientsCount() {
        return executeQuery(() -> 
            getAllPatients().stream()
                .filter(Patient::isVisited)
                .count(),
            "counting visited patients"
        );
    }
    
    public long getHighPriorityPatientsCount() {
        return executeQuery(() -> 
            getAllPatients().stream()
                .filter(patient -> patient.getPriorityLevel() >= 4)
                .count(),
            "counting high priority patients"
        );
    }
    
    public long getMediumPriorityPatientsCount() {
        return executeQuery(() -> 
            getAllPatients().stream()
                .filter(patient -> patient.getPriorityLevel() >= 2 && patient.getPriorityLevel() <= 3)
                .count(),
            "counting medium priority patients"
        );
    }
    
    public long getLowPriorityPatientsCount() {
        return executeQuery(() -> 
            getAllPatients().stream()
                .filter(patient -> patient.getPriorityLevel() == 1)
                .count(),
            "counting low priority patients"
        );
    }
    
    public int getTotalMedicalRecords() {
        return executeQuery(() -> {
            List<Patient> patients = getAllPatients();
            return patients.stream()
                .mapToInt(patient -> getPatientRecords(patient.getId()).size())
                .sum();
        }, "counting total medical records");
    }
    
    public int getTotalSchedules() {
        return executeQuery(() -> getAllSchedules().size(), "counting total schedules");
    }
    
    // ==================== VALIDATION METHODS ====================
    
    public boolean isUsernameAvailable(String username) {
        return executeQuery(() -> 
            getAllUsers().stream()
                .noneMatch(user -> user.getUsername().equalsIgnoreCase(username)),
            "checking username availability"
        );
    }
    
    public boolean validatePatientData(Patient patient) {
        if (patient == null) return false;
        return patient.getName() != null && !patient.getName().trim().isEmpty() &&
               patient.getDateOfBirth() != null;
    }
    
    // ==================== SEARCH METHODS ====================
    
    public List<Patient> searchPatients(String query) {
        return executeQuery(() -> {
            String searchQuery = query.toLowerCase();
            return getAllPatients().stream()
                    .filter(patient -> 
                        patient.getName().toLowerCase().contains(searchQuery) ||
                        patient.getContactInfo().toLowerCase().contains(searchQuery) ||
                        String.valueOf(patient.getId()).contains(searchQuery))
                    .collect(Collectors.toList());
        }, "searching patients");
    }
    
    // ==================== UTILITY METHODS ====================
    
    private <T> T executeQuery(DataQuery<T> query, String operation) {
        try {
            return query.execute();
        } catch (Exception e) {
            handleException("Error " + operation, e);
            return null;
        }
    }
    
    private boolean executeOperation(DataOperation operation, String action) {
        try {
            return operation.execute();
        } catch (Exception e) {
            handleException("Error " + action, e);
            return false;
        }
    }
    
    private void handleException(String message, Exception e) {
        System.err.println(message + ": " + e.getMessage());
        e.printStackTrace();
    }
    
    public void clearCurrentStaff() {
        this.currentStaff = null;
    }
    
    // ==================== FUNCTIONAL INTERFACES ====================
    
    @FunctionalInterface
    private interface DataQuery<T> {
        T execute() throws Exception;
    }
    
    @FunctionalInterface
    private interface DataOperation {
        boolean execute() throws Exception;
    }
    
    // ==================== COMPREHENSIVE STATISTICS ====================
    
    public StatisticsSummary getStatisticsSummary() {
        return executeQuery(() -> {
            StatisticsSummary summary = new StatisticsSummary();
            
            List<Patient> patients = getAllPatients();
            List<StaffSchedule> schedules = getAllSchedules();
            List<MedicalStaff> staff = getAllMedicalStaff();
            
            // Basic counts
            summary.setTotalPatients(patients.size());
            summary.setTotalStaff(staff.size());
            summary.setTotalSchedules(schedules.size());
            summary.setTotalMedicalRecords(getTotalMedicalRecords());
            
            // Distributions for charts
            summary.setGenderDistribution(getGenderDistribution());
            summary.setAgeGroupDistribution(getAgeGroupDistribution());
            summary.setPriorityDistribution(getPriorityDistribution());
            summary.setVisitStatusDistribution(getVisitStatusDistribution());
            summary.setActivityTypeDistribution(getActivityTypeDistribution());
            summary.setScheduleStatusDistribution(getScheduleStatusDistribution());
            
            // Additional metrics
            summary.setAveragePatientAge(calculateAverageAge(patients));
            summary.setMostCommonAgeGroup(findMostCommonAgeGroup(patients));
            summary.setBusiestDay(findBusiestDay(schedules));
            
            return summary;
        }, "generating statistics summary");
    }
    
    private double calculateAverageAge(List<Patient> patients) {
        if (patients.isEmpty()) return 0;
        return patients.stream()
            .mapToInt(this::calculateAge)
            .average()
            .orElse(0);
    }
    
    private String findMostCommonAgeGroup(List<Patient> patients) {
        Map<String, Long> ageGroups = getAgeGroupDistribution();
        return ageGroups.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("Unknown");
    }
    
    private String findBusiestDay(List<StaffSchedule> schedules) {
        if (schedules.isEmpty()) return "No schedules";
        
        Map<String, Long> dayCounts = schedules.stream()
            .collect(Collectors.groupingBy(
                schedule -> schedule.getScheduleDate().toString(),
                Collectors.counting()
            ));
        
        return dayCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("Unknown");
    }
    
    // ==================== STATISTICS SUMMARY CLASS ====================
    
    public static class StatisticsSummary {
        private int totalPatients;
        private int totalStaff;
        private int totalSchedules;
        private int totalMedicalRecords;
        private double averagePatientAge;
        private String mostCommonAgeGroup;
        private String busiestDay;
        
        private Map<String, Long> genderDistribution;
        private Map<String, Long> ageGroupDistribution;
        private Map<String, Long> priorityDistribution;
        private Map<String, Long> visitStatusDistribution;
        private Map<String, Long> activityTypeDistribution;
        private Map<String, Long> scheduleStatusDistribution;
        
        // Getters and setters
        public int getTotalPatients() { return totalPatients; }
        public void setTotalPatients(int totalPatients) { this.totalPatients = totalPatients; }
        
        public int getTotalStaff() { return totalStaff; }
        public void setTotalStaff(int totalStaff) { this.totalStaff = totalStaff; }
        
        public int getTotalSchedules() { return totalSchedules; }
        public void setTotalSchedules(int totalSchedules) { this.totalSchedules = totalSchedules; }
        
        public int getTotalMedicalRecords() { return totalMedicalRecords; }
        public void setTotalMedicalRecords(int totalMedicalRecords) { this.totalMedicalRecords = totalMedicalRecords; }
        
        public double getAveragePatientAge() { return averagePatientAge; }
        public void setAveragePatientAge(double averagePatientAge) { this.averagePatientAge = averagePatientAge; }
        
        public String getMostCommonAgeGroup() { return mostCommonAgeGroup; }
        public void setMostCommonAgeGroup(String mostCommonAgeGroup) { this.mostCommonAgeGroup = mostCommonAgeGroup; }
        
        public String getBusiestDay() { return busiestDay; }
        public void setBusiestDay(String busiestDay) { this.busiestDay = busiestDay; }
        
        public Map<String, Long> getGenderDistribution() { return genderDistribution; }
        public void setGenderDistribution(Map<String, Long> genderDistribution) { this.genderDistribution = genderDistribution; }
        
        public Map<String, Long> getAgeGroupDistribution() { return ageGroupDistribution; }
        public void setAgeGroupDistribution(Map<String, Long> ageGroupDistribution) { this.ageGroupDistribution = ageGroupDistribution; }
        
        public Map<String, Long> getPriorityDistribution() { return priorityDistribution; }
        public void setPriorityDistribution(Map<String, Long> priorityDistribution) { this.priorityDistribution = priorityDistribution; }
        
        public Map<String, Long> getVisitStatusDistribution() { return visitStatusDistribution; }
        public void setVisitStatusDistribution(Map<String, Long> visitStatusDistribution) { this.visitStatusDistribution = visitStatusDistribution; }
        
        public Map<String, Long> getActivityTypeDistribution() { return activityTypeDistribution; }
        public void setActivityTypeDistribution(Map<String, Long> activityTypeDistribution) { this.activityTypeDistribution = activityTypeDistribution; }
        
        public Map<String, Long> getScheduleStatusDistribution() { return scheduleStatusDistribution; }
        public void setScheduleStatusDistribution(Map<String, Long> scheduleStatusDistribution) { this.scheduleStatusDistribution = scheduleStatusDistribution; }
        
        @Override
        public String toString() {
            return String.format(
                "Statistics Summary:\n" +
                "Total Patients: %d\n" +
                "Total Staff: %d\n" +
                "Total Schedules: %d\n" +
                "Total Medical Records: %d\n" +
                "Average Patient Age: %.1f\n" +
                "Most Common Age Group: %s\n" +
                "Busiest Day: %s",
                totalPatients, totalStaff, totalSchedules, totalMedicalRecords,
                averagePatientAge, mostCommonAgeGroup, busiestDay
            );
        }
    }
}