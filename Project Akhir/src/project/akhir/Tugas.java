package project.akhir;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.JTable;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Desktop;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.JDialog;
import java.text.SimpleDateFormat;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import javax.imageio.ImageIO;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import java.sql.Date; 
import java.text.SimpleDateFormat;
import javax.swing.event.TableModelEvent;
import org.jfree.chart.plot.PlotOrientation;


public class Tugas extends javax.swing.JFrame {
    Connection conn;
    private DefaultTableModel tabel_m_dapat;
    private DefaultTableModel tabel_m_keluar;
    private Tugas2 tugas2Instance;


    public Tugas() {
    initComponents();
    setLocationRelativeTo(null);

    conn = koneksi.getConnection();
    tabel_m_dapat = new DefaultTableModel();
    tabel_m_keluar = new DefaultTableModel();
    
    dtabel.setModel(tabel_m_dapat);
    ktabel.setModel(tabel_m_keluar);

    tabel_m_dapat.addColumn("ID Pendapatan");
    tabel_m_dapat.addColumn("Jumlah Pendapatan");
    tabel_m_dapat.addColumn("Tanggal Pendapatan");
    tabel_m_dapat.addColumn("Keterangan Pendapatan");

    tabel_m_keluar.addColumn("ID Pengeluaran");
    tabel_m_keluar.addColumn("Jumlah Pengeluaran");
    tabel_m_keluar.addColumn("Tanggal Pengeluaran");
    tabel_m_keluar.addColumn("Keterangan Pendapatan");
    dtabel.setModel(tabel_m_dapat);
    dtabel.setCellSelectionEnabled(false); 
    dtabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); 
    dtabel.setRowSelectionAllowed(true);

    dtabel.setDefaultEditor(Object.class, null);

    ktabel.setModel(tabel_m_keluar);
    ktabel.setCellSelectionEnabled(false); 
    ktabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); 
    ktabel.setRowSelectionAllowed(true); 

    ktabel.setDefaultEditor(Object.class, null);
    LoadDataPemasukan();
    LoadDataPengeluaran();
    showDiagramOnPanel();
    LoadPemasukanIDs(); 
    LoadPengeluaranIDs(); 
    updateATotal();
    }





private int calculateSum(String sql) throws SQLException {
    int sum = 0;
    PreparedStatement ps = conn.prepareStatement(sql);
    ResultSet rs = ps.executeQuery();
    if (rs.next()) {
        sum = rs.getInt(1);
    }
    return sum;
}

    public void refreshData() {
    LoadDataPemasukan();
    LoadDataPengeluaran(); 
}
 
public void kembaliDariTugas2() {
    this.setVisible(true); 
    refreshData();       
}

private void showDiagramOnPanel() {
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();

    try {
        String sqlPendapatan = "SELECT MONTH(tanggal_dapat) AS bulan, SUM(jumlah_dapat) AS total_jumlah " +
                               "FROM td_pemasukan " +
                               "GROUP BY MONTH(tanggal_dapat)";
        PreparedStatement stmtPendapatan = conn.prepareStatement(sqlPendapatan);
        ResultSet rsPendapatan = stmtPendapatan.executeQuery();

        while (rsPendapatan.next()) {
            int bulan = rsPendapatan.getInt("bulan");
            int totalJumlah = rsPendapatan.getInt("total_jumlah");
            dataset.addValue(totalJumlah, "Pemasukan", getMonthName(bulan));
        }

        String sqlPengeluaran = "SELECT keterangan_keluar AS kategori, SUM(jumlah_keluar) AS total_jumlah " +
                                "FROM td_pengeluaran " +
                                "GROUP BY keterangan_keluar";
        PreparedStatement stmtPengeluaran = conn.prepareStatement(sqlPengeluaran);
        ResultSet rsPengeluaran = stmtPengeluaran.executeQuery();

        while (rsPengeluaran.next()) {
            String kategori = rsPengeluaran.getString("kategori");
            int totalJumlah = rsPengeluaran.getInt("total_jumlah");
            dataset.addValue(totalJumlah, "Pengeluaran", kategori);
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error memuat data diagram: " + e.getMessage());
    }

    JFreeChart barChart = ChartFactory.createBarChart(
            "Pemasukan dan Pengeluaran",
            "Kategori",
            "Jumlah",
            dataset,
            PlotOrientation.VERTICAL,
            true, true, false);

    CategoryPlot plot = barChart.getCategoryPlot();
    BarRenderer renderer = (BarRenderer) plot.getRenderer();
    renderer.setSeriesPaint(0, Color.BLUE);
    renderer.setSeriesPaint(1, Color.RED);

    ChartPanel chartPanel = new ChartPanel(barChart);
    chartPanel.setPreferredSize(new java.awt.Dimension(800, 400));

    statik.removeAll();
    statik.setLayout(new BorderLayout());
    statik.add(chartPanel, BorderLayout.CENTER);

    statik.revalidate();
    statik.repaint();
}

private String getMonthName(int month) {
    String[] months = {
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    };
    return months[month - 1];
}






public void exportToPDF(DefaultTableModel tableModel, String filePath, String title) {
    Document document = new Document();
    try {
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        document.add(new Phrase(title + "\n\n")); 

        PdfPTable table = new PdfPTable(tableModel.getColumnCount());

        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            table.addCell(new PdfPCell(new Phrase(tableModel.getColumnName(i))));
        }

        for (int row = 0; row < tableModel.getRowCount(); row++) {
            for (int col = 0; col < tableModel.getColumnCount(); col++) {
                table.addCell(new PdfPCell(new Phrase(tableModel.getValueAt(row, col).toString())));
            }
        }
        document.add(table);
        JOptionPane.showMessageDialog(this, "PDF berhasil dibuat di: " + filePath);
    } catch (DocumentException | IOException e) {
        JOptionPane.showMessageDialog(this, "Error dalam membuat PDF: " + e.getMessage());
    } finally {
        document.close();
    }


}

