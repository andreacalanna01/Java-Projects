import java.util.ArrayList;

public class PracticeRegistry<T extends BasePractice> {
    private final String officeName;
    private final ArrayList<T> registryList;

    public PracticeRegistry(String officeName) {
        this.officeName = officeName;
        this.registryList = new ArrayList<>();
    }

    public void addPractice(T practice) {
        registryList.add(practice);
        System.out.println("  + Registered: \"" + practice.getPracticeName() + "\" [" + practice.getId() + "]");
    }

    public T findById(String id) throws PracticeNotFoundException {
        for (T practice : registryList) {
            if (practice.getId().equalsIgnoreCase(id.trim())) return practice;
        }
        throw new PracticeNotFoundException("No active application found with ID: " + id.toUpperCase());
    }

    public ArrayList<T> getRegistryList() { return registryList; }
    public String getOfficeName() { return officeName; }
}