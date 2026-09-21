import java.util.ArrayList;

public class CafService {
    private final PracticeRegistry<BasePractice> registry;
    private final ArrayList<Client> clients;

    public CafService(PracticeRegistry<BasePractice> registry) {
        this.registry = registry;
        this.clients = new ArrayList<>();
    }

    public void registerClient(Client client) {
        clients.add(client);
        System.out.println("  + Client Registered: " + client.getFullName() + " (ID: " + client.getClientId() + ")");
    }

    public void assignPractice(int clientId, String practiceId) throws CafException, PracticeNotFoundException {
        Client client = getClientById(clientId);
        BasePractice practice = registry.findById(practiceId);

        if (!client.canSubmit()) {
            throw new CafException(client.getFullName() + " has reached the concurrent file processing limit (" + Client.MAX_PRACTICES + ").");
        }
        if (!practice.isPending()) {
            throw new CafException("Application \"" + practice.getPracticeName() + "\" is already being processed by another operator.");
        }

        practice.setPending(false);
        client.addPractice(practice.getId());
    }

    public void archivePractice(int clientId, String practiceId) throws CafException, PracticeNotFoundException {
        Client client = getClientById(clientId);
        BasePractice practice = registry.findById(practiceId);

        if (practice.isPending()) {
            throw new CafException("Application \"" + practice.getPracticeName() + "\" is not currently assigned or active.");
        }

        practice.setPending(true);
        client.removePractice(practice.getId());
    }

    private Client getClientById(int clientId) throws CafException {
        for (Client client : clients) {
            if (client.getClientId() == clientId) return client;
        }
        throw new CafException("No client found in database with ID: " + clientId);
    }

    public ArrayList<Client> getClients() { return clients; }
}