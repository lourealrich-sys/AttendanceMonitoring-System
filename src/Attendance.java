import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.IDateEvaluator;

public class Attendance extends JFrame {
    private JTextField txtId, txtName, txtSearch;
    private JDateChooser dateChooser;
    private DefaultTableModel model;
    private JTable table;
    private final String FILE_NAME = "attendance11.txt";
    private boolean isLoading = false;

    // DATA STORAGE: [5 Students][5 Days] (1=Present, 2=Absent)
    private int[][] attendanceGrid = new int[5][5];
    private Date[] allowedDates = new Date[5];
    private int selectedRowForEdit = -1;

    // ORIGINAL COLORS
    Color navyDark = new Color(0x2C3E50), successGreen = new Color(0x27AE60),
            formalRed = new Color(0xC0392B), steelBlue = new Color(0x2980B9), offWhite = new Color(0xECF0F1);

    public Attendance() {
        initDates();
        setupUI();
        loadFromFile();
        setVisible(true);
    }

    private boolean isIdDuplicate(String id, int excludeRow) {
        for (int i = 0; i < model.getRowCount(); i++) {
            if (i == excludeRow) continue;
            if (model.getValueAt(i, 0).toString().equals(id)) return true;
        }
        return false;
    }

    private void initDates() {
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < 5; i++) {
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == Calendar.SATURDAY) {
                cal.add(Calendar.DATE, 2);
            } else if (dayOfWeek == Calendar.SUNDAY) {
                cal.add(Calendar.DATE, 1);
            }
            allowedDates[i] = cal.getTime();
            cal.add(Calendar.DATE, 1);
        }
    }

    private void setupUI() {
        setTitle("Attendance Monitoring System");
        setSize(1350, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));

        // --- TOP PANEL ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, navyDark));
        topPanel.setPreferredSize(new Dimension(1100, 80));

        dateChooser = new JDateChooser(allowedDates[0]);
        dateChooser.setPreferredSize(new Dimension(150, 30));
        dateChooser.getJCalendar().getDayChooser().addDateEvaluator(new SelectableDateEvaluator());

        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 25));
        datePanel.setOpaque(false);
        datePanel.add(new JLabel("DATE:"));
        datePanel.add(dateChooser);

        txtSearch = new JTextField(15);
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 25));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("SEARCH:"));
        searchPanel.add(txtSearch);

        topPanel.add(datePanel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // --- CENTER: TABLE ---
        String[] columns = {"ID Number", "Name", "Total Present", "Total Absent", "Total Attendance", "%", "Mark Attendance"};
        model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return c == 6; }
        };
        table = new JTable(model);
        table.setRowHeight(50);

        JTableHeader header = table.getTableHeader();
        header.setBackground(navyDark); header.setForeground(Color.WHITE);
        header.setFont(new Font("SansSerif", Font.BOLD, 13));

        table.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JCheckBox()));
        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- BOTTOM PANEL ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        bottomPanel.setBackground(offWhite);

        txtId = new JTextField(10); txtName = new JTextField(15);
        JButton btnAdd = createStyledBtn("ADD STUDENT", navyDark);
        JButton btnEdit = createStyledBtn("EDIT INFO", steelBlue);
        JButton btnDelete = createStyledBtn("DELETE RECORD", Color.GRAY);

        bottomPanel.add(new JLabel("ID:")); bottomPanel.add(txtId);
        bottomPanel.add(new JLabel("NAME:")); bottomPanel.add(txtName);
        bottomPanel.add(btnAdd); bottomPanel.add(btnEdit); bottomPanel.add(btnDelete);
        add(bottomPanel, BorderLayout.SOUTH);

        // --- ACTIONS ---
        btnAdd.addActionListener(e -> {
            if (model.getRowCount() >= 5) {
                JOptionPane.showMessageDialog(this, "Max 5 students.");
                return;}
            String id = txtId.getText().trim();
            String name = txtName.getText().trim();
            if (id.isEmpty() || name.isEmpty()) return;
            if (isIdDuplicate(id, -1)) {
                JOptionPane.showMessageDialog(this, "Duplicate ID!");
                return;
            }
            model.addRow(new Object[]{id, name, "0", "0", "0", "0%", ""});
            saveToFile();
            txtId.setText(""); txtName.setText("");
        });

        btnEdit.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (selectedRowForEdit == -1 && viewRow == -1){
                JOptionPane.showMessageDialog(this, "Select row to Edit");
                return;}
            if (selectedRowForEdit == -1 && viewRow != -1) {
                selectedRowForEdit = table.convertRowIndexToModel(viewRow);
                txtId.setText(model.getValueAt(selectedRowForEdit, 0).toString());
                txtName.setText(model.getValueAt(selectedRowForEdit, 1).toString());
                btnEdit.setText("UPDATE"); btnAdd.setEnabled(false);
            } else if (selectedRowForEdit != -1) {
                String newId = txtId.getText().trim();
                String newName = txtName.getText().trim();
                if (isIdDuplicate(newId, selectedRowForEdit)) {
                    JOptionPane.showMessageDialog(this, "Cannot update: ID " + newId + " is already used by another student!");
                    return; // Stops the update process
                }
                model.setValueAt(txtId.getText(), selectedRowForEdit, 0);
                model.setValueAt(txtName.getText(), selectedRowForEdit, 1);
                saveToFile();
                btnEdit.setText("EDIT INFO"); btnAdd.setEnabled(true);
                selectedRowForEdit = -1;
                txtId.setText(""); txtName.setText("");
            }
        });

        btnDelete.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if(viewRow == -1){
                JOptionPane.showMessageDialog(this, "Select row to Delete.");
                return;
            } else if (viewRow != -1) {
                int modelRow = table.convertRowIndexToModel(viewRow);
                // Clear the data for this row in the grid
                for(int j=0; j<5; j++) attendanceGrid[modelRow][j] = 0;
                model.removeRow(modelRow);
                saveToFile();
            }
        });

        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                TableRowSorter<DefaultTableModel> tr = new TableRowSorter<>(model);
                table.setRowSorter(tr);
                tr.setRowFilter(RowFilter.regexFilter("(?i)" + txtSearch.getText()));

            }
        });
    }

    public void markAttendance(int type, int row) {
        int dayIdx = -1;
        for(int i=0; i<5; i++) if(isSameDay(dateChooser.getDate(), allowedDates[i])) dayIdx = i;

        if(dayIdx != -1 && row != -1) {
            attendanceGrid[row][dayIdx] = type;
            updateStats(row);
            saveToFile();
        }
    }

    private void updateStats(int row) {
        int p = 0, a = 0;
        for(int j=0; j<5; j++) {
            if(attendanceGrid[row][j] == 1) p++;
            else if(attendanceGrid[row][j] == 2) a++;
        }
        int tot = p + a;
        model.setValueAt(String.valueOf(p), row, 2);
        model.setValueAt(String.valueOf(a), row, 3);
        model.setValueAt(String.valueOf(tot), row, 4);
        model.setValueAt((tot == 0 ? 0 : (p * 100 / tot)) + "%", row, 5);
    }

    private void saveToFile()  {
        if (isLoading) return;
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d");
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME, false))) {
            pw.println("WEEKLY ATTENDANCE: " + sdf.format(allowedDates[0]) + " - " + sdf.format(allowedDates[4]));
            pw.println("-------------------------------------------------------------------------------");

            StringBuilder header = new StringBuilder(String.format("%-10s | %-15s", "ID", "NAME"));
            for (Date d : allowedDates) {
                header.append(String.format(" | %-10s", sdf.format(d)));
            }
            pw.println(header.toString());
            pw.println("-".repeat(header.length()));

            for (int i = 0; i < model.getRowCount(); i++) {
                StringBuilder row = new StringBuilder(String.format("%-10s | %-15s",
                        model.getValueAt(i, 0), model.getValueAt(i, 1)));
                for (int j = 0; j < 5; j++) {
                    String status = (attendanceGrid[i][j] == 1) ? "PRESENT" : (attendanceGrid[i][j] == 2 ? "ABSENT" : "-");
                    row.append(String.format(" | %-10s", status));
                }
                pw.println(row.toString());
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;
        isLoading = true;
        model.setRowCount(0);
        // Reset grid to zeros before loading
        for (int i = 0; i < 5; i++) Arrays.fill(attendanceGrid[i], 0);

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int r = 0;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("-") || line.contains("ID") || line.startsWith("WEEKLY")) continue;

                String[] p = line.split("\\|");
                if (p.length >= 7 && r < 5) {
                    String id = p[0].trim();
                    String name = p[1].trim();

                    model.addRow(new Object[]{id, name, "0", "0", "0", "0%", ""});
                    for (int j = 0; j < 5; j++) {
                        String stat = p[j+2].trim();
                        if (stat.equals("PRESENT")) attendanceGrid[r][j] = 1;
                        else if (stat.equals("ABSENT")) attendanceGrid[r][j] = 2;
                    }
                    updateStats(r);
                    r++;
                }
            }
        } catch (Exception e) { e.printStackTrace();
        } finally { isLoading = false; }
    }

    private boolean isSameDay(Date d1, Date d2) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        return fmt.format(d1).equals(fmt.format(d2));
    }

    private JButton createStyledBtn(String t, Color b) {
        JButton btn = new JButton(t); btn.setBackground(b); btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 11)); btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        return btn;
    }

    class SelectableDateEvaluator implements IDateEvaluator {
        public boolean isSpecial(Date d) { return false; }
        public Color getSpecialForegroundColor() { return null; }
        public Color getSpecialBackroundColor() { return null; }
        public String getSpecialTooltip() { return null; }
        public boolean isInvalid(Date d) {
            for (Date a : allowedDates) if (isSameDay(d, a)) return false;
            return true;
        }
        public Color getInvalidForegroundColor() { return Color.LIGHT_GRAY; }
        public Color getInvalidBackroundColor() { return null; }
        public String getInvalidTooltip() { return "Outside Range"; }
    }

    class ButtonRenderer extends JPanel implements TableCellRenderer {
        JButton p = new JButton("PRESENT"), a = new JButton("ABSENT");
        public ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 10));
            p.setBackground(successGreen); p.setForeground(Color.WHITE);
            a.setBackground(formalRed); a.setForeground(Color.WHITE);
            add(p); add(a);
        }
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            setBackground(s ? t.getSelectionBackground() : Color.WHITE); return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 10));
        int editingRow;
        public ButtonEditor(JCheckBox c) {
            super(c);
            JButton p = new JButton("PRESENT"), a = new JButton("ABSENT");
            p.setBackground(successGreen); p.setForeground(Color.WHITE);
            a.setBackground(formalRed); a.setForeground(Color.WHITE);
            p.addActionListener(e -> { markAttendance(1, editingRow); stopCellEditing(); });
            a.addActionListener(e -> { markAttendance(2, editingRow); stopCellEditing(); });
            panel.add(p); panel.add(a);
        }
        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            this.editingRow = t.convertRowIndexToModel(r);
            panel.setBackground(t.getSelectionBackground()); return panel;
        }
    }

    public static void main(String[] args) { new Attendance(); }
}