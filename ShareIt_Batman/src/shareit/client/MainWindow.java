/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package shareit.client;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import shareit.ChangePasswordRequest;
import shareit.ChangeUsernameRequest;
import shareit.FileInfo;
import shareit.FileListRequest;
import shareit.FileRequest;
import shareit.User;
import shareit.UserListRequest;
import shareit.Utils;

/**
 *
 * @author ioana
 */
public class MainWindow extends javax.swing.JFrame {

    /**
     * Creates new form AppWindow
     */
    class SearchTabResponse {
        public String fileName;
        public String size;
        public String type;
        public String username;
        public SearchTabResponse(String fn, String sz, String tp, String un) {
            this.fileName = fn;
            this.size = sz;
            this.type = tp;
            this.username = un;
        }
    }
    private ArrayList<SearchTabResponse> filesList = new ArrayList<> ();
    
    public void hideAuxPanels() {
        setConnectionPanel.setVisible(false);
        setProfilePanel.setVisible(false);
        setSharingPanel.setVisible(false);
    }
    
    
    public void clearFileResults() {
        filesList.clear();
        displaySearchTabResponse();
        
    }
    public void clearUsersListResults() {
        DefaultTableModel model = (DefaultTableModel) usersSearchResults.getModel();
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }
   //     onlineUsers.clear();
    }
    
    public void displaySearchTabResponse() {
        goToSearchTab();
        DefaultTableModel model = (DefaultTableModel) fileSearchResponse.getModel();
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }
        for (SearchTabResponse file : filesList) {
            model.addRow(new Object[]{"Download", file.fileName,
                file.size, file.type, file.username});
        }
        return;
    }
    
    public void displayUsersSearchTabRespose(UserListRequest userListResponse) {
        goToUsersTab();
        DefaultTableModel model = (DefaultTableModel) usersSearchResults.getModel();
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }
        if (userListResponse.isSuccess) {
            for (User user : userListResponse.users) {
                model.addRow(new Object[]{"Download", user.username});
            }
            return;
        } 
        JOptionPane.showMessageDialog(AppTab, userListResponse.message);
    }
    
    public void updateFileResults(FileListRequest fileListRequest) {
        // size = 0 => eroare
        if (fileListRequest.isSuccess == false) {
            JOptionPane.showMessageDialog(AppTab, fileListRequest.message);
            return;
        }
        else {
            for (FileInfo fi : fileListRequest.fileList) {
                filesList.add(new SearchTabResponse(fi.path, "" + fi.size, 
                        fi.filetype, fileListRequest.responderUsername));
            }
        }
        displaySearchTabResponse();
    }
    
    public void updateUserList(UserListRequest userListResponse) {
        //System.out.println(userListResponse.isSuccess);
        if (userListResponse.isSuccess == true) {
            displayUsersSearchTabRespose(userListResponse);
            return;
        }
        //System.out.println("117");
        JOptionPane.showMessageDialog(AppTab, userListResponse.message);
    }
    
    public void updateHistory(ArrayList<Object> log) {
        DefaultListModel model = new DefaultListModel();
        for (int i = 0; i < log.size(); i++) {
            Object o = log.get(i);
            if (o instanceof FileRequest) {
                FileRequest r = (FileRequest) o;
                model.addElement("Downloaded " + r.filename);
            }
            if (o instanceof ChangeUsernameRequest) {
                ChangeUsernameRequest r = (ChangeUsernameRequest) o;
                model.addElement("Changed username to " + r.newUsername);
            }
            if (o instanceof FileListRequest) {
                FileListRequest r = (FileListRequest) o;
                model.addElement("Downloaded file list from " + r.responderUsername);
            }
        }
        historyResultsList.setModel(model);
    }
    
    public void clearHistory() {
        DefaultListModel model = new DefaultListModel();
        model.removeAllElements();
        historyResultsList.setModel(model);
    }
    
    public void goToSearchTab() {
        // tabul de search are indexul 1 
        AppTab.setSelectedIndex(1);
    }
    public void goToUsersTab() {
        // tabul de search are indexul 1 
        //AppTab.setSelectedIndex(2);
    }
    public class MyTableModel extends DefaultTableModel {

        public MyTableModel(Object[][] tableData, Object[] colNames) {
           super(tableData, colNames);
        }

        public boolean isCellEditable(int row, int column) {
           return false;
        }
    }

    public void showFilelistResults() {
        DefaultTableModel model = (DefaultTableModel) filelistsResultsTable.getModel();
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }
        for (String currentUser : Model.getInstance().getDownloadedFilelist()) {
            model.addRow(new Object[]{"Download", currentUser});
        }
    }
    public MainWindow() {
        initComponents();
        hideAuxPanels();
        searchingHistoryButton.setVisible(false);
        filelistsSearchButton.setVisible(false);
        sharedSearchInput.setVisible(false);
        filelistsSeatchInput.setVisible(false);
        profileSearchInput.setVisible(false);
        usersSearchResults.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    JTable target = (JTable)e.getSource();
                    int row = target.getSelectedRow();
                    int column = target.getSelectedColumn();
                    if (column > 0) {
                        return;
                    }
                    Controller c = Controller.getInstance();
                    Model.getInstance().addDownloadedFilelist(target.getValueAt(row, 1).toString());
                    showFilelistResults();
                    c.sendSearchCommand("(.*)",target.getValueAt(row, 1).toString());
                }
            }
        });
        
        filelistsResultsTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JTable target = (JTable)e.getSource();
                    int row = target.getSelectedRow();
                    int column = target.getSelectedColumn();
                    if (column > 0) {
                        return;
                    }
                    Controller c = Controller.getInstance();
                    Model.getInstance().addDownloadedFilelist(target.getValueAt(row, 1).toString());
                    showFilelistResults();
                    c.sendSearchCommand("(.*)",target.getValueAt(row, 1).toString());
                }
            }
        });
        
        fileSearchResponse.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JTable target = (JTable)e.getSource();
                    int row = target.getSelectedRow();
                    int column = target.getSelectedColumn();
                    if (column > 0) {
                        return;
                    }
                    Controller c = Controller.getInstance();
                    
                    c.downloadFile(target.getValueAt(row, 1).toString(), 
                            target.getValueAt(row, 4).toString());
                }
            }
        });

        
        DefaultListModel model = new DefaultListModel();
        sharedResultsList.setModel(model);

        newDefaultDownloadDirectory.setText(Model.getInstance().getDownloadDirectory().toString());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        AppTab = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        quickSearchInput = new javax.swing.JTextField();
        searchTab = new javax.swing.JPanel();
        searchInput = new javax.swing.JTextField();
        searchButton = new javax.swing.JButton();
        searchJpgButton = new javax.swing.JButton();
        pngSearchButton = new javax.swing.JButton();
        aviSearchButton = new javax.swing.JButton();
        mp4SearchButton = new javax.swing.JButton();
        mp3SearchButton = new javax.swing.JButton();
        folderSearchButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        fileSearchResponse = new javax.swing.JTable();
        usersTab = new javax.swing.JPanel();
        searchUsersInput = new javax.swing.JTextField();
        searchUsersButton = new javax.swing.JButton();
        refreshUsersListButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        usersSearchResults = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        showAllHistoryButton = new javax.swing.JButton();
        searchingHistoryButton = new javax.swing.JButton();
        filelistsSearchButton = new javax.swing.JButton();
        deleteItemsButton = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        historyResultsList = new javax.swing.JList();
        jPanel5 = new javax.swing.JPanel();
        sharedSearchInput = new javax.swing.JTextField();
        clearSearedButton = new javax.swing.JButton();
        addSharedFilesButton = new javax.swing.JButton();
        deleteSharedFilesButton = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        sharedResultsList = new javax.swing.JList();
        jPanel6 = new javax.swing.JPanel();
        filelistsSeatchInput = new javax.swing.JTextField();
        clearFilelistsButton = new javax.swing.JButton();
        deteleFilelistsButton = new javax.swing.JButton();
        jScrollPane8 = new javax.swing.JScrollPane();
        filelistsResultsTable = new javax.swing.JTable();
        jPanel8 = new javax.swing.JPanel();
        profileSearchInput = new javax.swing.JTextField();
        profileSearchButton = new javax.swing.JButton();
        setConnectionButon = new javax.swing.JButton();
        setSharingSettingsButton = new javax.swing.JButton();
        logOutButton = new javax.swing.JButton();
        setProfilePanel = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        newUsernameInput = new javax.swing.JTextPane();
        newPasswordInput = new javax.swing.JPasswordField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        newLocationInput = new javax.swing.JTextField();
        jScrollPane7 = new javax.swing.JScrollPane();
        newDescriptionInput = new javax.swing.JTextArea();
        profileSaveButton = new javax.swing.JButton();
        setConnectionPanel = new javax.swing.JPanel();
        uploadSlotsNumber = new javax.swing.JTextField();
        newConnectionTime = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        downloadSlotsNumber = new javax.swing.JTextField();
        saveButton = new javax.swing.JButton();
        setSharingPanel = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        newDefaultDownloadDirectory = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        AppTab.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                AppTabMouseClicked(evt);
            }
        });
        AppTab.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                AppTabFocusGained(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Ubuntu", 1, 36)); // NOI18N
        jLabel1.setText("Quick Search:");

        quickSearchInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quickSearchInputActionPerformed(evt);
            }
        });
        quickSearchInput.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                quickSearchInputKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(181, 181, 181)
                        .addComponent(quickSearchInput, javax.swing.GroupLayout.PREFERRED_SIZE, 521, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(310, 310, 310)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 279, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(267, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(53, 53, 53)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(quickSearchInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(485, Short.MAX_VALUE))
        );

        AppTab.addTab("", new javax.swing.ImageIcon(getClass().getResource("/shareit/resources/batmanIcon.png")), jPanel1); // NOI18N

        searchTab.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                searchTabMouseClicked(evt);
            }
        });

        searchInput.setText("SEARCH");
        searchInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchInputActionPerformed(evt);
            }
        });

        searchButton.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        searchButton.setText("QuickSearch");
        searchButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                searchButtonMouseClicked(evt);
            }
        });
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        searchJpgButton.setText(".jpg");
        searchJpgButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                searchJpgButtonMouseClicked(evt);
            }
        });

        pngSearchButton.setText(".png");
        pngSearchButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pngSearchButtonMouseClicked(evt);
            }
        });

        aviSearchButton.setText(".avi");
        aviSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aviSearchButtonActionPerformed(evt);
            }
        });

        mp4SearchButton.setText(".mp4");
        mp4SearchButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mp4SearchButtonMouseClicked(evt);
            }
        });

        mp3SearchButton.setText(".mp3");
        mp3SearchButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mp3SearchButtonMouseClicked(evt);
            }
        });

        folderSearchButton.setText("Folder");

        fileSearchResponse.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null}
            },
            new String [] {
                "Match", "Name", "Size", "Type", "User"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(fileSearchResponse);

        javax.swing.GroupLayout searchTabLayout = new javax.swing.GroupLayout(searchTab);
        searchTab.setLayout(searchTabLayout);
        searchTabLayout.setHorizontalGroup(
            searchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(searchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(searchButton)
                    .addComponent(folderSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, searchTabLayout.createSequentialGroup()
                        .addGroup(searchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, searchTabLayout.createSequentialGroup()
                                .addComponent(searchJpgButton, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(searchTabLayout.createSequentialGroup()
                                .addComponent(aviSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(8, 8, 8)))
                        .addGroup(searchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(mp4SearchButton, javax.swing.GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE)
                            .addComponent(pngSearchButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(mp3SearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(57, 57, 57)
                .addGroup(searchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(searchInput)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 697, Short.MAX_VALUE))
                .addContainerGap(657, Short.MAX_VALUE))
        );
        searchTabLayout.setVerticalGroup(
            searchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(searchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchButton))
                .addGap(18, 18, 18)
                .addGroup(searchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(searchTabLayout.createSequentialGroup()
                        .addGroup(searchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(searchJpgButton, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(pngSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(searchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(mp4SearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(aviSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(mp3SearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(folderSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(135, Short.MAX_VALUE))
        );

        AppTab.addTab("Search", searchTab);

        searchUsersInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchUsersInputActionPerformed(evt);
            }
        });

        searchUsersButton.setText("Search Users");
        searchUsersButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                searchUsersButtonMouseClicked(evt);
            }
        });

        refreshUsersListButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/shareit/resources/refreshImg.jpg"))); // NOI18N
        refreshUsersListButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                refreshUsersListButtonMouseClicked(evt);
            }
        });

        usersSearchResults.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Download", "Username"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(usersSearchResults);

        javax.swing.GroupLayout usersTabLayout = new javax.swing.GroupLayout(usersTab);
        usersTab.setLayout(usersTabLayout);
        usersTabLayout.setHorizontalGroup(
            usersTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(usersTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(usersTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(usersTabLayout.createSequentialGroup()
                        .addComponent(searchUsersButton, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(9, 9, 9))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, usersTabLayout.createSequentialGroup()
                        .addComponent(refreshUsersListButton)
                        .addGap(26, 26, 26)))
                .addGroup(usersTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 764, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchUsersInput, javax.swing.GroupLayout.PREFERRED_SIZE, 752, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(82, Short.MAX_VALUE))
        );
        usersTabLayout.setVerticalGroup(
            usersTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(usersTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(searchUsersInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(usersTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(usersTabLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(searchUsersButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(refreshUsersListButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(usersTabLayout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(150, Short.MAX_VALUE))
        );

        AppTab.addTab("Users", usersTab);

        showAllHistoryButton.setText("Show All");
        showAllHistoryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showAllHistoryButtonActionPerformed(evt);
            }
        });

        searchingHistoryButton.setText("Searching");

        filelistsSearchButton.setText("Filelists");

        deleteItemsButton.setText("Delete");
        deleteItemsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteItemsButtonActionPerformed(evt);
            }
        });

        clearButton.setText("Clear");
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });

        historyResultsList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "andrei" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane3.setViewportView(historyResultsList);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addComponent(clearButton, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12))
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(filelistsSearchButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(searchingHistoryButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                        .addComponent(showAllHistoryButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(deleteItemsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 727, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(711, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(46, 46, 46)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(showAllHistoryButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(searchingHistoryButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(filelistsSearchButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteItemsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(44, 44, 44)
                        .addComponent(clearButton, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(247, Short.MAX_VALUE))
        );

        AppTab.addTab("History", jPanel4);

        sharedSearchInput.setText("SEARCH");

        clearSearedButton.setText("Clear");
        clearSearedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearSearedButtonActionPerformed(evt);
            }
        });

        addSharedFilesButton.setText("Add");
        addSharedFilesButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addSharedFilesButtonMouseClicked(evt);
            }
        });
        addSharedFilesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSharedFilesButtonActionPerformed(evt);
            }
        });

        deleteSharedFilesButton.setText("Delete");
        deleteSharedFilesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteSharedFilesButtonActionPerformed(evt);
            }
        });

        jScrollPane4.setViewportView(sharedResultsList);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(clearSearedButton, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addSharedFilesButton, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deleteSharedFilesButton, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(sharedSearchInput)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 768, Short.MAX_VALUE))
                .addContainerGap(89, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(sharedSearchInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(clearSearedButton, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addSharedFilesButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteSharedFilesButton, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 334, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(241, Short.MAX_VALUE))
        );

        AppTab.addTab("Shared", jPanel5);

        jPanel6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel6MouseClicked(evt);
            }
        });
        jPanel6.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jPanel6FocusGained(evt);
            }
        });

        filelistsSeatchInput.setText("SEARCH");

        clearFilelistsButton.setText("Clear");
        clearFilelistsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearFilelistsButtonActionPerformed(evt);
            }
        });

        deteleFilelistsButton.setText("Delete");
        deteleFilelistsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                deteleFilelistsButtonMouseClicked(evt);
            }
        });

        filelistsResultsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Download", "Username"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane8.setViewportView(filelistsResultsTable);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(clearFilelistsButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(deteleFilelistsButton, javax.swing.GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 770, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(110, 110, 110)
                        .addComponent(filelistsSeatchInput, javax.swing.GroupLayout.PREFERRED_SIZE, 769, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(695, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(filelistsSeatchInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(clearFilelistsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deteleFilelistsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(149, Short.MAX_VALUE))
        );

        AppTab.addTab("Filelist", jPanel6);

        profileSearchInput.setText("SEARCH");

        profileSearchButton.setText("Profile");
        profileSearchButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                profileSearchButtonMouseClicked(evt);
            }
        });
        profileSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profileSearchButtonActionPerformed(evt);
            }
        });

        setConnectionButon.setText("Connection");
        setConnectionButon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                setConnectionButonMouseClicked(evt);
            }
        });

        setSharingSettingsButton.setText("Sharing");
        setSharingSettingsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                setSharingSettingsButtonMouseClicked(evt);
            }
        });

        logOutButton.setText("LOG OUT");
        logOutButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                logOutButtonMouseClicked(evt);
            }
        });

        jScrollPane6.setViewportView(newUsernameInput);

        jLabel2.setText("username");

        jLabel3.setText("password");

        jLabel4.setText("Location");

        jLabel5.setText("Description");

        newDescriptionInput.setColumns(20);
        newDescriptionInput.setRows(5);
        newDescriptionInput.setToolTipText("");
        jScrollPane7.setViewportView(newDescriptionInput);
        newDescriptionInput.getAccessibleContext().setAccessibleName("");

        profileSaveButton.setText("Save");
        profileSaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profileSaveButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout setProfilePanelLayout = new javax.swing.GroupLayout(setProfilePanel);
        setProfilePanel.setLayout(setProfilePanelLayout);
        setProfilePanelLayout.setHorizontalGroup(
            setProfilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(setProfilePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(setProfilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addGap(18, 18, 18)
                .addGroup(setProfilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane6)
                    .addComponent(newPasswordInput, javax.swing.GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE)
                    .addComponent(newLocationInput))
                .addGap(36, 36, 36)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(profileSaveButton)
                .addContainerGap(223, Short.MAX_VALUE))
        );
        setProfilePanelLayout.setVerticalGroup(
            setProfilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(setProfilePanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(setProfilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(setProfilePanelLayout.createSequentialGroup()
                        .addGroup(setProfilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(setProfilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel2))
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(setProfilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(newPasswordInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(setProfilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(newLocationInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(setProfilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(profileSaveButton)
                        .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );

        setConnectionPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                setConnectionPanelMouseClicked(evt);
            }
        });

        uploadSlotsNumber.setText("1");
        uploadSlotsNumber.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uploadSlotsNumberActionPerformed(evt);
            }
        });
        uploadSlotsNumber.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                uploadSlotsNumberFocusLost(evt);
            }
        });

        newConnectionTime.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newConnectionTimeActionPerformed(evt);
            }
        });
        newConnectionTime.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                newConnectionTimeFocusLost(evt);
            }
        });

        jLabel6.setText("Slots Up / Down");

        jLabel7.setText("Connection Time");

        downloadSlotsNumber.setText("1");
        downloadSlotsNumber.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                downloadSlotsNumberFocusLost(evt);
            }
        });

        saveButton.setText("Save");
        saveButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                saveButtonMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout setConnectionPanelLayout = new javax.swing.GroupLayout(setConnectionPanel);
        setConnectionPanel.setLayout(setConnectionPanelLayout);
        setConnectionPanelLayout.setHorizontalGroup(
            setConnectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(setConnectionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(uploadSlotsNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15)
                .addComponent(downloadSlotsNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(newConnectionTime, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(saveButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        setConnectionPanelLayout.setVerticalGroup(
            setConnectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(setConnectionPanelLayout.createSequentialGroup()
                .addContainerGap(14, Short.MAX_VALUE)
                .addGroup(setConnectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(uploadSlotsNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(newConnectionTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(downloadSlotsNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(saveButton)))
        );

        jLabel8.setText("Default Download Directory:");

        newDefaultDownloadDirectory.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                newDefaultDownloadDirectoryFocusLost(evt);
            }
        });

        javax.swing.GroupLayout setSharingPanelLayout = new javax.swing.GroupLayout(setSharingPanel);
        setSharingPanel.setLayout(setSharingPanelLayout);
        setSharingPanelLayout.setHorizontalGroup(
            setSharingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(setSharingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addGap(32, 32, 32)
                .addComponent(newDefaultDownloadDirectory, javax.swing.GroupLayout.PREFERRED_SIZE, 312, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        setSharingPanelLayout.setVerticalGroup(
            setSharingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(setSharingPanelLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(setSharingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(newDefaultDownloadDirectory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(logOutButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(setSharingSettingsButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(setConnectionButon, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE)
                    .addComponent(profileSearchButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(profileSearchInput)
                    .addComponent(setProfilePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(setConnectionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(50, 50, 50))
                    .addComponent(setSharingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(502, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(profileSearchInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(profileSearchButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(setConnectionButon)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(setSharingSettingsButton)
                        .addGap(39, 39, 39)
                        .addComponent(logOutButton))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(setProfilePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(42, 42, 42)
                        .addComponent(setConnectionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(setSharingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(286, Short.MAX_VALUE))
        );

        AppTab.addTab("Profile", jPanel8);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(AppTab)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(AppTab)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void AppTabMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_AppTabMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_AppTabMouseClicked

    private void quickSearchInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quickSearchInputActionPerformed

        String result = evt.getActionCommand();
        if (result == null || result.isEmpty() ) {
            goToSearchTab();
        }
        Controller c = Controller.getInstance();
        c.sendSearchCommand(evt.getActionCommand(),"");
    }//GEN-LAST:event_quickSearchInputActionPerformed

    private void searchInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchInputActionPerformed
        // TODO add your handling code here:
        String result = evt.getActionCommand();
        if (result != null && !result.isEmpty() ) {
            Controller controller = Controller.getInstance();
            controller.sendSearchCommand(result,"");
           
            return;
        }
        goToSearchTab();
    }//GEN-LAST:event_searchInputActionPerformed

    private void clearFilelistsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearFilelistsButtonActionPerformed
        // TODO add your handling code here:
        DefaultTableModel model = (DefaultTableModel) filelistsResultsTable.getModel();
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }
        Model.getInstance().clearDownloadedFilelist();
    }//GEN-LAST:event_clearFilelistsButtonActionPerformed

    private void profileSearchButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_profileSearchButtonMouseClicked

        hideAuxPanels();
        setProfilePanel.setVisible(true);
    }//GEN-LAST:event_profileSearchButtonMouseClicked

    private void profileSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profileSearchButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_profileSearchButtonActionPerformed

    private void uploadSlotsNumberActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uploadSlotsNumberActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_uploadSlotsNumberActionPerformed

    private void newConnectionTimeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newConnectionTimeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_newConnectionTimeActionPerformed

    private void setConnectionPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_setConnectionPanelMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_setConnectionPanelMouseClicked

    private void setConnectionButonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_setConnectionButonMouseClicked
        // TODO add your handling code here:
        hideAuxPanels();
        setConnectionPanel.setVisible(true);
    }//GEN-LAST:event_setConnectionButonMouseClicked

    private void setSharingSettingsButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_setSharingSettingsButtonMouseClicked
        // TODO add your handling code here:
         hideAuxPanels();
         setSharingPanel.setVisible(true);
    }//GEN-LAST:event_setSharingSettingsButtonMouseClicked

    private void searchTabMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchTabMouseClicked

    }//GEN-LAST:event_searchTabMouseClicked

    private void quickSearchInputKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_quickSearchInputKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_quickSearchInputKeyPressed

    private void searchJpgButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchJpgButtonMouseClicked
        // TODO add your handling code here:
        Controller c = Controller.getInstance();
        c.sendSearchCommand("(.*).jpg","");
    }//GEN-LAST:event_searchJpgButtonMouseClicked

    private void pngSearchButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pngSearchButtonMouseClicked
        // TODO add your handling code here:
        Controller c = Controller.getInstance();
        c.sendSearchCommand("(.*).png","");
    }//GEN-LAST:event_pngSearchButtonMouseClicked

    private void aviSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aviSearchButtonActionPerformed
        // TODO add your handling code here:
        Controller c = Controller.getInstance();
        c.sendSearchCommand("(.*).avi","");
    }//GEN-LAST:event_aviSearchButtonActionPerformed

    private void mp4SearchButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mp4SearchButtonMouseClicked
        // TODO add your handling code here:
        Controller c = Controller.getInstance();
        c.sendSearchCommand("(.*).mp4","");
    }//GEN-LAST:event_mp4SearchButtonMouseClicked

    private void mp3SearchButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mp3SearchButtonMouseClicked
        // TODO add your handling code here:
        Controller c = Controller.getInstance();
        c.sendSearchCommand("(.*).mp3","");
    }//GEN-LAST:event_mp3SearchButtonMouseClicked

    private void searchButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchButtonMouseClicked
        // TODO add your handling code here:
        String result = searchInput.getText();
        if (result != null && !result.isEmpty() ) {
            Controller controller = Controller.getInstance();
            controller.sendSearchCommand(result,"");
            
            return;
        }
        goToSearchTab();
        
    }//GEN-LAST:event_searchButtonMouseClicked

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        // TODO add your handling code here:
        
        
    }//GEN-LAST:event_searchButtonActionPerformed

    private void searchUsersInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchUsersInputActionPerformed
        // TODO add your handling code here:
        String result = evt.getActionCommand();
        if (result != null && !result.isEmpty() ) {
            Controller controller = Controller.getInstance();
            controller.sendSearchUsersCommand(result);
            
            return;
        }
        goToUsersTab();
    }//GEN-LAST:event_searchUsersInputActionPerformed

    private void searchUsersButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchUsersButtonMouseClicked
        // TODO add your handling code here:
        String result = searchUsersInput.getText();
        //if (result != null && !result.isEmpty() ) {
        Controller controller = Controller.getInstance();
        controller.sendSearchUsersCommand(result);
        //}
        //goToUsersTab();
    }//GEN-LAST:event_searchUsersButtonMouseClicked

    private void addSharedFilesButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addSharedFilesButtonMouseClicked
        // TODO add your handling code here:
        String input = sharedSearchInput.getText();
        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(AppTab, "Insert a string please!");
        }
    }//GEN-LAST:event_addSharedFilesButtonMouseClicked

    private void AppTabFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_AppTabFocusGained
        Controller.getInstance().updateHistoryTab(historyResultsList);
    }//GEN-LAST:event_AppTabFocusGained

    private void uploadSlotsNumberFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_uploadSlotsNumberFocusLost

    }//GEN-LAST:event_uploadSlotsNumberFocusLost

    private void downloadSlotsNumberFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_downloadSlotsNumberFocusLost

    }//GEN-LAST:event_downloadSlotsNumberFocusLost

    public void updateUsername(ChangeUsernameRequest response) {
        if (response.isSuccess == false) {
            JOptionPane.showMessageDialog(AppTab, response.message);
        }
    }
    private void addSharedFilesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSharedFilesButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