public void openPDF(String filePath) {
    try {
        File pdfFile = new File(filePath);
        if (pdfFile.exists()) {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(pdfFile);
            } else {
                JOptionPane.showMessageDialog(this, "Desktop tidak didukung. Tidak bisa membuka PDF.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "File PDF tidak ditemukan di: " + filePath);
        }
    } catch (IOException e) {
        JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat membuka PDF: " + e.getMessage());
    }
}
private void LoadDataPemasukan() {
    tabel_m_dapat.setRowCount(0); 
    String sql = "SELECT id_dapat, jumlah_dapat, tanggal_dapat, keterangan_dapat, total_dapat FROM td_pemasukan";
    try {
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            tabel_m_dapat.addRow(new Object[]{
                rs.getInt("id_dapat"),
                rs.getInt("jumlah_dapat"),
                rs.getDate("tanggal_dapat"),
                rs.getString("keterangan_dapat"),
                rs.getInt("total_dapat")
            });
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error loading pemasukan data: " + e.getMessage());
    }
}


private void LoadDataPengeluaran() {
    tabel_m_keluar.setRowCount(0); 
    String sql = "SELECT id_keluar, jumlah_keluar, tanggal_keluar, keterangan_keluar FROM td_pengeluaran";
    try {
        PreparedStatement dp = conn.prepareStatement(sql);
        ResultSet rs = dp.executeQuery();
        while (rs.next()) {
            java.sql.Date sqlDate = rs.getDate("tanggal_keluar");
            String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(sqlDate);

            tabel_m_keluar.addRow(new Object[]{
                rs.getInt("id_keluar"),
                rs.getInt("jumlah_keluar"),
                formattedDate,  
                rs.getString("keterangan_keluar")
            });
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error loading pengeluaran data: " + e.getMessage());
    }
}


private void SaveDataPemasukan() {
    try {
        if (djumlah.getText().isEmpty() || !djumlah.getText().matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Jumlah tidak valid. Masukkan angka.");
            return;
        }
        if (dtanggal.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Tanggal tidak boleh kosong.");
            return;
        }

        int jumlahPendapatan = Integer.parseInt(djumlah.getText());
        String formattedTanggal = new SimpleDateFormat("yyyy-MM-dd").format(dtanggal.getDate());
        String bulanPendapatan = new SimpleDateFormat("MMMM yyyy").format(dtanggal.getDate());
        String keterangan = dketerangan.getText();

        int totalPendapatanBaru = jumlahPendapatan;

        String sqlCekBulan = """
            SELECT SUM(jumlah_dapat) AS total_sebelumnya
            FROM td_pemasukan
            WHERE bulan_dapat = ?
        """;
        PreparedStatement psCek = conn.prepareStatement(sqlCekBulan);
        psCek.setString(1, bulanPendapatan);
        ResultSet rsCek = psCek.executeQuery();

        if (rsCek.next() && rsCek.getInt("total_sebelumnya") > 0) {
            int totalSebelumnya = rsCek.getInt("total_sebelumnya");
            totalPendapatanBaru += totalSebelumnya;
        }

        String sqlInsert = """
            INSERT INTO td_pemasukan (jumlah_dapat, tanggal_dapat, keterangan_dapat, bulan_dapat, total_dapat)
            VALUES (?, ?, ?, ?, ?)
        """;
        PreparedStatement psInsert = conn.prepareStatement(sqlInsert);
        psInsert.setInt(1, jumlahPendapatan);        
        psInsert.setString(2, formattedTanggal);     
        psInsert.setString(3, keterangan);           
        psInsert.setString(4, bulanPendapatan);      
        psInsert.setInt(5, totalPendapatanBaru);      
        psInsert.executeUpdate();

        LoadDataPemasukan();
        showDiagramOnPanel();

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error saat menyimpan pemasukan: " + e.getMessage());
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Format jumlah tidak valid. Masukkan angka.");
    }

    djumlah.setText("");
    dtanggal.setDate(null);
    dketerangan.setText("");
}



private void SaveDataPengeluaran() {
    try {
        String sql = "INSERT INTO td_pengeluaran (jumlah_keluar, tanggal_keluar, keterangan_keluar) VALUES (?, ?, ?)";
        PreparedStatement psht = conn.prepareStatement(sql);
        psht.setInt(1, Integer.parseInt(kjumlah.getText()));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = dateFormat.format(ktanggal.getDate());
        psht.setString(2, formattedDate);
        psht.setString(3, kketerangan.getText());
        psht.executeUpdate();

        JOptionPane.showMessageDialog(this, "Data pengeluaran berhasil disimpan.");

        LoadDataPengeluaran(); 
        LoadPengeluaranIDs(); 
        showDiagramOnPanel();
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error Save Data: " + e.getMessage());
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Format jumlah tidak valid.");
    }
}

private void saveToTdTotal(String bulanDipilih, String kategoriDipilih, int totalPendapatan, int totalPengeluaran, int sisaSaldo) {
    try {
        String tanggalSaatIni = new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());

        String sqlCheck = "SELECT COUNT(*) AS jumlah FROM td_total WHERE bulan_d_total = ? AND keterangan_k_total = ?";
        PreparedStatement psCheck = conn.prepareStatement(sqlCheck);
        psCheck.setString(1, bulanDipilih);
        psCheck.setString(2, kategoriDipilih);
        ResultSet rsCheck = psCheck.executeQuery();

        int jumlah = 0;
        if (rsCheck.next()) {
            jumlah = rsCheck.getInt("jumlah");
        }

        if (jumlah == 0) {
            String sqlInsertTotal = """
                INSERT INTO td_total (jumlah_d_total, bulan_d_total, jumlah_k_total, sisa_saldo, keterangan_k_total, tanggal_k_total)
                VALUES (?, ?, ?, ?, ?, ?)
            """;
            PreparedStatement psInsertTotal = conn.prepareStatement(sqlInsertTotal);
            psInsertTotal.setInt(1, totalPendapatan);      
            psInsertTotal.setString(2, bulanDipilih);      
            psInsertTotal.setInt(3, totalPengeluaran);     
            psInsertTotal.setInt(4, sisaSaldo);            
            psInsertTotal.setString(5, kategoriDipilih);   
            psInsertTotal.setString(6, tanggalSaatIni);    
            psInsertTotal.executeUpdate();

            JOptionPane.showMessageDialog(this, "Data berhasil disimpan ke tabel td_total.");
        } else {
            System.out.println("Data sudah ada, tidak perlu disimpan lagi.");
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Terjadi kesalahan: " + e.getMessage());
    }
}



private void UpdateDataPengeluaran(int idKeluar, String jumlah, java.util.Date tanggal, String keterangan) {
    try {
        String sql = "UPDATE td_pengeluaran SET jumlah_keluar = ?, tanggal_keluar = ?, keterangan_keluar = ? WHERE id_keluar = ?";
        PreparedStatement psht = conn.prepareStatement(sql);
        psht.setInt(1, Integer.parseInt(jumlah)); 
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = dateFormat.format(tanggal);
        psht.setString(2, formattedDate);
        psht.setString(3, keterangan);
        psht.setInt(4, idKeluar); 
        psht.executeUpdate();

        String sqlTotalPengeluaran = "SELECT SUM(jumlah_keluar) AS total_pengeluaran FROM td_pengeluaran";
        PreparedStatement psTotal = conn.prepareStatement(sqlTotalPengeluaran);
        ResultSet rsTotal = psTotal.executeQuery();

        int totalPengeluaran = 0;
        if (rsTotal.next()) {
            totalPengeluaran = rsTotal.getInt("total_pengeluaran");
        }


        JOptionPane.showMessageDialog(this, "Data pengeluaran berhasil diperbarui.");

        LoadDataPengeluaran(); 
        LoadPengeluaranIDs(); 
        showDiagramOnPanel();  

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error Update Data: " + e.getMessage());
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Format jumlah tidak valid.");
    }
}

private void updateTdTotal(String bulanDipilih, String keteranganDipilih) {
    try {
        String sqlUpdateTotal = """
            UPDATE td_total t
            LEFT JOIN (
                SELECT 
                    SUM(jumlah_keluar) AS total_pengeluaran,
                    MAX(tanggal_keluar) AS tanggal_terakhir,
                    keterangan_keluar
                FROM 
                    td_pengeluaran
                WHERE 
                    keterangan_keluar = ? 
                GROUP BY 
                    keterangan_keluar
            ) p ON t.keterangan_k_total = p.keterangan_keluar
            SET 
                t.jumlah_k_total = p.total_pengeluaran,
                t.tanggal_k_total = p.tanggal_terakhir,
                t.keterangan_k_total = p.keterangan_keluar
            WHERE 
                t.bulan_d_total = ? AND t.keterangan_k_total = ?;
        """;

        PreparedStatement psUpdate = conn.prepareStatement(sqlUpdateTotal);
        psUpdate.setString(1, keteranganDipilih); 
        psUpdate.setString(2, bulanDipilih);     
        psUpdate.setString(3, keteranganDipilih); 
        psUpdate.executeUpdate();

        JOptionPane.showMessageDialog(this, "Data td_total berhasil diperbarui untuk keterangan: " + keteranganDipilih);
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error saat memperbarui td_total: " + e.getMessage());
    }
}




private void updateATotal() {
    try {
        String sql = "SELECT SUM(jumlah_dapat) AS total_pendapatan FROM td_pemasukan";
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        int totalPendapatan = 0;
        if (rs.next()) {
            totalPendapatan = rs.getInt("total_pendapatan");
        }


    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error mengambil total pendapatan: " + e.getMessage());
    }
}


private void LoadPemasukanIDs() {
    adapat.removeAllItems(); 
    String sql = "SELECT DISTINCT bulan_dapat FROM td_pemasukan WHERE bulan_dapat IS NOT NULL";

    try {
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            String bulanTahun = rs.getString("bulan_dapat");
            if (bulanTahun != null && !bulanTahun.isEmpty()) {
                adapat.addItem(bulanTahun);
            }
        }

        if (adapat.getItemCount() == 0) {
            adapat.addItem("Tidak ada data");
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error memuat data bulan pemasukan: " + e.getMessage());
    }
}


private void LoadPengeluaranIDs() {
    akeluar.removeAllItems(); 
    String sql = "SELECT DISTINCT keterangan_keluar FROM td_pengeluaran";
    try {
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            String keterangan = rs.getString("keterangan_keluar"); 
            akeluar.addItem(keterangan); 
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error Load Pengeluaran: " + e.getMessage());
    }
}




protected void datatablek() {
    Object[] Baris = {"ID Keluar", "Jumlah Keluar", "Tanggal Keluar", "Keterangan Keluar"};
    tabel_m_keluar = new DefaultTableModel(null, Baris);
    String cariitem = kcari.getText(); 
    try {
        if (conn == null || conn.isClosed()) {
            JOptionPane.showMessageDialog(null, "Koneksi ke database tidak tersedia.");
            return;
        }

        String sql = "SELECT * FROM td_pengeluaran WHERE id_keluar LIKE ? OR keterangan_keluar LIKE ? ORDER BY id_keluar ASC";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, "%" + cariitem + "%");
        ps.setString(2, "%" + cariitem + "%");

        ResultSet hasil = ps.executeQuery();
        while (hasil.next()) {
            tabel_m_keluar.addRow(new Object[]{
                hasil.getString("id_keluar"),
                hasil.getString("jumlah_keluar"),
                hasil.getString("tanggal_keluar"),
                hasil.getString("keterangan_keluar"),
            });
        }
        ktabel.setModel(tabel_m_keluar);
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Data gagal dipanggil: " + e.getMessage());
    }
}
protected void datatabled() {
    Object[] Baris = {"ID Pendapatan", "Jumlah Pendapatan", "Tanggal Pendapatan", "Keterangan Pendapatan"};
    tabel_m_dapat = new DefaultTableModel(null, Baris);
    String cariitem = dcari.getText(); 
    try {
        if (conn == null || conn.isClosed()) {
            JOptionPane.showMessageDialog(null, "Koneksi ke database tidak tersedia.");
            return;
        }

        String sql = "SELECT * FROM td_pemasukan WHERE id_dapat LIKE ? OR keterangan_dapat LIKE ? ORDER BY id_dapat ASC";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, "%" + cariitem + "%");
        ps.setString(2, "%" + cariitem + "%");

        ResultSet hasil = ps.executeQuery();
        while (hasil.next()) {
            tabel_m_dapat.addRow(new Object[]{
                hasil.getString("id_dapat"),
                hasil.getString("jumlah_dapat"),
                hasil.getString("tanggal_dapat"),
                hasil.getString("keterangan_dapat"),
            });
        }
        dtabel.setModel(tabel_m_dapat);
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Data gagal dipanggil: " + e.getMessage());
    }
}


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jRadioButtonMenuItem1 = new javax.swing.JRadioButtonMenuItem();
        jCalendar1 = new com.toedter.calendar.JCalendar();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel9 = new javax.swing.JLabel();
        atas = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        PENDAPATAN = new javax.swing.JPanel();
        background1 = new Login.Background();
        TABELD = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        dtabel = new javax.swing.JTable();
        dcari = new javax.swing.JTextField();
        bdcari = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        dcetak = new javax.swing.JButton();
        preset = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        dketerangan = new swing.TextField();
        jLabel4 = new javax.swing.JLabel();
        dtanggal = new com.toedter.calendar.JDateChooser();
        jLabel2 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        dkeluar = new swing.Button();
        dsimpan = new swing.Button();
        djumlah = new swing.TextField();
        PENGELUARAN = new javax.swing.JPanel();
        listpengeluaran = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        ktabel = new javax.swing.JTable();
        kcari = new javax.swing.JTextField();
        jButton7 = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        kcetak = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        background2 = new Login.Background();
        ksimpan = new swing.Button();
        kubah = new swing.Button();
        khapus = new swing.Button();
        kakeluar = new swing.Button();
        kketerangan = new swing.TextField();
        kjumlah = new swing.TextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        ktanggal = new com.toedter.calendar.JDateChooser();
        jLabel11 = new javax.swing.JLabel();
        ANALISIS = new javax.swing.JPanel();
        statik = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        background3 = new Login.Background();
        jLabel8 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        adapat = new javax.swing.JComboBox<>();
        akeluar = new javax.swing.JComboBox<>();
        alapor = new swing.Button();
        ankeluar = new swing.Button();

        jRadioButtonMenuItem1.setSelected(true);
        jRadioButtonMenuItem1.setText("jRadioButtonMenuItem1");

        jLabel9.setText("jLabel9");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);

        atas.setMinimumSize(new java.awt.Dimension(962, 623));

        jTabbedPane1.setPreferredSize(new java.awt.Dimension(600, 655));

        PENDAPATAN.setBackground(new java.awt.Color(255, 255, 255));
        PENDAPATAN.setForeground(new java.awt.Color(255, 255, 255));
        PENDAPATAN.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        background1.setBackground(new java.awt.Color(255, 255, 255));
        background1.setBlur(TABELD);
        background1.setOpaque(true);

        TABELD.setBackground(new java.awt.Color(0, 204, 204));
        TABELD.setOpaque(false);

        dtabel.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        dtabel.setOpaque(false);
        dtabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dtabelMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(dtabel);

        dcari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                dcariKeyPressed(evt);
            }
        });

        bdcari.setText("Cari");
        bdcari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bdcariActionPerformed(evt);
            }
        });

        jLabel7.setBackground(new java.awt.Color(255, 255, 255));
        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("LIST PEMASUKAN ");

        dcetak.setText("Cetak PDF");
        dcetak.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dcetakActionPerformed(evt);
            }
        });

        preset.setText("Reset");
        preset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                presetActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout TABELDLayout = new javax.swing.GroupLayout(TABELD);
        TABELD.setLayout(TABELDLayout);
        TABELDLayout.setHorizontalGroup(
            TABELDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TABELDLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(TABELDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(TABELDLayout.createSequentialGroup()
                        .addComponent(dcari, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(bdcari)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(preset, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(dcetak))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 597, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(21, Short.MAX_VALUE))
        );
        TABELDLayout.setVerticalGroup(
            TABELDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TABELDLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(TABELDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dcari, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(TABELDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(bdcari)
                        .addComponent(jLabel7)
                        .addComponent(dcetak)
                        .addComponent(preset)))
                .addGap(12, 12, 12)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        background1.add(TABELD);
        TABELD.setBounds(170, 180, 640, 420);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("TANGGAL");
        background1.add(jLabel1);
        jLabel1.setBounds(360, 70, 60, 16);

        dketerangan.setForeground(new java.awt.Color(0, 255, 255));
        background1.add(dketerangan);
        dketerangan.setBounds(730, 60, 130, 34);

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("KETERANGAN");
        background1.add(jLabel4);
        jLabel4.setBounds(630, 70, 90, 16);
        background1.add(dtanggal);
        dtanggal.setBounds(440, 70, 160, 22);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("JUMLAH");
        background1.add(jLabel2);
        jLabel2.setBounds(100, 70, 60, 16);

        jLabel5.setBackground(new java.awt.Color(0, 0, 0));
        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("PENCATATAN PEMASUKAN");
        background1.add(jLabel5);
        jLabel5.setBounds(340, 10, 320, 30);

        dkeluar.setForeground(new java.awt.Color(255, 255, 255));
        dkeluar.setText("KELUAR");
        dkeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dkeluarActionPerformed(evt);
            }
        });
        background1.add(dkeluar);
        dkeluar.setBounds(640, 120, 90, 32);

        dsimpan.setForeground(new java.awt.Color(255, 255, 255));
        dsimpan.setText("SIMPAN");
        dsimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dsimpanActionPerformed(evt);
            }
        });
        background1.add(dsimpan);
        dsimpan.setBounds(270, 120, 80, 32);
        background1.add(djumlah);
        djumlah.setBounds(170, 60, 160, 34);

        PENDAPATAN.add(background1, new org.netbeans.lib.awtextra.AbsoluteConstraints(-10, 0, 980, 620));

        jTabbedPane1.addTab("PEMASUKAN", PENDAPATAN);

        PENGELUARAN.setBackground(new java.awt.Color(102, 204, 255));
        PENGELUARAN.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        listpengeluaran.setBackground(new java.awt.Color(204, 153, 255));
        listpengeluaran.setOpaque(false);

        ktabel.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        ktabel.setOpaque(false);
        ktabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ktabelMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(ktabel);

        kcari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kcariKeyPressed(evt);
            }
        });

        jButton7.setText("Cari");

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(255, 255, 255));
        jLabel12.setText("LIST PENGELUARAN");

        kcetak.setText("Cetak PDF");
        kcetak.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kcetakActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout listpengeluaranLayout = new javax.swing.GroupLayout(listpengeluaran);
        listpengeluaran.setLayout(listpengeluaranLayout);
        listpengeluaranLayout.setHorizontalGroup(
            listpengeluaranLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(listpengeluaranLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(listpengeluaranLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 597, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(listpengeluaranLayout.createSequentialGroup()
                        .addComponent(kcari, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel12)
                        .addGap(66, 66, 66)
                        .addComponent(kcetak)))
                .addGap(0, 0, 0))
        );
        listpengeluaranLayout.setVerticalGroup(
            listpengeluaranLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, listpengeluaranLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(listpengeluaranLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, listpengeluaranLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(kcari, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton7))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, listpengeluaranLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel12)
                        .addComponent(kcetak)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21))
        );

        PENGELUARAN.add(listpengeluaran, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 190, 670, -1));

        jLabel13.setBackground(new java.awt.Color(255, 255, 255));
        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(255, 255, 255));
        jLabel13.setText("PENCATATAN PENGELUARAN");
        PENGELUARAN.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 10, 320, -1));

        background2.setBlur(listpengeluaran);

        ksimpan.setForeground(new java.awt.Color(255, 255, 255));
        ksimpan.setText("SIMPAN");
        ksimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ksimpanActionPerformed(evt);
            }
        });
        background2.add(ksimpan);
        ksimpan.setBounds(190, 140, 70, 32);

        kubah.setForeground(new java.awt.Color(255, 255, 255));
        kubah.setText("UBAH");
        kubah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kubahActionPerformed(evt);
            }
        });
        background2.add(kubah);
        kubah.setBounds(280, 140, 70, 32);

        khapus.setForeground(new java.awt.Color(255, 255, 255));
        khapus.setText("HAPUS");
        khapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                khapusActionPerformed(evt);
            }
        });
        background2.add(khapus);
        khapus.setBounds(620, 140, 70, 32);

        kakeluar.setForeground(new java.awt.Color(255, 255, 255));
        kakeluar.setText("KELUAR");
        kakeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kakeluarActionPerformed(evt);
            }
        });
        background2.add(kakeluar);
        kakeluar.setBounds(712, 142, 80, 30);
        background2.add(kketerangan);
        kketerangan.setBounds(710, 70, 110, 34);
        background2.add(kjumlah);
        kjumlah.setBounds(230, 70, 130, 34);

        jLabel6.setBackground(new java.awt.Color(0, 0, 0));
        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("JUMLAH");
        background2.add(jLabel6);
        jLabel6.setBounds(160, 80, 60, 20);

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("TANGGAL");
        background2.add(jLabel3);
        jLabel3.setBounds(370, 80, 60, 16);
        background2.add(ktanggal);
        ktanggal.setBounds(450, 80, 160, 22);

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setText("KETERANGAN");
        background2.add(jLabel11);
        jLabel11.setBounds(620, 80, 90, 16);

        PENGELUARAN.add(background2, new org.netbeans.lib.awtextra.AbsoluteConstraints(-10, 0, 990, 630));

        jTabbedPane1.addTab("PENGELUARAN", PENGELUARAN);

        ANALISIS.setBackground(new java.awt.Color(0, 204, 204));
        ANALISIS.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        statik.setBackground(new java.awt.Color(153, 153, 153));
        statik.setOpaque(false);

        javax.swing.GroupLayout statikLayout = new javax.swing.GroupLayout(statik);
        statik.setLayout(statikLayout);
        statikLayout.setHorizontalGroup(
            statikLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 820, Short.MAX_VALUE)
        );
        statikLayout.setVerticalGroup(
            statikLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 383, Short.MAX_VALUE)
        );

        ANALISIS.add(statik, new org.netbeans.lib.awtextra.AbsoluteConstraints(89, 209, 820, -1));

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(255, 255, 255));
        jLabel14.setText("ANALISA PADA PENDAPATAN DAN PENGELUARAN");
        ANALISIS.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(236, 29, -1, -1));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("PENGELUARAN");
        background3.add(jLabel8);
        jLabel8.setBounds(550, 100, 100, 16);

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setText("PENDAPATAN");
        background3.add(jLabel10);
        jLabel10.setBounds(180, 100, 90, 16);

        adapat.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        adapat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adapatActionPerformed(evt);
            }
        });
        background3.add(adapat);
        adapat.setBounds(280, 100, 175, 22);

        akeluar.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        background3.add(akeluar);
        akeluar.setBounds(650, 100, 210, 22);

        alapor.setForeground(new java.awt.Color(255, 255, 255));
        alapor.setText("LIHAT LAPORAN");
        alapor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                alaporActionPerformed(evt);
            }
        });
        background3.add(alapor);
        alapor.setBounds(360, 160, 100, 32);

        ankeluar.setForeground(new java.awt.Color(255, 255, 255));
        ankeluar.setText("KELUAR");
        ankeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ankeluarActionPerformed(evt);
            }
        });
        background3.add(ankeluar);
        ankeluar.setBounds(560, 160, 100, 32);

        ANALISIS.add(background3, new org.netbeans.lib.awtextra.AbsoluteConstraints(-10, 0, 990, 630));

        jTabbedPane1.addTab("ANALISIS", ANALISIS);

        javax.swing.GroupLayout atasLayout = new javax.swing.GroupLayout(atas);
        atas.setLayout(atasLayout);
        atasLayout.setHorizontalGroup(
            atasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 980, Short.MAX_VALUE)
        );
        atasLayout.setVerticalGroup(
            atasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(atasLayout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        getContentPane().add(atas, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void kcetakActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_kcetakActionPerformed
        String outputPath = "data pengeluaran.pdf";
        exportToPDF(tabel_m_keluar, "Data Pengeluaran.pdf", "Data Pengeluaran");
        openPDF(outputPath);
    }//GEN-LAST:event_kcetakActionPerformed

    private void ktabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ktabelMouseClicked

    int bar = ktabel.getSelectedRow(); 
    String jumlah = tabel_m_keluar.getValueAt(bar, 1).toString();
    String tanggal = tabel_m_keluar.getValueAt(bar, 2).toString();
    String keterangan = tabel_m_keluar.getValueAt(bar, 3).toString();

    kjumlah.setText(jumlah);

    try {
        java.util.Date date = new SimpleDateFormat("yyyy-MM-dd").parse(tanggal);
        ktanggal.setDate(date); 
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error parsing date: " + e.getMessage());
    }

    kketerangan.setText(keterangan);


    }//GEN-LAST:event_ktabelMouseClicked

    private void kcariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kcariKeyPressed
            if(evt.getKeyCode() == KeyEvent.VK_ENTER){
            datatablek();
        }
    }//GEN-LAST:event_kcariKeyPressed

    private void dcetakActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dcetakActionPerformed
        String outputPath = "data pemasukan.pdf";
        exportToPDF(tabel_m_dapat, "Data Pemasukan.pdf", "Data Pemasukan");
        openPDF(outputPath);
    }//GEN-LAST:event_dcetakActionPerformed

    private void bdcariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bdcariActionPerformed

    }//GEN-LAST:event_bdcariActionPerformed

    private void dcariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_dcariKeyPressed
        if(evt.getKeyCode() == KeyEvent.VK_ENTER){
            datatabled();
        }
    }//GEN-LAST:event_dcariKeyPressed

    private void dtabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dtabelMouseClicked

    }//GEN-LAST:event_dtabelMouseClicked

    private void adapatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adapatActionPerformed
    }//GEN-LAST:event_adapatActionPerformed

    private void dkeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dkeluarActionPerformed
        int result = JOptionPane.showConfirmDialog(this,
            "Keluar Program",
            "Keluar",
            JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }//GEN-LAST:event_dkeluarActionPerformed

    private void dsimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dsimpanActionPerformed
