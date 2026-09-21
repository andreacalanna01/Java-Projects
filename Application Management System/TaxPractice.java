public class TaxPractice extends BasePractice {
    private String taxCode;
    private int financialYear;
    private String incomeType;
    private static final int PROCESSING_DAYS = 15;

    public TaxPractice(String id, String practiceName, String applicantName, String taxCode, int financialYear, String incomeType) {
        super(id, practiceName, applicantName);
        this.taxCode = taxCode;
        this.financialYear = financialYear;
        this.incomeType = incomeType;
    }

    @Override
    public String getDepartment() { return "Tax (CAF)"; }

    @Override
    public int getProcessingDays() { return PROCESSING_DAYS; }

    public String getTaxCode() { return taxCode; }
    public int getFinancialYear() { return financialYear; }
    public String getIncomeType() { return incomeType; }

    @Override
    public String toString() {
        return super.toString() + " | FiscalCode: " + taxCode + " | Year: " + financialYear + " | Income: " + incomeType;
    }
}