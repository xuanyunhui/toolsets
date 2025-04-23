package cn.ac.bestheme.toolsets.mqa.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.ArrayList;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Program {
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("reference_number")
    private String referenceNumber;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("level")
    private String level;
    
    @JsonProperty("field")
    private String field;

    @JsonProperty("details")
    private ProgramDetails details;

    public static class ProgramDetails {
        @JsonProperty("certificate_number")
        private String certificateNumber;

        @JsonProperty("previous_name")
        private String previousName;

        @JsonProperty("accreditation_date")
        private String accreditationDate;

        @JsonProperty("compliance_audit")
        private String complianceAudit;

        @JsonProperty("mqf_level")
        private String mqfLevel;

        @JsonProperty("nec_field")
        private String necField;

        @JsonProperty("number_of_credits")
        private String numberOfCredits;

        @JsonProperty("mode_of_study")
        private String modeOfStudy;

        @JsonProperty("mode_of_delivery")
        private String modeOfDelivery;

        @JsonProperty("remarks")
        private String remarks;

        @JsonProperty("duration_table")
        private List<DurationRow> durationTable;

        @JsonProperty("study_schedule")
        private List<StudyScheduleRow> studySchedule;

        public static class DurationRow {
            @JsonProperty("type")
            private String type;

            @JsonProperty("weeks_per_semester")
            private String weeksPerSemester;

            @JsonProperty("semesters")
            private String semesters;

            @JsonProperty("duration")
            private String duration;

            public DurationRow(String type, String weeksPerSemester, String semesters, String duration) {
                this.type = type;
                this.weeksPerSemester = weeksPerSemester;
                this.semesters = semesters;
                this.duration = duration;
            }

            // Getters and Setters
            public String getType() { return type; }
            public void setType(String type) { this.type = type; }
            public String getWeeksPerSemester() { return weeksPerSemester; }
            public void setWeeksPerSemester(String weeksPerSemester) { this.weeksPerSemester = weeksPerSemester; }
            public String getSemesters() { return semesters; }
            public void setSemesters(String semesters) { this.semesters = semesters; }
            public String getDuration() { return duration; }
            public void setDuration(String duration) { this.duration = duration; }
        }

        public static class StudyScheduleRow {
            @JsonProperty("starting")
            private String starting;

            @JsonProperty("weeks_per_semester")
            private String weeksPerSemester;

            @JsonProperty("semesters")
            private String semesters;

            @JsonProperty("industrial_training")
            private String industrialTraining;

            @JsonProperty("years")
            private String years;

            @JsonProperty("credits")
            private String credits;

            public StudyScheduleRow(String starting, String weeksPerSemester, String semesters, 
                                  String industrialTraining, String years, String credits) {
                this.starting = starting;
                this.weeksPerSemester = weeksPerSemester;
                this.semesters = semesters;
                this.industrialTraining = industrialTraining;
                this.years = years;
                this.credits = credits;
            }

            // Getters and Setters
            public String getStarting() { return starting; }
            public void setStarting(String starting) { this.starting = starting; }
            public String getWeeksPerSemester() { return weeksPerSemester; }
            public void setWeeksPerSemester(String weeksPerSemester) { this.weeksPerSemester = weeksPerSemester; }
            public String getSemesters() { return semesters; }
            public void setSemesters(String semesters) { this.semesters = semesters; }
            public String getIndustrialTraining() { return industrialTraining; }
            public void setIndustrialTraining(String industrialTraining) { this.industrialTraining = industrialTraining; }
            public String getYears() { return years; }
            public void setYears(String years) { this.years = years; }
            public String getCredits() { return credits; }
            public void setCredits(String credits) { this.credits = credits; }
        }

        public ProgramDetails() {
            this.durationTable = new ArrayList<>();
            this.studySchedule = new ArrayList<>();
        }

        // Getters and Setters
        public String getCertificateNumber() { return certificateNumber; }
        public void setCertificateNumber(String certificateNumber) { this.certificateNumber = certificateNumber; }
        public String getPreviousName() { return previousName; }
        public void setPreviousName(String previousName) { this.previousName = previousName; }
        public String getAccreditationDate() { return accreditationDate; }
        public void setAccreditationDate(String accreditationDate) { this.accreditationDate = accreditationDate; }
        public String getComplianceAudit() { return complianceAudit; }
        public void setComplianceAudit(String complianceAudit) { this.complianceAudit = complianceAudit; }
        public String getMqfLevel() { return mqfLevel; }
        public void setMqfLevel(String mqfLevel) { this.mqfLevel = mqfLevel; }
        public String getNecField() { return necField; }
        public void setNecField(String necField) { this.necField = necField; }
        public String getNumberOfCredits() { return numberOfCredits; }
        public void setNumberOfCredits(String numberOfCredits) { this.numberOfCredits = numberOfCredits; }
        public String getModeOfStudy() { return modeOfStudy; }
        public void setModeOfStudy(String modeOfStudy) { this.modeOfStudy = modeOfStudy; }
        public String getModeOfDelivery() { return modeOfDelivery; }
        public void setModeOfDelivery(String modeOfDelivery) { this.modeOfDelivery = modeOfDelivery; }
        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
        public List<DurationRow> getDurationTable() { return durationTable; }
        public void setDurationTable(List<DurationRow> durationTable) { this.durationTable = durationTable; }
        public List<StudyScheduleRow> getStudySchedule() { return studySchedule; }
        public void setStudySchedule(List<StudyScheduleRow> studySchedule) { this.studySchedule = studySchedule; }
    }

    public Program(String id, String referenceNumber, String name, String type, String level, String field) {
        this.id = id;
        this.referenceNumber = referenceNumber;
        this.name = name;
        this.type = type;
        this.level = level;
        this.field = field;
        this.details = new ProgramDetails();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public ProgramDetails getDetails() {
        return details;
    }

    public void setDetails(ProgramDetails details) {
        this.details = details;
    }
} 