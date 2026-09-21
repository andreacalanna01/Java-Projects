import java.util.ArrayList;

public class Client {
    private static int nextId = 1;
    private final int clientId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String address;
    private String fiscalCode;
    private final ArrayList<String> activePracticeIds;
    public static final int MAX_PRACTICES = 3;

    public Client(String firstName, String lastName, String email, String phoneNumber, String address, String fiscalCode) {
        this.clientId = nextId++;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.fiscalCode = fiscalCode;
        this.activePracticeIds = new ArrayList<>();
    }

    public int getClientId() { return clientId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAddress() { return address; }
    public String getFiscalCode() { return fiscalCode; }
    public ArrayList<String> getActivePracticeIds() { return activePracticeIds; }
    public String getFullName() { return firstName + " " + lastName; }

    public boolean canSubmit() { return activePracticeIds.size() < MAX_PRACTICES; }
    public void addPractice(String practiceId) { activePracticeIds.add(practiceId); }
    public boolean removePractice(String practiceId) { return activePracticeIds.remove(practiceId); }

    @Override
    public String toString() {
        return "Client ID: " + clientId + " | " + getFullName() + " | Active Apps: " + activePracticeIds.size() + "/" + MAX_PRACTICES;
    }
}