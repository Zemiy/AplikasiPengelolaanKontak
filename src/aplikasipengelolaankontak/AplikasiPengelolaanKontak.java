package aplikasipengelolaankontak;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AplikasiPengelolaanKontak extends javax.swing.JFrame {
    private DefaultTableModel tableModel;
    public AplikasiPengelolaanKontak() {
        initComponents();
        // Inisialisasi model tabel
        tableModel = new DefaultTableModel(new String[]{"ID", "Nama", "Telepon", "Kategori"}, 0);
        // Menghubungkan model ke JTable
        kontakTable.setModel(tableModel);
        
        kontakTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = kontakTable.getSelectedRow();
            if (selectedRow != -1) { // Pastikan ada baris yang dipilih
                // Ambil data dari baris yang dipilih dan tampilkan di JTextField
                String nama = (String) tableModel.getValueAt(selectedRow, 1); // Ambil nama dari kolom ke-1
                String telepon = (String) tableModel.getValueAt(selectedRow, 2); // Ambil telepon dari kolom ke-2
                namaTextField.setText(nama); // Masukkan nama ke dalam JTextField
                teleponTextField.setText(telepon); // Masukkan nomor telepon ke dalam JTextField
            }
        });
    }
    
    public class DatabaseHelper {
    public Connection connect() {
        Connection conn = null;
        try{
            Class.forName("org.sqlite.JDBC");
            Connection con = DriverManager.getConnection("jdbc:sqlite:kontak.db");
            System.out.println("Connect Berhasil");
                     
            return con;
           }catch (Exception e){
            System.out.println("Connect Gagal"+e);
            return null;
           }
    }
}
    
    public void tambahKontak(String nama, String telepon, String kategori) {
    DatabaseHelper dbHelper = new DatabaseHelper();
    try (Connection conn = dbHelper.connect()) {
        String sql = "INSERT INTO kontak (nama, telepon, kategori) VALUES (?, ?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, nama);
        pstmt.setString(2, telepon);
        pstmt.setString(3, kategori);
        pstmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
    
    public List<String[]> getKontakList() {
    List<String[]> kontakList = new ArrayList<>();
    DatabaseHelper dbHelper = new DatabaseHelper();
    try (Connection conn = dbHelper.connect()) {
        String sql = "SELECT * FROM kontak";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {
            String[] kontak = new String[4];
            kontak[0] = String.valueOf(rs.getInt("id"));
            kontak[1] = rs.getString("nama");
            kontak[2] = rs.getString("telepon");
            kontak[3] = rs.getString("kategori");
            kontakList.add(kontak);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return kontakList;
}
    
    public void updateKontak(int id, String nama, String telepon, String kategori) {
    DatabaseHelper dbHelper = new DatabaseHelper();
    try (Connection conn = dbHelper.connect()) {
        String sql = "UPDATE kontak SET nama = ?, telepon = ?, kategori = ? WHERE id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, nama);
        pstmt.setString(2, telepon);
        pstmt.setString(3, kategori);
        pstmt.setInt(4, id);
        pstmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
    
    public void hapusKontak(int id) {
    DatabaseHelper dbHelper = new DatabaseHelper();
    try (Connection conn = dbHelper.connect()) {
        String sql = "DELETE FROM kontak WHERE id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, id);
        pstmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
    
   public void tampilkanKontak(DefaultTableModel model) {
    model.setRowCount(0); // Menghapus baris lama
    List<String[]> kontakList = getKontakList(); // Mendapatkan daftar kontak dari database

    for (String[] kontak : kontakList) {
        model.addRow(kontak); // Tambahkan kontak ke dalam JTable
    }
}
   
   public List<String[]> cariKontak(String keyword) {
    List<String[]> kontakList = new ArrayList<>();
    DatabaseHelper dbHelper = new DatabaseHelper();
    
    try (Connection conn = dbHelper.connect()) {
        String sql = "SELECT * FROM kontak WHERE nama LIKE ? OR telepon LIKE ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, "%" + keyword + "%");
        pstmt.setString(2, "%" + keyword + "%");
        
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            String[] kontak = new String[4];
            kontak[0] = String.valueOf(rs.getInt("id"));
            kontak[1] = rs.getString("nama");
            kontak[2] = rs.getString("telepon");
            kontak[3] = rs.getString("kategori");
            kontakList.add(kontak);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    
    return kontakList;
}
   
   public void tampilkanKontakBerdasarkanKategori(String kategori) {
    tableModel.setRowCount(0); // Menghapus semua baris lama di tabel
    if (kategori == null || tableModel == null) {
        System.out.println("Kategori atau tableModel tidak boleh null");
        return;
    }

    List<String[]> kontakList = getKontakList(); // Dapatkan daftar semua kontak

    // Filter kontak berdasarkan kategori
    for (String[] kontak : kontakList) {
        if (kontak[3].equals(kategori)) { // Asumsi kontak[3] adalah kolom kategori
            tableModel.addRow(kontak); // Tambahkan kontak yang sesuai ke tabel
        }
    }
}
   
   public boolean validasiNomorTelepon(String telepon) {
    // Cek apakah panjang nomor telepon sesuai
    if (telepon.length() < 10 || telepon.length() > 13) {
        return false;
    }
    // Cek apakah hanya berisi angka
    for (char c : telepon.toCharArray()) {
        if (!Character.isDigit(c)) {
            return false;
        }
    }
    return true;
}
   
   public void eksporKeCSV(String filePath) {
    try (FileWriter writer = new FileWriter(filePath)) {
        // Tulis header kolom
        writer.write("ID,Nama,Telepon,Kategori\n");
        
        // Iterasi melalui data di tableModel
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String id = tableModel.getValueAt(i, 0).toString();
            String nama = tableModel.getValueAt(i, 1).toString();
            String telepon = tableModel.getValueAt(i, 2).toString();
            String kategori = tableModel.getValueAt(i, 3).toString();
            
            writer.write(id + "," + nama + "," + telepon + "," + kategori + "\n");
        }
        
        writer.flush();
        JOptionPane.showMessageDialog(this, "Data berhasil diekspor ke " + filePath, "Ekspor Berhasil", JOptionPane.INFORMATION_MESSAGE);
    } catch (IOException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat mengekspor data", "Error", JOptionPane.ERROR_MESSAGE);
    }
}
   
   public void imporDariCSV(String filePath) {
    DatabaseHelper dbHelper = new DatabaseHelper();
    
    try (BufferedReader reader = new BufferedReader(new FileReader(filePath));
         Connection conn = dbHelper.connect()) {
         
        String line;
        
        // Lewati header
        reader.readLine();
        
        // Iterasi setiap baris
        while ((line = reader.readLine()) != null) {
            String[] data = line.split(",");
            
            if (data.length == 4) { // Pastikan ada 4 kolom data
                String id = data[0];
                String nama = data[1];
                String telepon = data[2];
                String kategori = data[3];
                
                // Tambahkan data ke database
                String sql = "INSERT INTO kontak (id, nama, telepon, kategori) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, Integer.parseInt(id));
                pstmt.setString(2, nama);
                pstmt.setString(3, telepon);
                pstmt.setString(4, kategori);
                pstmt.executeUpdate();
            }
        }
        
        JOptionPane.showMessageDialog(this, "Data berhasil diimpor dari " + filePath, "Impor Berhasil", JOptionPane.INFORMATION_MESSAGE);
        
    } catch (IOException | SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat mengimpor data", "Error", JOptionPane.ERROR_MESSAGE);
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
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        namaTextField = new javax.swing.JTextField();
        teleponTextField = new javax.swing.JTextField();
        kategoriComboBox = new javax.swing.JComboBox<>();
        tambahButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        hapusButton = new javax.swing.JButton();
        cariButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        kontakTable = new javax.swing.JTable();
        eksporButton = new javax.swing.JButton();
        imporButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Aplikasi Pengelolaan Kontak", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 18))); // NOI18N
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Nama");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(68, 40, 0, 0);
        jPanel1.add(jLabel1, gridBagConstraints);

        jLabel2.setText("Nomor Telpon");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(25, 40, 0, 0);
        jPanel1.add(jLabel2, gridBagConstraints);

        jLabel3.setText("Kategori");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(25, 40, 0, 0);
        jPanel1.add(jLabel3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 192;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(65, 53, 0, 0);
        jPanel1.add(namaTextField, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 192;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(22, 53, 0, 0);
        jPanel1.add(teleponTextField, gridBagConstraints);

        kategoriComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pilih Kategori", "Keluarga", "Teman", "Kerja" }));
        kategoriComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                kategoriComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 43;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(22, 53, 0, 0);
        jPanel1.add(kategoriComboBox, gridBagConstraints);

        tambahButton.setText("Tambah");
        tambahButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tambahButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(64, 42, 0, 46);
        jPanel1.add(tambahButton, gridBagConstraints);

        editButton.setText("Ubah");
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(21, 41, 0, 44);
        jPanel1.add(editButton, gridBagConstraints);

        hapusButton.setText("Hapus");
        hapusButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hapusButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(21, 41, 49, 44);
        jPanel1.add(hapusButton, gridBagConstraints);

        cariButton.setText("Cari");
        cariButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cariButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(21, 18, 49, 0);
        jPanel1.add(cariButton, gridBagConstraints);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Daftar Kontak"));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        kontakTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Nama", "Nomor Telpon", "Kategori"
            }
        ));
        jScrollPane1.setViewportView(kontakTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 457;
        gridBagConstraints.ipady = 260;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(22, 16, 0, 29);
        jPanel2.add(jScrollPane1, gridBagConstraints);

        eksporButton.setText("Exspor Data");
        eksporButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eksporButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 16, 7, 0);
        jPanel2.add(eksporButton, gridBagConstraints);

        imporButton.setText("Impor Data");
        imporButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imporButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 18, 7, 0);
        jPanel2.add(imporButton, gridBagConstraints);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 373, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tambahButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tambahButtonActionPerformed
        // TODO add your handling code here:
        
            String nama = namaTextField.getText();
            String telepon = teleponTextField.getText();
            String kategori = (String) kategoriComboBox.getSelectedItem();

            // Validasi nomor telepon
            if (!validasiNomorTelepon(telepon)) {
                JOptionPane.showMessageDialog(this, "Nomor telepon harus berisi angka dan memiliki panjang antara 10-13 digit.", "Error", JOptionPane.ERROR_MESSAGE);
                return; // Batalkan proses jika validasi gagal
            }

            // Jika validasi berhasil, lanjutkan dengan menambah kontak
            tambahKontak(nama, telepon, kategori);
            tampilkanKontak(tableModel); // Refresh JTable setelah tambah data

    }//GEN-LAST:event_tambahButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        // TODO add your handling code here:
        int selectedRow = kontakTable.getSelectedRow();
        if (selectedRow != -1) { // Pastikan ada baris yang dipilih
            int id = Integer.parseInt((String) tableModel.getValueAt(selectedRow, 0)); // Ambil ID dari kolom ke-0
            String nama = namaTextField.getText(); // Ambil nama dari JTextField
            String telepon = teleponTextField.getText(); // Ambil telepon dari JTextField
            String kategori = (String) kategoriComboBox.getSelectedItem(); // Ambil kategori dari JComboBox

            // Update kontak dalam database atau model
            updateKontak(id, nama, telepon, kategori);

            // Refresh JTable setelah update data
            tampilkanKontak(tableModel);
        }
    }//GEN-LAST:event_editButtonActionPerformed

    private void hapusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hapusButtonActionPerformed
        // TODO add your handling code here:
        hapusButton.addActionListener(e -> {
            int selectedRow = kontakTable.getSelectedRow();
            if (selectedRow != -1) { // Pastikan ada baris yang dipilih
                int id = Integer.parseInt((String) tableModel.getValueAt(selectedRow, 0));
                hapusKontak(id);
                tampilkanKontak(tableModel); // Refresh JTable setelah hapus data
            }
        });
    }//GEN-LAST:event_hapusButtonActionPerformed

    private void cariButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cariButtonActionPerformed
        // TODO add your handling code here:
        cariButton.addActionListener(e -> {
            String keyword = namaTextField.getText();
            List<String[]> hasilCari = cariKontak(keyword); // cariKontak adalah metode pencarian
            tableModel.setRowCount(0); // Bersihkan tabel sebelum menampilkan hasil
            for (String[] kontak : hasilCari) {
                tableModel.addRow(kontak);
            }
        });
    }//GEN-LAST:event_cariButtonActionPerformed

    private void kategoriComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_kategoriComboBoxItemStateChanged
        // TODO add your handling code here:
        kategoriComboBox.addItemListener(e -> {
            if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                String kategoriTerpilih = (String) e.getItem();
                // Panggil metode untuk menampilkan kontak berdasarkan kategori
                tampilkanKontakBerdasarkanKategori(kategoriTerpilih);
            }
        });
    
    }//GEN-LAST:event_kategoriComboBoxItemStateChanged

    private void eksporButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eksporButtonActionPerformed
        // TODO add your handling code here:
        eksporButton.addActionListener(e -> {
            String filePath = "kontak.csv"; // Tentukan path atau gunakan dialog untuk memilih path
            eksporKeCSV(filePath);
        });
    }//GEN-LAST:event_eksporButtonActionPerformed

    private void imporButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imporButtonActionPerformed
        // Pilih file CSV menggunakan JFileChooser
        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        int returnValue = fileChooser.showOpenDialog(this);

        if (returnValue == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            String filePath = file.getAbsolutePath();

            // Panggil metode impor dari CSV
            imporDariCSV(filePath);

            // Refresh JTable setelah impor
            tampilkanKontak(tableModel);
        }
    }//GEN-LAST:event_imporButtonActionPerformed

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
            java.util.logging.Logger.getLogger(AplikasiPengelolaanKontak.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AplikasiPengelolaanKontak.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AplikasiPengelolaanKontak.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AplikasiPengelolaanKontak.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AplikasiPengelolaanKontak().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cariButton;
    private javax.swing.JButton editButton;
    private javax.swing.JButton eksporButton;
    private javax.swing.JButton hapusButton;
    private javax.swing.JButton imporButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox<String> kategoriComboBox;
    private javax.swing.JTable kontakTable;
    private javax.swing.JTextField namaTextField;
    private javax.swing.JButton tambahButton;
    private javax.swing.JTextField teleponTextField;
    // End of variables declaration//GEN-END:variables
}
