/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package project.akhir;
import com.itextpdf.text.BaseColor;
import java.awt.Color;
import javax.swing.JFrame;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import javax.swing.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Desktop;
import java.io.IOException;
import com.itextpdf.text.PageSize;

public class Tugas2 extends javax.swing.JFrame {
    private Connection conn;
    private DefaultTableModel tabel_m_hasil;
    private Tugas frameTugas; 

    public Tugas2(Tugas frameTugas) {
    initComponents();
    this.frameTugas = frameTugas; 
    setLocationRelativeTo(null);

    conn = koneksi.getConnection(); 
    tabel_m_hasil = new DefaultTableModel();

        tabel_m_hasil.addColumn("ID Total");
        tabel_m_hasil.addColumn("Jumlah Pendapatan");
        tabel_m_hasil.addColumn("Bulan Pemasukan");
        tabel_m_hasil.addColumn("Jumlah Pengeluaran");
        tabel_m_hasil.addColumn("Tanggal Pengeluaran");
        tabel_m_hasil.addColumn("Keterangan Pengeluaran");
        tabel_m_hasil.addColumn("Sisa Saldo");

    khasil.setModel(tabel_m_hasil);
    LoadDataTotal(); 
    }

public Tugas2() {
    this(null); 
}
    public void updateDataTotal() {
        LoadDataTotal();
    }
public void LoadDataTotal() {
    tabel_m_hasil.setRowCount(0);

    try {
        String sql = """
            SELECT 
                t.id_total, 
                t.jumlah_d_total, 
                t.bulan_d_total, 
                t.jumlah_k_total, 
                t.tanggal_k_total, 
                t.keterangan_k_total, 
                (t.jumlah_d_total - COALESCE(t.jumlah_k_total, 0)) AS sisa_saldo
            FROM 
                td_total t;
        """;

        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            tabel_m_hasil.addRow(new Object[]{
                rs.getInt("id_total"),
                rs.getInt("jumlah_d_total"),
                rs.getString("bulan_d_total"),
                rs.getInt("jumlah_k_total"),
                rs.getDate("tanggal_k_total"),
                rs.getString("keterangan_k_total"),
                rs.getInt("sisa_saldo")
            });
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error memuat data total: " + e.getMessage());
    }
}



public void tampilkanDataKeTabel(int idTotal, String bulanPendapatan, int totalPendapatan, 
                                 String kategoriPengeluaran, int totalPengeluaran, 
                                 String tanggalPengeluaran, int sisaSaldo) {
    tabel_m_hasil.addRow(new Object[] {
        idTotal,               
        totalPendapatan,      
        bulanPendapatan,       
        totalPengeluaran,    
        tanggalPengeluaran,    
        kategoriPengeluaran,   
        sisaSaldo             
    });
}

    private void exportToPDF(DefaultTableModel tableModel, String filePath, String title) {
        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            document.add(new Paragraph(title, titleFont));
            document.add(new Paragraph(" "));

            PdfPTable pdfTable = new PdfPTable(tableModel.getColumnCount());

            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                PdfPCell headerCell = new PdfPCell(new Phrase(tableModel.getColumnName(i)));
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                headerCell.setBackgroundColor(BaseColor.YELLOW);
                pdfTable.addCell(headerCell);
            }

            for (int row = 0; row < tableModel.getRowCount(); row++) {
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    pdfTable.addCell(tableModel.getValueAt(row, col).toString());
                }
            }

            pdfTable.setWidthPercentage(100);
            document.add(pdfTable);

            JOptionPane.showMessageDialog(this, "PDF berhasil dibuat di: " + filePath);
        } catch (DocumentException | IOException e) {
            JOptionPane.showMessageDialog(this, "Error saat membuat PDF: " + e.getMessage());
        } finally {
            document.close();
        }
    }

    private void openPDF(String filePath) {
        try {
            File pdfFile = new File(filePath);
            if (pdfFile.exists()) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(pdfFile);
                } else {
                    JOptionPane.showMessageDialog(this, "Desktop tidak didukung.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "File PDF tidak ditemukan.");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error membuka PDF: " + e.getMessage());
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

        jPanel1 = new javax.swing.JPanel();
        background1 = new Login.Background();
        jLabel1 = new javax.swing.JLabel();
        kcetak = new swing.Button();
        kkembali = new swing.Button();
        jScrollPane1 = new javax.swing.JScrollPane();
        khasil = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);

        jPanel1.setBackground(new java.awt.Color(0, 255, 153));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("LAPORAN KEUANGAN");
        background1.add(jLabel1);
        jLabel1.setBounds(420, 10, 310, 38);

        kcetak.setForeground(new java.awt.Color(255, 255, 255));
        kcetak.setText("CETAK PDF");
        kcetak.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kcetakActionPerformed(evt);
            }
        });
        background1.add(kcetak);
        kcetak.setBounds(740, 90, 100, 32);

        kkembali.setForeground(new java.awt.Color(255, 255, 255));
        kkembali.setText("KEMBALI");
        kkembali.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kkembaliActionPerformed(evt);
            }
        });
        background1.add(kkembali);
        kkembali.setBounds(300, 90, 100, 32);

        khasil.setBackground(new java.awt.Color(204, 204, 255));
        khasil.setModel(new javax.swing.table.DefaultTableModel(
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
        khasil.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                khasilMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(khasil);

        background1.add(jScrollPane1);
        jScrollPane1.setBounds(40, 150, 1055, 290);

        jPanel1.add(background1, new org.netbeans.lib.awtextra.AbsoluteConstraints(-10, 0, 1140, 480));

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void khasilMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_khasilMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_khasilMouseClicked

    private void kcetakActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_kcetakActionPerformed
        // TODO add your handling code here:
    String outputPath = "laporan_keuangan.pdf"; 
    exportToPDF(tabel_m_hasil, outputPath, "Laporan Keuangan"); 
    openPDF(outputPath);
    }//GEN-LAST:event_kcetakActionPerformed

    private void kkembaliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_kkembaliActionPerformed
        // TODO add your handling code here:
    if (frameTugas != null) {
        frameTugas.kembaliDariTugas2(); 
    }
    this.dispose();
    }//GEN-LAST:event_kkembaliActionPerformed

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
            java.util.logging.Logger.getLogger(Tugas2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Tugas2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Tugas2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Tugas2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
    java.awt.EventQueue.invokeLater(new Runnable() {
        public void run() {
            Tugas tugas = new Tugas();
            tugas.setVisible(true);

            Tugas2 tugas2 = new Tugas2(tugas);
            tugas2.setVisible(true);
        }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private Login.Background background1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private swing.Button kcetak;
    private javax.swing.JTable khasil;
    private swing.Button kkembali;
    // End of variables declaration//GEN-END:variables
}
