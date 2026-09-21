public abstract class BasePractice {
    private final String id;
    private String practiceName;
    private String applicantName;
    private boolean pending;
    private static int totalPractices = 0;

    public BasePractice(String id, String practiceName, String applicantName) {
        this.id = id.trim().toUpperCase();
        this.practiceName = practiceName;
        this.applicantName = applicantName;
        this.pending = true;
        totalPractices++;
    }

    public abstract String getDepartment();
    public abstract int getProcessingDays();

    public String getId() { return id; }
    public String getPracticeName() { return practiceName; }
    public String getApplicantName() { return applicantName; }
    public boolean isPending() { return pending; }
    public void setPending(boolean pending) { this.pending = pending; }
    public static int getTotalPractices() { return totalPractices; }

    @Override
    public String toString() {
        return "[" + id + "] " + practiceName + " (" + applicantName + ")"
             + " | Dept: " + getDepartment() 
             + " | Status: " + (pending ? "Ready" : "In Progress") 
             + " | Est: " + getProcessingDays() + " days";
    }
}