if (djumlah.getText().isEmpty()) { 
    JOptionPane.showMessageDialog(this, "Jumlah tidak boleh kosong.", "Input Error", JOptionPane.ERROR_MESSAGE);
    return;
}

if (!djumlah.getText().matches("\\d+")) { 
    JOptionPane.showMessageDialog(this, "Jumlah hanya boleh berisi angka.", "Input Error", JOptionPane.ERROR_MESSAGE);
    return;
}

if (dtanggal.getDate() == null) {
    JOptionPane.showMessageDialog(this, "Tanggal tidak boleh kosong.", "Input Error", JOptionPane.ERROR_MESSAGE);
    return;
}

if (dketerangan.getText().trim().isEmpty()) { 
    JOptionPane.showMessageDialog(this, "Keterangan tidak boleh kosong.", "Input Error", JOptionPane.ERROR_MESSAGE);
    return;
}

try {
    int jumlahPendapatan = Integer.parseInt(djumlah.getText());
    java.util.Date tanggalPendapatan = dtanggal.getDate();
    String keterangan = dketerangan.getText().trim(); 

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String formattedTanggal = dateFormat.format(tanggalPendapatan);

    SimpleDateFormat bulanFormat = new SimpleDateFormat("MMMM yyyy");
    String bulanPendapatan = bulanFormat.format(tanggalPendapatan);

    SaveDataPemasukan();  

    LoadPemasukanIDs();
    updateATotal();

    JOptionPane.showMessageDialog(this, "Data pemasukan berhasil disimpan.");
    LoadDataPemasukan(); 
    showDiagramOnPanel(); 

} catch (NumberFormatException e) {
    JOptionPane.showMessageDialog(this, "Format jumlah tidak valid. Masukkan angka.");
}