//            addSharedResultsList(chooser.getSelectedFile().getAbsolutePath(), "");
            Controller.getInstance().addSharedResultsList(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_addSharedFilesButtonActionPerformed

    private void deleteSharedFilesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteSharedFilesButtonActionPerformed
        int[] idxs = sharedResultsList.getSelectedIndices();
        DefaultListModel m = (DefaultListModel) sharedResultsList.getModel();
        for (int i = 0 ; i < idxs.length; ++i) {
            Model.getInstance().removeSharedFile((String)m.getElementAt(idxs[i]-i));
            m.remove(idxs[i]-i);
        }
        sharedResultsList.setModel(m);
    }//GEN-LAST:event_deleteSharedFilesButtonActionPerformed

    private void clearSearedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearSearedButtonActionPerformed
        Model.getInstance().clearSharedFiles();
        DefaultListModel m = new DefaultListModel();
        sharedResultsList.setModel(m);
    }//GEN-LAST:event_clearSearedButtonActionPerformed

    public void updatePassword(ChangePasswordRequest response) {
        if (response.isSuccess == false) {
            JOptionPane.showMessageDialog(AppTab, response.message);
        }
    }

    private void newPasswordInputFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_newPasswordInputFocusLost
        String newPassword = new String(newPasswordInput.getPassword());
        if (newPassword.isEmpty()) {
            JOptionPane.showMessageDialog(AppTab, "Abort: newPassword cannot be empty.");
            return;
        }
        Controller.getInstance().changePassword(newPassword);
    }//GEN-LAST:event_newPasswordInputFocusLost

    private void newLocationInputFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_newLocationInputFocusLost
        String newLocation = newLocationInput.getText();
        if (newLocation.isEmpty()) {
            JOptionPane.showMessageDialog(AppTab, "Abort: Location cannot be empty.");
            return;
        }
        Model.getInstance().setLocation(newLocation);
    }//GEN-LAST:event_newLocationInputFocusLost

    private void newDescriptionInputFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_newDescriptionInputFocusLost
        // TODO add your handling code here:
        String newDescription = newDescriptionInput.getText();
        if (newDescription.length() > 140) {
            JOptionPane.showMessageDialog(AppTab, "Abort: Description is too long. Max 140 characters please!");
            return;
        }
        if (newDescription.isEmpty()) {
            JOptionPane.showMessageDialog(AppTab, "Abort: Location cannot be empty.");
            return;
        }
        Model.getInstance().setDescription(newDescription);
    }//GEN-LAST:event_newDescriptionInputFocusLost

    private void newConnectionTimeFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_newConnectionTimeFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_newConnectionTimeFocusLost

    private void newDefaultDownloadDirectoryFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_newDefaultDownloadDirectoryFocusLost
        // TODO add your handling code here:
        Model.getInstance().setDownloadDirectory(newDefaultDownloadDirectory.getText());
    }//GEN-LAST:event_newDefaultDownloadDirectoryFocusLost

    private void saveButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveButtonMouseClicked
        // TODO add your handling code here:
        Integer no = Utils.parsePort(uploadSlotsNumber.getText());
        if (no != null) {
            Model.getInstance().setUploadSlots(no);
        }
        
        no = Utils.parsePort(downloadSlotsNumber.getText());
        if (no != null) {
            Model.getInstance().setDownloadSlots(no);
        }
        
        no = Utils.parsePort(newConnectionTime.getText());
        if (no != null) {
            Model.getInstance().setConnectionTime(no);
        } 
    }//GEN-LAST:event_saveButtonMouseClicked

    private void refreshUsersListButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_refreshUsersListButtonMouseClicked
        // TODO add your handling code here:
        
        Controller controller = Controller.getInstance();
        controller.sendSearchUsersCommand("");
    }//GEN-LAST:event_refreshUsersListButtonMouseClicked

    private void deteleFilelistsButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deteleFilelistsButtonMouseClicked
        // TODO add your handling code here:
        //
        int [] selectedRows = filelistsResultsTable.getSelectedRows();
        DefaultTableModel model = (DefaultTableModel) filelistsResultsTable.getModel();
        for (int i = 0; i < selectedRows.length; ++i) {
            Model.getInstance().deleteDownloadedFilelist(filelistsResultsTable.getValueAt(selectedRows[i] - i, 1).toString());
            model.removeRow(selectedRows[i] - i);
        }
    }//GEN-LAST:event_deteleFilelistsButtonMouseClicked

    private void logOutButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_logOutButtonMouseClicked
        // TODO add your handling code here:
        Controller.getInstance().signOut();
    }//GEN-LAST:event_logOutButtonMouseClicked

    private void profileSaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profileSaveButtonActionPerformed
        Controller.getInstance().saveInfo(newUsernameInput.getText(),
                                        new String(newPasswordInput.getPassword()),
                                        newLocationInput.getText(),
                                        newDescriptionInput.getText());
    }//GEN-LAST:event_profileSaveButtonActionPerformed


    private void jPanel6FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jPanel6FocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_jPanel6FocusGained

    private void jPanel6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel6MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jPanel6MouseClicked

    private void showAllHistoryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showAllHistoryButtonActionPerformed
        updateHistory(Controller.getInstance().getLogItems());
    }//GEN-LAST:event_showAllHistoryButtonActionPerformed

    private void deleteItemsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteItemsButtonActionPerformed
        Controller.getInstance().removeLogItems(historyResultsList.getSelectedIndices());
        updateHistory(Controller.getInstance().getLogItems());
    }//GEN-LAST:event_deleteItemsButtonActionPerformed

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        // TODO add your handling code here:
        clearHistory();
        Controller.getInstance().clearLog();
    }//GEN-LAST:event_clearButtonActionPerformed


    public void addSharedFiles(ArrayList<String> files) {
        DefaultListModel m = (DefaultListModel) sharedResultsList.getModel();
        int size = files.size();
        
        for(int i = 0; i < size; i++) {
            if(!m.contains(files.get(i))) {
                m.addElement(files.get(i));
            }
        }
        
        sharedResultsList.setModel(m);
    }

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
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainWindow().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane AppTab;
    private javax.swing.JButton addSharedFilesButton;
    private javax.swing.JButton aviSearchButton;
    private javax.swing.JButton clearButton;
    private javax.swing.JButton clearFilelistsButton;
    private javax.swing.JButton clearSearedButton;
    private javax.swing.JButton deleteItemsButton;
    private javax.swing.JButton deleteSharedFilesButton;
    private javax.swing.JButton deteleFilelistsButton;
    private javax.swing.JTextField downloadSlotsNumber;
    private javax.swing.JTable fileSearchResponse;
    private javax.swing.JTable filelistsResultsTable;
    private javax.swing.JButton filelistsSearchButton;
    private javax.swing.JTextField filelistsSeatchInput;
    private javax.swing.JButton folderSearchButton;
    private javax.swing.JList historyResultsList;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JButton logOutButton;
    private javax.swing.JButton mp3SearchButton;
    private javax.swing.JButton mp4SearchButton;
    private javax.swing.JTextField newConnectionTime;
    private javax.swing.JTextField newDefaultDownloadDirectory;
    private javax.swing.JTextArea newDescriptionInput;
    private javax.swing.JTextField newLocationInput;
    private javax.swing.JPasswordField newPasswordInput;
    private javax.swing.JTextPane newUsernameInput;
    private javax.swing.JButton pngSearchButton;
    private javax.swing.JButton profileSaveButton;
    private javax.swing.JButton profileSearchButton;
    private javax.swing.JTextField profileSearchInput;
    private javax.swing.JTextField quickSearchInput;
    private javax.swing.JButton refreshUsersListButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JButton searchButton;
    private javax.swing.JTextField searchInput;
    private javax.swing.JButton searchJpgButton;
    private javax.swing.JPanel searchTab;
    private javax.swing.JButton searchUsersButton;
    private javax.swing.JTextField searchUsersInput;
    private javax.swing.JButton searchingHistoryButton;
    private javax.swing.JButton setConnectionButon;
    private javax.swing.JPanel setConnectionPanel;
    private javax.swing.JPanel setProfilePanel;
    private javax.swing.JPanel setSharingPanel;
    private javax.swing.JButton setSharingSettingsButton;
    private javax.swing.JList sharedResultsList;
    private javax.swing.JTextField sharedSearchInput;
    private javax.swing.JButton showAllHistoryButton;
    private javax.swing.JTextField uploadSlotsNumber;
    private javax.swing.JTable usersSearchResults;
    private javax.swing.JPanel usersTab;
    // End of variables declaration//GEN-END:variables
}
