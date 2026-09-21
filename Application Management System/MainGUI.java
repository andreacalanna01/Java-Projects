import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

enum PracticeType {
    TAX_730("730", 15, new Color(52, 152, 219)),
    ISEE("ISEE", 10, new Color(155, 89, 182)),
    NASPI("NASPI", 25, new Color(46, 204, 113)),
    PENSION("Pension ", 40, new Color(230, 126, 34));

    private final String label;
    private final int effort;
    private final Color color;

    PracticeType(String label, int effort, Color color) {
        this.label = label;
        this.effort = effort;
        this.color = color;
    }

    public String getLabel() { return label; }
    public int getEffort() { return effort; }
    public Color getColor() { return color; }
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

class LiveAnalyticsPanel extends JPanel {
    private final List<CitizenRequest> history;
    private final Color BG_CARD = new Color(34, 34, 46);
    private final Color TEXT_LIGHT = new Color(242, 242, 247);
    private final Color TEXT_MUTED = new Color(152, 152, 170);

    public LiveAnalyticsPanel(List<CitizenRequest> history) {
        this.history = history;
        setBackground(BG_CARD);
        setBorder(new EmptyBorder(15, 15, 15, 15));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        g2.setColor(TEXT_LIGHT);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2.drawString("WORKLOAD DISTRIBUTION (JAVA 2D)", 15, 25);

        PracticeType[] types = PracticeType.values();
        int[] counts = new int[types.length];
        int maxCount = 0;

        for (CitizenRequest req : history) {
            counts[req.getType().ordinal()]++;
        }

        for (int count : counts) {
            if (count > maxCount) maxCount = count;
        }

        int startX = 25;
        int startY = 60;
        int chartHeight = height - 110;
        int barWidth = (width - 50) / types.length - 15;

        g2.setColor(new Color(60, 60, 75));
        g2.drawLine(startX, startY + chartHeight, width - 20, startY + chartHeight);

        for (int i = 0; i < types.length; i++) {
            PracticeType type = types[i];
            int count = counts[i];

            int barHeight = (maxCount == 0) ? 0 : (int) (((double) count / maxCount) * chartHeight);
            int x = startX + i * (barWidth + 15);
            int y = startY + chartHeight - barHeight;

            g2.setColor(type.getColor());
            g2.fill(new RoundRectangle2D.Float(x, y, barWidth, barHeight, 6, 6));

            g2.setColor(TEXT_LIGHT);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.drawString(String.valueOf(count), x + (barWidth / 2) - 4, y - 6);

            g2.setColor(TEXT_MUTED);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            String label = type.getLabel();
            int labelWidth = g2.getFontMetrics().stringWidth(label);
            g2.drawString(label, x + (barWidth / 2) - (labelWidth / 2), startY + chartHeight + 18);
        }
    }
}

public class MainGUI extends JFrame {
    private final PracticeRegistry<BasePractice> registry = new PracticeRegistry<>("Central Registry");
    private final CafService cafService = new CafService(registry);

    private final List<CitizenRequest> queue = new CopyOnWriteArrayList<>();
    private final List<CitizenRequest> processed = new CopyOnWriteArrayList<>();
    private final CitizenRequest[] activeDesks = new CitizenRequest[3];

    private DefaultListModel<CitizenRequest> queueModel;
    private JList<CitizenRequest> queueJList;
    
    private JLabel statusLabel;
    private JLabel processedLabel;
    private LiveAnalyticsPanel analyticsPanel;
    
    private JLabel infoEmail, infoPhone, infoAddress, infoFiscalCode;
    
    private JButton[] deskButtons;
    private JToggleButton autoFlowToggle;
    private JTextField customNameField;
    private JComboBox<PracticeType> typeComboBox;

    private boolean autoFlowEnabled = true;
    private int idSequence = 100;
    private final Random rand = new Random();

    private final Color BG_DARK = new Color(24, 24, 32);
    private final Color BG_CARD = new Color(34, 34, 46);
    private final Color TEXT_LIGHT = new Color(242, 242, 247);
    private final Color ACCENT_BLUE = new Color(41, 128, 185);
    private final Color ACCENT_GREEN = new Color(39, 174, 96);
    private final Color ACCENT_RED = new Color(192, 57, 43);

    private final String[] randomNames = {"Leonardi Carlo", "Andrea Calanna", "Gianmarco Cagliari", "Ciccio Ruggieri", "Gabriele Lenzo", "Stefano Fortezza"};
    private final String[] randomAddresses = {"Via Roma 12, Milano", "Corso Italia 45, Roma", "Via Napoli 7, Torino", "Via Etnea 100, Catania"};
    private final String[] randomFiscalCodes = {"LNRCRL95H15H501V", "CLNNDR95H15H501Z", "CGLGMR95H15H501F", "LNZGRL95H15H501J", "FRZSFN95H15H501P"};

