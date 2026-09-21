public class WelfarePractice extends BasePractice {
    private int protocolNumber;
    private String submissionMonth;
    private int submissionYear;
    private static final int PROCESSING_DAYS = 45;

    public WelfarePractice(String id, String practiceName, String applicantName, int protocolNumber, String submissionMonth, int submissionYear) {
        super(id, practiceName, applicantName);
        this.protocolNumber = protocolNumber;
        this.submissionMonth = submissionMonth;
        this.submissionYear = submissionYear;
    }

    @Override
    public String getDepartment() { return "Patronato "; }

    @Override
    public int getProcessingDays() { return PROCESSING_DAYS; }

    public int getProtocolNumber() { return protocolNumber; }
    public String getSubmissionMonth() { return submissionMonth; }
    public int getSubmissionYear() { return submissionYear; }

    @Override
    public String toString() {
        return super.toString() + " | Protocol: #" + protocolNumber + " | Filed: " + submissionMonth + " " + submissionYear;
    }
}