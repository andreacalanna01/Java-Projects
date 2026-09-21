import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

enum PracticeType {
    TAX_730("730", 15),
    ISEE("ISEE", 10),
    NASPI("NASPI", 25),
    PENSION("Pension ", 40);

    private final String label;
    private final int effort;

    PracticeType(String label, int effort) {
        this.label = label;
        this.effort = effort;
    }

    public String getLabel() { return label; }
    public int getEffort() { return effort; }
}

class CitizenRequest {
    private final Client client;
    private final BasePractice practice;
    private final PracticeType type;

    public CitizenRequest(Client client, BasePractice practice, PracticeType type) {
        this.client = client;
        this.practice = practice;
        this.type = type;
    }

    public Client getClient() { return client; }
    public BasePractice getPractice() { return practice; }
    public PracticeType getType() { return type; }

    @Override
    public String toString() {
        return "[" + practice.getId() + "] " + client.getFullName() + " (" + type.getLabel() + ")";
    }
}

public class Main {
    private final PracticeRegistry<BasePractice> registry;
    private final CafService cafService;

    private final List<CitizenRequest> queue;
    private final List<CitizenRequest> processed;
    private final CitizenRequest[] activeDesks;

    private boolean autoFlowEnabled = true;
    private int idSequence = 100;
    private final Random rand = new Random();

    private final String[] randomNames = {"Leonardi Carlo", "Andrea Calanna", "Gianmarco Cagliari", "Ciccio Ruggieri", "Gabriele Lenzo", "Stefano Fortezza"};
    private final String[] randomAddresses = {"Via Roma 12, Milano", "Corso Italia 45, Roma", "Via Napoli 7, Torino", "Via Etnea 100, Catania"};
    private final String[] randomFiscalCodes = {"LNRCRL95H15H501V", "CLNNDR95H15H501Z", "CGLGMR95H15H501F", "LNZGRL95H15H501J", "FRZSFN95H15H501P"};

    private Runnable onUpdateCallback;

    public Main() {
        this.registry = new PracticeRegistry<>("Central Registry");
        this.cafService = new CafService(registry);
        this.queue = new CopyOnWriteArrayList<>();
        this.processed = new CopyOnWriteArrayList<>();
        this.activeDesks = new CitizenRequest[3];

        startBackgroundEngine();
    }

    public List<CitizenRequest> getQueue() { return queue; }
    public List<CitizenRequest> getProcessed() { return processed; }
    public CitizenRequest[] getActiveDesks() { return activeDesks; }
    public boolean isAutoFlowEnabled() { return autoFlowEnabled; }
    public CafService getCafService() { return cafService; }
    
    public void toggleAutoFlow(boolean enabled) {
        this.autoFlowEnabled = enabled;
        triggerUpdate();
    }

    public void injectCustomCitizen(String fullName, PracticeType pType) throws Exception {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new Exception("Please enter a valid citizen name.");
        }
        
        idSequence++;
        String firstName = fullName;
        String lastName = "User";
        if (fullName.contains(" ")) {
            String[] parts = fullName.split(" ", 2);
            firstName = parts[0];
            lastName = parts[1];
        }

        Client client = findOrCreateClient(firstName, lastName);
        BasePractice practice = createPractice(pType, client);
        
        registry.addPractice(practice);
        CitizenRequest req = new CitizenRequest(client, practice, pType);
        
        queue.add(req);
        triggerUpdate();
    }

    public void processSpecificRequest(CitizenRequest selected) throws Exception {
        if (selected == null) throw new Exception("Select a citizen first.");

        int targetDesk = getFirstAvailableDesk();
        if (targetDesk == -1) throw new Exception("All service desks are busy. Free a desk first.");

        cafService.assignPractice(selected.getClient().getClientId(), selected.getPractice().getId());
        queue.remove(selected);
        activeDesks[targetDesk] = selected;
        
        triggerUpdate();
    }

    public void clearDesk(int deskIndex) throws Exception {
        CitizenRequest req = activeDesks[deskIndex];
        if (req == null) return;

        cafService.archivePractice(req.getClient().getClientId(), req.getPractice().getId());
        activeDesks[deskIndex] = null;
        processed.add(req);
        
        triggerUpdate();
    }

    private int getFirstAvailableDesk() {
        for (int i = 0; i < 3; i++) {
            if (activeDesks[i] == null) return i;
        }
        return -1;
    }

    private void triggerUpdate() {
        if (onUpdateCallback != null) {
            onUpdateCallback.run();
        }
    }

    private Client findOrCreateClient(String firstName, String lastName) {
        for (Client c : cafService.getClients()) {
            if (c.getFirstName().equalsIgnoreCase(firstName) && c.getLastName().equalsIgnoreCase(lastName)) {
                return c;
            }
        }
        String email = firstName.toLowerCase() + "@caf.it";
        String phone = "+39 3" + (10000000 + rand.nextInt(90000000));
        String address = randomAddresses[rand.nextInt(randomAddresses.length)];
        String fiscalCode = randomFiscalCodes[rand.nextInt(randomFiscalCodes.length)];
        
        Client newClient = new Client(firstName, lastName, email, phone, address, fiscalCode);
        cafService.registerClient(newClient);
        return newClient;
    }

    private BasePractice createPractice(PracticeType pType, Client client) {
        if (pType == PracticeType.TAX_730 || pType == PracticeType.ISEE) {
            return new TaxPractice("P" + idSequence, pType.getLabel(), client.getFullName(), "TAX-" + idSequence, 2026, "Standard");
        } else {
            return new WelfarePractice("P" + idSequence, pType.getLabel(), client.getFullName(), idSequence, "June", 2026);
        }
    }

    private void startBackgroundEngine() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(2500);
                    if (autoFlowEnabled && queue.size() < 12) {
                        idSequence++;
                        String fullRandomName = randomNames[rand.nextInt(randomNames.length)];
                        String[] names = fullRandomName.split(" ");
                        
                        Client client = findOrCreateClient(names[0], names[1]);
                        PracticeType pType = PracticeType.values()[rand.nextInt(PracticeType.values().length)];
                        BasePractice practice = createPractice(pType, client);
                        
                        registry.addPractice(practice);
                        queue.add(new CitizenRequest(client, practice, pType));
                        
                        triggerUpdate(); 
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(4000);
                    if (autoFlowEnabled && !queue.isEmpty()) {
                        int desk = getFirstAvailableDesk();
                        if (desk != -1) {
                            CitizenRequest next = queue.get(0);
                            try {
                                cafService.assignPractice(next.getClient().getClientId(), next.getPractice().getId());
                                queue.remove(0);
                                activeDesks[desk] = next;
                                triggerUpdate();
                            } catch (Exception ex) {
                                queue.remove(0); 
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }
}