djumlah.setText("");
dtanggal.setDate(null);
dketerangan.setText("");
    }//GEN-LAST:event_dsimpanActionPerformed

    private void ksimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ksimpanActionPerformed
    if (!kjumlah.getText().matches("\\d+")) { 
        JOptionPane.showMessageDialog(this, "Jumlah hanya boleh berisi angka, tidak boleh berisi simbol.", "Input Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    try {
        SaveDataPengeluaran(); 
        showDiagramOnPanel(); 

        String bulanDipilih = new SimpleDateFormat("MMMM yyyy").format(ktanggal.getDate());
        String keteranganDipilih = kketerangan.getText();
        updateTdTotal(bulanDipilih, keteranganDipilih); 

        kjumlah.setText("");
        ktanggal.setDate(null);
        kketerangan.setText("");

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error saat menyimpan data pengeluaran: " + e.getMessage());
    }

    }//GEN-LAST:event_ksimpanActionPerformed

    private void kubahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_kubahActionPerformed
    int selectedRow = ktabel.getSelectedRow();
    if (selectedRow == -1) { 
        JOptionPane.showMessageDialog(this, "Harap pilih data pengeluaran dari tabel.", "Error", JOptionPane.WARNING_MESSAGE);
        return;
    }

    if (!kjumlah.getText().matches("\\d+")) { 
        JOptionPane.showMessageDialog(this, "Jumlah hanya boleh berisi angka.", "Input Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    try {
        int idKeluar = Integer.parseInt(ktabel.getValueAt(selectedRow, 0).toString());
        String jumlah = kjumlah.getText();
        String keterangan = kketerangan.getText();
        java.util.Date tanggal = ktanggal.getDate();

        if (tanggal == null) {
            JOptionPane.showMessageDialog(this, "Harap pilih tanggal.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        UpdateDataPengeluaran(idKeluar, jumlah, tanggal, keterangan);

        String bulanDipilih = new SimpleDateFormat("MMMM yyyy").format(tanggal);
        String keteranganDipilih = keterangan;
        updateTdTotal(bulanDipilih, keteranganDipilih);

        showDiagramOnPanel();

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Terjadi kesalahan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }


    }//GEN-LAST:event_kubahActionPerformed

    private void khapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_khapusActionPerformed
    int selectedRow = ktabel.getSelectedRow();
    if (selectedRow == -1) { 
        JOptionPane.showMessageDialog(this, "Harap pilih data pengeluaran yang ingin dihapus.");
        return;
    }

    int confirm = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin menghapus data ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
    if (confirm == JOptionPane.YES_OPTION) {
        try {
            int idKeluar = Integer.parseInt(ktabel.getValueAt(selectedRow, 0).toString());
            String keteranganDipilih = ktabel.getValueAt(selectedRow, 3).toString(); // Ambil keterangan_keluar
            String bulanDipilih = new SimpleDateFormat("MMMM yyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(ktabel.getValueAt(selectedRow, 2).toString()));

            String sqlDelete = "DELETE FROM td_pengeluaran WHERE id_keluar = ?";
            PreparedStatement ps = conn.prepareStatement(sqlDelete);
            ps.setInt(1, idKeluar);
            ps.executeUpdate();

            updateTdTotal(bulanDipilih, keteranganDipilih);

            LoadDataPengeluaran();
            showDiagramOnPanel();

            JOptionPane.showMessageDialog(this, "Data pengeluaran berhasil dihapus.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat menghapus data: " + e.getMessage());
        }
    }
    }//GEN-LAST:event_khapusActionPerformed

    private void kakeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_kakeluarActionPerformed
        int result = JOptionPane.showConfirmDialog(this,
            "Keluar Program",
            "Keluar",
            JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }//GEN-LAST:event_kakeluarActionPerformed

    private void alaporActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_alaporActionPerformed
    try {
        if (adapat.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Harap pilih bulan pemasukan terlebih dahulu.");
            return;
        }
        if (akeluar.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Harap pilih kategori pengeluaran terlebih dahulu.");
            return;
        }

        String bulanDipilih = adapat.getSelectedItem().toString(); 
        String kategoriDipilih = akeluar.getSelectedItem().toString(); 

        String sqlPendapatan = "SELECT SUM(jumlah_dapat) AS total_pendapatan FROM td_pemasukan WHERE bulan_dapat = ?";
        PreparedStatement psPendapatan = conn.prepareStatement(sqlPendapatan);
        psPendapatan.setString(1, bulanDipilih);
        ResultSet rsPendapatan = psPendapatan.executeQuery();

        int totalPendapatan = 0;
        if (rsPendapatan.next()) {
            totalPendapatan = rsPendapatan.getInt("total_pendapatan");
        }

        String sqlPengeluaran = """
            SELECT SUM(jumlah_keluar) AS total_pengeluaran 
            FROM td_pengeluaran 
            WHERE keterangan_keluar = ?
        """;
        PreparedStatement psPengeluaran = conn.prepareStatement(sqlPengeluaran);
        psPengeluaran.setString(1, kategoriDipilih);
        ResultSet rsPengeluaran = psPengeluaran.executeQuery();

        int totalPengeluaran = 0;
        if (rsPengeluaran.next()) {
            totalPengeluaran = rsPengeluaran.getInt("total_pengeluaran");
        }

        int sisaSaldo = totalPendapatan - totalPengeluaran;


        saveToTdTotal(bulanDipilih, kategoriDipilih, totalPendapatan, totalPengeluaran, sisaSaldo);

        updateTdTotal(bulanDipilih, kategoriDipilih); 

        if (tugas2Instance == null) {
            tugas2Instance = new Tugas2(this); 
        }

        tugas2Instance.LoadDataTotal();
        tugas2Instance.setVisible(true); 
        this.setVisible(false); 
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Terjadi kesalahan pada database: " + e.getMessage());
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Kesalahan tidak terduga: " + e.getMessage());
    }

 
    }//GEN-LAST:event_alaporActionPerformed

    private void ankeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ankeluarActionPerformed
        // TODO add your handling code here:
        int result = JOptionPane.showConfirmDialog(this,
            "Keluar Program",
            "Keluar",
            JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }//GEN-LAST:event_ankeluarActionPerformed

    private void presetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_presetActionPerformed
    DefaultTableModel model = (DefaultTableModel) dtabel.getModel();
    model.setRowCount(0); 
    JOptionPane.showMessageDialog(this, "Tabel berhasil direset.");
    }//GEN-LAST:event_presetActionPerformed



    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Tugas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Tugas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Tugas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Tugas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Tugas().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ANALISIS;
    private javax.swing.JPanel PENDAPATAN;
    private javax.swing.JPanel PENGELUARAN;
    private javax.swing.JPanel TABELD;
    private javax.swing.JComboBox<String> adapat;
    private javax.swing.JComboBox<String> akeluar;
    private swing.Button alapor;
    private swing.Button ankeluar;
    private javax.swing.JPanel atas;
    private Login.Background background1;
    private Login.Background background2;
    private Login.Background background3;
    private javax.swing.JButton bdcari;
    private javax.swing.JTextField dcari;
    private javax.swing.JButton dcetak;
    private swing.TextField djumlah;
    private swing.Button dkeluar;
    private swing.TextField dketerangan;
    private swing.Button dsimpan;
    private javax.swing.JTable dtabel;
    private com.toedter.calendar.JDateChooser dtanggal;
    private javax.swing.JButton jButton7;
    private com.toedter.calendar.JCalendar jCalendar1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private swing.Button kakeluar;
    private javax.swing.JTextField kcari;
    private javax.swing.JButton kcetak;
    private swing.Button khapus;
    private swing.TextField kjumlah;
    private swing.TextField kketerangan;
    private swing.Button ksimpan;
    private javax.swing.JTable ktabel;
    private com.toedter.calendar.JDateChooser ktanggal;
    private swing.Button kubah;
    private javax.swing.JPanel listpengeluaran;
    private javax.swing.JButton preset;
    private javax.swing.JPanel statik;
    // End of variables declaration//GEN-END:variables
}