    public MainGUI() {
        setTitle("CAF Enterprise Command Center");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(15, 15));

        buildHeader();
        buildMainGrid();
        buildBottomControlStation();

        startBackgroundEngine();
    }

    private void buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_DARK);
        header.setBorder(new EmptyBorder(15, 20, 0, 20));

        JLabel title = new JLabel("CAF & Patronato Interactive Hub");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_LIGHT);
        header.add(title, BorderLayout.WEST);

        JPanel monitor = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 0));
        monitor.setBackground(BG_DARK);

        statusLabel = new JLabel("Queue Size: 0");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(ACCENT_RED);

        processedLabel = new JLabel("Archived: 0");
        processedLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        processedLabel.setForeground(ACCENT_GREEN);

        monitor.add(statusLabel);
        monitor.add(processedLabel);
        header.add(monitor, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
    }

    private void buildMainGrid() {
        JPanel grid = new JPanel(new GridLayout(1, 3, 15, 0));
        grid.setBackground(BG_DARK);
        grid.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel queueCard = createCardContainer("⏳ Live Waiting Room Queue (Select Item)");
        queueModel = new DefaultListModel<>();
        queueJList = new JList<>(queueModel);
        queueJList.setBackground(BG_CARD);
        queueJList.setForeground(TEXT_LIGHT);
        queueJList.setFont(new Font("Consolas", Font.PLAIN, 12));
        queueJList.setSelectionBackground(new Color(60, 65, 85));
        queueJList.setFixedCellHeight(28);
        queueCard.add(new JScrollPane(queueJList), BorderLayout.CENTER);
        
        JPanel profilePanel = new JPanel(new GridLayout(4, 1, 4, 4));
        profilePanel.setBackground(BG_CARD);
        profilePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(60, 60, 75)), "👤 Selected Citizen Profile", 0, 0, new Font("Segoe UI", Font.BOLD, 11), TEXT_LIGHT),
                new EmptyBorder(8, 8, 8, 8)
        ));

        infoEmail = new JLabel("📧 Email: --");
        infoPhone = new JLabel("📱 Phone: --");
        infoAddress = new JLabel("🏠 Address: --");
        infoFiscalCode = new JLabel("🆔 Fiscal Code: --");

        Font fInfo = new Font("Segoe UI", Font.PLAIN, 12);
        Color cInfo = new Color(185, 185, 200);
        for (JLabel lbl : new JLabel[]{infoEmail, infoPhone, infoAddress, infoFiscalCode}) {
            lbl.setFont(fInfo);
            lbl.setForeground(cInfo);
            profilePanel.add(lbl);
        }

        queueJList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                CitizenRequest selected = queueJList.getSelectedValue();
                if (selected != null) {
                    Client c = selected.getClient();
                    infoEmail.setText("📧 Email: " + c.getEmail());
                    infoPhone.setText("📱 Phone: " + c.getPhoneNumber());
                    infoAddress.setText("🏠 Address: " + c.getAddress());
                    infoFiscalCode.setText("🆔 Fiscal Code: " + c.getFiscalCode());
                } else {
                    resetProfilePanel();
                }
            }
        });

        JPanel queueSouth = new JPanel(new BorderLayout(10, 10));
        queueSouth.setBackground(BG_CARD);
        queueSouth.add(profilePanel, BorderLayout.NORTH);

        JButton processSelectedBtn = new JButton("Process Selected Citizen");
        styleButton(processSelectedBtn, ACCENT_BLUE);
        processSelectedBtn.addActionListener(e -> processSelectedQueueItem());
        queueSouth.add(processSelectedBtn, BorderLayout.SOUTH);
        queueCard.add(queueSouth, BorderLayout.SOUTH);
        grid.add(queueCard);

        analyticsPanel = new LiveAnalyticsPanel(processed);
        grid.add(analyticsPanel);

        JPanel desksCard = createCardContainer("🖥️ Active Operator Service Desks (Click to Complete)");
        JPanel desksGrid = new JPanel(new GridLayout(3, 1, 10, 10));
        desksGrid.setBackground(BG_CARD);
        deskButtons = new JButton[3];
        for (int i = 0; i < 3; i++) {
            final int index = i;
            deskButtons[i] = new JButton("Desk " + (i + 1) + ": VACANT");
            styleButton(deskButtons[i], new Color(45, 52, 54));
            deskButtons[i].setFont(new Font("Segoe UI", Font.BOLD, 12));
            deskButtons[i].addActionListener(e -> clearDeskManually(index));
            desksGrid.add(deskButtons[i]);
        }
        desksCard.add(desksGrid, BorderLayout.CENTER);
        grid.add(desksCard);

        add(grid, BorderLayout.CENTER);
    }

    private void buildBottomControlStation() {
        JPanel footer = new JPanel(new BorderLayout(10, 10));
        footer.setBackground(BG_CARD);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(60, 60, 75)),
                new EmptyBorder(15, 20, 15, 20)
        ));

        JPanel manualForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        manualForm.setBackground(BG_CARD);

        JLabel nameLabel = new JLabel("Citizen Name:");
        nameLabel.setForeground(TEXT_LIGHT);
        manualForm.add(nameLabel);

        customNameField = new JTextField(12);
        customNameField.setBackground(BG_DARK);
        customNameField.setForeground(TEXT_LIGHT);
        customNameField.setCaretColor(TEXT_LIGHT);
        customNameField.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 95)));
        manualForm.add(customNameField);

        JLabel typeLabel = new JLabel("Practice Type:");
        typeLabel.setForeground(TEXT_LIGHT);
        manualForm.add(typeLabel);

        typeComboBox = new JComboBox<>(PracticeType.values());
        typeComboBox.setBackground(BG_DARK);
        typeComboBox.setForeground(TEXT_LIGHT);
        manualForm.add(typeComboBox);

        JButton injectBtn = new JButton("Inject Citizen");
        styleButton(injectBtn, ACCENT_GREEN);
        injectBtn.addActionListener(e -> injectCustomCitizen());
        manualForm.add(injectBtn);

        footer.add(manualForm, BorderLayout.WEST);

        autoFlowToggle = new JToggleButton("🤖 Auto-Simulation: ON");
        styleButton(autoFlowToggle, ACCENT_BLUE);
        autoFlowToggle.addActionListener(e -> {
            autoFlowEnabled = autoFlowToggle.isSelected();
            autoFlowToggle.setText(autoFlowEnabled ? "🤖 Auto-Simulation: ON" : "⏸️ Auto-Simulation: PAUSED");
            autoFlowToggle.setBackground(autoFlowEnabled ? ACCENT_BLUE : new Color(127, 140, 141));
        });
        autoFlowToggle.setSelected(true);
        footer.add(autoFlowToggle, BorderLayout.EAST);

        add(footer, BorderLayout.SOUTH);
    }

    private JPanel createCardContainer(String titleText) {
        JPanel wrapper = new JPanel(new BorderLayout(10, 10));
        wrapper.setBackground(BG_CARD);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(55, 55, 70), 1),
                new EmptyBorder(12, 12, 12, 12)
        ));
        JLabel label = new JLabel(titleText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_LIGHT);
        wrapper.add(label, BorderLayout.NORTH);
        return wrapper;
    }

    private void styleButton(AbstractButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
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
                        
                        Client client = null;
                        for (Client c : cafService.getClients()) {
                            if (c.getFirstName().equals(names[0]) && c.getLastName().equals(names[1])) {
                                client = c;
                                break;
                            }
                        }

                        if (client == null) {
                            String email = names[0].toLowerCase() + "@caf.it";
                            String phone = "+39 3" + (10000000 + rand.nextInt(90000000));
                            String address = randomAddresses[rand.nextInt(randomAddresses.length)];
                            String fiscalCode = randomFiscalCodes[rand.nextInt(randomFiscalCodes.length)];
                            
                            client = new Client(names[0], names[1], email, phone, address, fiscalCode);
                            cafService.registerClient(client);
                        }

                        PracticeType pType = PracticeType.values()[rand.nextInt(PracticeType.values().length)];
                        BasePractice practice;
                        if (pType == PracticeType.TAX_730 || pType == PracticeType.ISEE) {
                            practice = new TaxPractice("P" + idSequence, pType.getLabel(), client.getFullName(), "TAX-" + idSequence, 2026, "Standard");
                        } else {
                            practice = new WelfarePractice("P" + idSequence, pType.getLabel(), client.getFullName(), idSequence, "June", 2026);
                        }
                        registry.addPractice(practice);

                        CitizenRequest req = new CitizenRequest(client, practice, pType);
                        queue.add(req);
                        SwingUtilities.invokeLater(() -> {
                            queueModel.addElement(req);
                            updateInterfaceMetrics();
                        });
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
                        for (int i = 0; i < 3; i++) {
                            if (activeDesks[i] == null) {
                                deployNextToDesk(i);
                                break;
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    private synchronized void injectCustomCitizen() {
        String name = customNameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a valid citizen name.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        idSequence++;
        String firstName = name;
        String lastName = "User";
        if (name.contains(" ")) {
            String[] parts = name.split(" ", 2);
            firstName = parts[0];
            lastName = parts[1];
        }

        Client client = null;
        for (Client c : cafService.getClients()) {
            if (c.getFirstName().equalsIgnoreCase(firstName) && c.getLastName().equalsIgnoreCase(lastName)) {
                client = c;
                break;
            }
        }

        if (client == null) {
            String email = firstName.toLowerCase() + "@custom.it";
            String phone = "+39 3" + (10000000 + rand.nextInt(90000000));
            String address = "Via delle Pratiche " + (rand.nextInt(99) + 1) + ", Roma";
            String fiscalCode = (lastName.length() >= 3 ? lastName.substring(0, 3) : "XYZ").toUpperCase() + 
                                (firstName.length() >= 3 ? firstName.substring(0, 3) : "ABC").toUpperCase() + "85M01H501Y";

            client = new Client(firstName, lastName, email, phone, address, fiscalCode);
            cafService.registerClient(client);
        }

        PracticeType pType = (PracticeType) typeComboBox.getSelectedItem();
        BasePractice practice;
        if (pType == PracticeType.TAX_730 || pType == PracticeType.ISEE) {
            practice = new TaxPractice("P" + idSequence, pType.getLabel(), client.getFullName(), "TAX-" + idSequence, 2026, "Custom");
        } else {
            practice = new WelfarePractice("P" + idSequence, pType.getLabel(), client.getFullName(), idSequence, "June", 2026);
        }
        registry.addPractice(practice);

        CitizenRequest req = new CitizenRequest(client, practice, pType);
        queue.add(req);
        queueModel.addElement(req);
        customNameField.setText("");
        updateInterfaceMetrics();
    }

    private synchronized void processSelectedQueueItem() {
        CitizenRequest selected = queueJList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select a citizen from the waiting list first.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int targetDesk = -1;
        for (int i = 0; i < 3; i++) {
            if (activeDesks[i] == null) {
                targetDesk = i;
                break;
            }
        }

        if (targetDesk == -1) {
            JOptionPane.showMessageDialog(this, "All service desks are busy. Free a desk first.", "Desks Full", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            cafService.assignPractice(selected.getClient().getClientId(), selected.getPractice().getId());
            queue.remove(selected);
            queueModel.removeElement(selected);
            occupyDesk(targetDesk, selected);
            resetProfilePanel();
        } catch (CafException | PracticeNotFoundException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "CAF Backend Core Exception", JOptionPane.ERROR_MESSAGE);
        }
    }

    private synchronized void deployNextToDesk(int deskIndex) {
        if (queue.isEmpty() || activeDesks[deskIndex] != null) return;
        CitizenRequest next = queue.get(0);
        
        try {
            cafService.assignPractice(next.getClient().getClientId(), next.getPractice().getId());
            queue.remove(0);
            SwingUtilities.invokeLater(() -> {
                queueModel.removeElement(next);
                resetProfilePanel();
            });
            occupyDesk(deskIndex, next);
        } catch (CafException | PracticeNotFoundException ex) {
            queue.remove(0);
            SwingUtilities.invokeLater(() -> queueModel.removeElement(next));
        }
    }

    private void occupyDesk(int index, CitizenRequest req) {
        activeDesks[index] = req;
        SwingUtilities.invokeLater(() -> {
            deskButtons[index].setText("<html><center><b>DESK " + (index + 1) + " (BUSY)</b><br>" 
                    + req.getClient().getFullName() + " - " + req.getType().getLabel() + "<br><font color='#E74C3C'>⚡ Click to Finish</font></center></html>");
            deskButtons[index].setBackground(new Color(44, 62, 80));
            updateInterfaceMetrics();
        });
    }

    private synchronized void clearDeskManually(int index) {
        CitizenRequest req = activeDesks[index];
        if (req == null) return;

        try {
            cafService.archivePractice(req.getClient().getClientId(), req.getPractice().getId());
            activeDesks[index] = null;
            processed.add(req);

            SwingUtilities.invokeLater(() -> {
                deskButtons[index].setText("Desk " + (index + 1) + ": VACANT");
                deskButtons[index].setBackground(new Color(45, 52, 54));
                updateInterfaceMetrics();
            });
        } catch (CafException | PracticeNotFoundException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "CAF Backend Core Exception", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetProfilePanel() {
        infoEmail.setText("📧 Email: --");
        infoPhone.setText("📱 Phone: --");
        infoAddress.setText("🏠 Address: --");
        infoFiscalCode.setText("🆔 Fiscal Code: --");
    }

    private void updateInterfaceMetrics() {
        statusLabel.setText("Queue Size: " + queue.size());
        processedLabel.setText("Archived: " + processed.size());
        analyticsPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGUI().setVisible(true));
    }
}