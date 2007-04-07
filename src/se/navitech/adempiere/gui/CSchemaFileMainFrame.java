/*
 * ?SchemaFileMainFrame.java
 *
 * Created on den 31 december 2005, 16:59
 */

package se.navitech.adempiere.gui;

import javax.swing.event.*;
import se.navitech.adempiere.CAccountElement;
import se.navitech.adempiere.CAccountSchema;
import se.navitech.adempiere.CAccountSchemaFile;
import se.navitech.adempiere.CAccountType;
import se.navitech.adempiere.CDefaultAccount;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.table.*;
import java.util.*;

/**
 * Class representing a visual display of an account schema file.
 *
 * @author  Daniel Norin
 */
public class CSchemaFileMainFrame extends javax.swing.JInternalFrame implements KeyListener, ChangeListener, FocusListener {
    
    private CAccountSchemaFile  m_schemaFile;
    private File                m_file;
    private CAccountElement     m_currentAccount;
    private CSchemaFileMainFrame    m_thisFrame;
    private DefaultMutableTreeNode m_treeRoot;
    private SortedMap<String, SortedSet<String>> m_accountTree;
    private SortedMap<String, CDefaultAccount>   m_defaultAccountTree;
    private Vector<CDefaultAccount>              m_defaultAccountVector;
    private CDefAcctTableModel                   m_defAcctTableModel;
    
    private javax.swing.JPopupMenu  m_accountMenu;
    private javax.swing.JPopupMenu  m_accountGroupMenu;
    
    private boolean         m_updateProgress = true;
    
    private boolean         m_modified;         // Flag if the current selected record is modified.
    
    
    /**
     * Action that deletes an account element from the schema
     */
    class ActionDelete extends AbstractAction {
        public ActionDelete(String text, ImageIcon icon,
                String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        
        public void actionPerformed(ActionEvent e) {
            
            if (m_currentAccount==null) {
                JOptionPane.showMessageDialog(m_thisFrame,
                        "You must first select an account to remove.",
                        "Select account", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // See if the selected account has any children
            if (!m_schemaFile.getSchema().hasChildren(m_currentAccount.getKey())) {
                
                // Find what node is selected
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)accountTree.getLastSelectedPathComponent();
                DefaultMutableTreeNode nextNode = node.getNextNode();
                DefaultTreeModel model = (DefaultTreeModel)accountTree.getModel();
                int selRow = accountTree.getLeadSelectionRow();
                model.removeNodeFromParent(node);
                accountTree.setAnchorSelectionPath(accountTree.getPathForRow(selRow));
                m_schemaFile.getSchema().removeAccountElement(m_currentAccount);
                m_currentAccount = null;
                
                // Refresh the account list
                CAccountListTableModel acctListModel = (CAccountListTableModel)accountListTable.getModel();
                acctListModel.refreshValuesFromSchema();
                m_defAcctTableModel.refreshValuesFromSchema();
                
            } else {
                JOptionPane.showMessageDialog(m_thisFrame,
                        "This account has subaccounts. Please remove all subaccounts first.",
                        "Remove subaccounts", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            m_thisFrame.setModified(true);
        }
    }
    
    /**
     * Account that adds an account
     */
    class ActionAdd extends AbstractAction {
        public ActionAdd(String text, ImageIcon icon,
                String desc, Integer mnemonic) {
            super(text,icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        
        public void actionPerformed(ActionEvent e) {
            addAccount(e);
        }
    }
    
    /**
     * Action that saves the current schema file
     */
    class ActionSave extends AbstractAction {
        public ActionSave(String text, ImageIcon icon,
                String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        
        public void actionPerformed(ActionEvent e) {
            try {
                // Save if necessary
                if (m_currentAccount!=null) {
                    getDetailsForm(m_currentAccount);
                }
                m_schemaFile.saveFile(m_file);
                setModified(false);
                JOptionPane.showMessageDialog(m_thisFrame, m_file.getName() + " was saved.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(m_thisFrame, ex.getMessage());
            }
        }
    }
    
    //==========================================================================
    // Action definitions
    
    private ActionDelete actionDelete = new ActionDelete("Remove Account", null, "Removes an account from the schema", KeyEvent.VK_DELETE);
    private ActionAdd    actionAdd = new ActionAdd("Add account", null, "Adds account to the schema", new Integer(KeyEvent.CTRL_DOWN_MASK + KeyEvent.VK_N));
    private ActionSave actionSave = new ActionSave("Save Schema File", null, "Saves schema file", new Integer(KeyEvent.CTRL_DOWN_MASK + KeyEvent.VK_S));
    
    /**
     * Creates new form CSchemaFileMainFrame
     *
     * @param   schemaFile      Pointer to schema file to display. If null, a new file is 
     *                          assumed.
     * @param   defaultAccountTree  A sorted map of default accounts to use for this schema file.
     *
     */
    public CSchemaFileMainFrame(File schemaFile, SortedMap<String, CDefaultAccount> defaultAccountTree) {
        m_thisFrame = this;
        m_file = schemaFile;
        m_defaultAccountTree = defaultAccountTree;
        m_defaultAccountVector = new Vector<CDefaultAccount>(m_defaultAccountTree.values());
        m_schemaFile = new CAccountSchemaFile(defaultAccountTree);

        // Read accounts from file
        if (schemaFile!=null) {
            try {
                m_schemaFile.readFile(schemaFile);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, schemaFile.getAbsolutePath() + " is not a valid compiere account file.\n" +
                        e.getMessage());
                return;
            }
        } else {
            m_schemaFile.newFile();
            actionSave.setEnabled(false);
        }
        
        m_accountTree = m_schemaFile.getSchema().getAccountTree();
        m_treeRoot = createTreeNodes();

        m_defAcctTableModel = new CDefAcctTableModel(m_schemaFile.getSchema());
        initComponents();
        // Insert empty element in default account combobox
        ((DefaultComboBoxModel)defaultAcctCombo.getModel()).insertElementAt(new CDefaultAccount("", ""), 0);
        defaultAcctCombo.setSelectedIndex(0);
        
        setTitle(schemaFile!=null ? schemaFile.getAbsolutePath() : "New schema");
        
        // Adjust table column widths for account list table.
        TableColumnModel tableColModel = accountListTable.getColumnModel();
        tableColModel.getColumn(0).setPreferredWidth(75);
        tableColModel.getColumn(1).setPreferredWidth(150);
        
        // Adjust table column widths for default account list table.
        tableColModel = defAcctTable.getColumnModel();
        tableColModel.getColumn(0).setPreferredWidth(150);
        tableColModel.getColumn(1).setPreferredWidth(150);
        tableColModel.getColumn(2).setPreferredWidth(75);
        
        CAccountElement elem;
        CAccountSchema schema = m_schemaFile.getSchema();
        String defaultAccount;
        
        // Add key listeners
        acctNameText.addKeyListener(this);
        acctDescText.addKeyListener(this);
        
        // Add focus listeners
        acctNameText.addFocusListener(this);
        acctDescText.addFocusListener(this);
        
        // Define account popup menu used on the account tree
        m_accountMenu = new JPopupMenu();
        m_accountMenu.add(new JMenuItem(actionDelete));
        
        // Define account group menu used on the account tree
        m_accountGroupMenu = new JPopupMenu();
        m_accountGroupMenu.add(new JMenuItem(actionAdd));
        
        // Add mouselisteners to account tree for the popup menus.
        accountTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    maybePopUpMenu(evt);
                }
            }
        });
    
        m_updateProgress = false;
    }

    /**
     * Called from the mouse listener attached to the account tree.
     * Finds out which node is closest and displays relevant popup menu.
     */
    private void maybePopUpMenu(java.awt.event.MouseEvent evt) {
        // Select the tree node closest to the mouse
        TreePath path = accountTree.getClosestPathForLocation(evt.getX(), evt.getY());
        if (path!=null) {
            accountTree.setSelectionPath(path);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            Object userObject = node.getUserObject();
            if (userObject instanceof CAccountElement) {
                CAccountElement elem = (CAccountElement)userObject;
                if (elem.isSummary()) {
                    m_accountGroupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                } else {
                    m_accountMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            } else {
                m_accountGroupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
            }
        }
    }
    
    /**
     * Adds account to currently selected account group. Called from ActionAdd.
     */
    private void addAccount(ActionEvent e) {
        TreePath path = accountTree.getSelectionPath();
        if (path==null) {
            JOptionPane.showMessageDialog(m_thisFrame, "Select an account to add to (parent account)");
            return;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
        Object userObject = node.getUserObject();
        String parentId = "";
        if (userObject instanceof CAccountElement) {
            CAccountElement elem = (CAccountElement)userObject;
            if (!elem.isSummary()) {
                JOptionPane.showMessageDialog(m_thisFrame, "Can't add account to a non-summary account");
                return;
            } else {
                parentId = elem.getKey();
            }
        }
        CNewAccountDialog addDiag = new CNewAccountDialog(null, parentId);
        addDiag.setLocationRelativeTo(accountTree);
        addDiag.setVisible(true);
        if (addDiag.isOk()) {
            // Create a new account element
            CAccountElement acct = new CAccountElement();
            acct.setKey(addDiag.getAccountId());
            acct.setName(addDiag.getAccountName());
            acct.setSummary(addDiag.isSummaryAccount());
            acct.setParentKey(parentId);
            // Get account type and account sign from parent
            CAccountSchema schema = m_schemaFile.getSchema();
            if (acct.hasParentKey()) {
                CAccountElement parentElem = schema.getAccountElement(parentId);
                acct.setType(parentElem.getType());
                acct.setSign(parentElem.getSign());
            }
            // ... and add to schema
            schema.addAccountElement(acct);
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(acct);
            // Update tree
            boolean added = false;
            DefaultMutableTreeNode nextNode;
            DefaultTreeModel treeModel = (DefaultTreeModel)accountTree.getModel();
            int addIndex = node.getChildCount();
            String existingKey;
            String insertKey = acct.getKey();
            int cmp;
            for (int i=0; i<node.getChildCount(); i++) {
                nextNode = (DefaultMutableTreeNode)node.getChildAt(i);
                existingKey = ((CAccountElement)nextNode.getUserObject()).getKey();
                cmp = insertKey.compareTo(existingKey);
                if (cmp<0) {
                    // Add node after this node
                    addIndex = i;
                    break;
                }
            }
            treeModel.insertNodeInto(newNode, node, addIndex);
            // Select the newly inserted node
            accountTree.setSelectionPath(new TreePath(newNode.getPath()));
            // Refresh the account list
            CAccountListTableModel acctListModel = (CAccountListTableModel)accountListTable.getModel();
            acctListModel.refreshValuesFromSchema();
            // Set modified to true
            setModified(true);
        }
        
    }
    
    /**
     * Recursive method to add accounts to the account tree.
     * Called from createTreeNodes.
     *
     * @param   root    Root of current tree operation
     * @param   accountKey  Account key.
     *
     * @see     createTreeNodes()
     */
    private void addChildren(DefaultMutableTreeNode root, String accountKey) {
        // Get all keys that are children to this account key
        SortedSet<String> keys = m_accountTree.get(accountKey);
        // If there are no children, end the procedure.
        if (keys==null || keys.size()==0) return;
        CAccountElement elem;
        DefaultMutableTreeNode node;
        // Add all children for this account key
        for (Iterator<String> it = keys.iterator(); it.hasNext();) {
            elem = m_schemaFile.getSchema().getAccountElement(it.next());
            node = new DefaultMutableTreeNode(elem);
            root.add(node);
            // Add children
            addChildren(node, elem.getKey());
        }
    }
    
    /**
     * Create tree nodes from schema file.
     */
    private DefaultMutableTreeNode createTreeNodes() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(m_file!=null ? m_file.getName() : "New schema");
        DefaultMutableTreeNode node;
        CAccountSchema schema = m_schemaFile.getSchema();
        CAccountElement elem;
        for (Iterator<String> it = schema.getRootKeys().iterator(); it.hasNext(); ) {
            elem = schema.getAccountElement(it.next());
            node = new DefaultMutableTreeNode(elem);
            root.add(node);
            addChildren(node, elem.getKey());
        }
        
        return(root);
    }
    
    /**
     * Sets the form from a CAccountElement
     *
     * @param   elem    Account element.
     */
    private void setDetailsForm(CAccountElement elem) {
        m_updateProgress = true;
        acctValueText.setText(elem.getKey());
        acctNameText.setText(elem.getName());
        acctDescText.setText(elem.getDescription());
        // Account type
        acctTypeCombo.setSelectedItem(elem.getType());
        // Account sign
        acctSignCombo.setSelectedItem(elem.getSign());
        docCtlCheck.setSelected(elem.isDocument());
        summaryAcctCheck.setSelected(elem.isSummary());
        // Default account
        String defaultAccount = elem.getDefaultAccount();
        setComboFromDefaultAccount(defaultAccount);
        // Parent account
        CAccountElement parent = m_schemaFile.getSchema().getAccountElement(elem.getParentKey());
        if (parent!=null) {
            acctParentText.setText(parent.toString());
        } else {
            acctParentText.setText("");
        }
        balanceSheetText.setText(elem.getBalanceSheet());
        balanceSheetNameText.setText(elem.getBalanceSheetName());
        us1120balanceSheetText.setText(elem.getUs1120sheet());
        us1120balanceSheetNameText.setText(elem.getUs1120sheetName());
        pnlText.setText(elem.getProfitLoss());
        pnlNameText.setText(elem.getProfitLossName());
        us1120incomeStmtText.setText(elem.getUs1120incomeStmt());
        us1120incomeStmtNameText.setText(elem.getUs1120incomeStmtName());
        cashFlowText.setText(elem.getCashFlow());
        cashFlowNameText.setText(elem.getCashFlowName());
        m_updateProgress = false;
    }
    
    /**
     * Gets the form to a CAccountElement
     *
     * @param   elem    Account element.
     */
    private void getDetailsForm(CAccountElement elem) {
        if (elem!=null) {
            elem.setKey(acctValueText.getText());
            elem.setName(acctNameText.getText());
            elem.setDescription(acctDescText.getText());
            // Account type
            if (acctTypeCombo.getSelectedIndex()>=0) {
                elem.setType(acctTypeCombo.getSelectedItem().toString());
            } else {
                elem.setType("");
            }
            // Account sign
            if (acctSignCombo.getSelectedIndex()>=0) {
                elem.setSign(acctSignCombo.getSelectedItem().toString());
            } else {
                elem.setSign("");
            }
            elem.setDocument(docCtlCheck.isSelected());
            elem.setSummary(summaryAcctCheck.isSelected());
            
            // Parent account
            // TODO Set parent account
            
            elem.setBalanceSheet(balanceSheetText.getText());
            elem.setBalanceSheetName(balanceSheetNameText.getText());
            elem.setUs1120sheet(us1120balanceSheetText.getText());
            elem.setUs1120sheetName(us1120balanceSheetNameText.getText());
            elem.setProfitLoss(pnlText.getText());
            elem.setProfitLossName(pnlNameText.getText());
            elem.setUs1120incomeStmt(us1120incomeStmtText.getText());
            elem.setUs1120incomeStmtName(us1120incomeStmtNameText.getText());
            elem.setCashFlow(cashFlowText.getText());
            elem.setCashFlowName(cashFlowNameText.getText());
        }
    }

    /**
     * Helper method to get the default accounts combo box value
     */
    private String getDefaultAccountComboValue() {
        String value = "";
        // Default account
        if (defaultAcctCombo.getSelectedIndex()>0) {
            CDefaultAccount defAcct = (CDefaultAccount)defaultAcctCombo.getSelectedItem();
            value = defAcct.getDefaultAcct();
        }
        return(value);
    }

    /**
     * Helper method to set default accounts combo box value
     *
     * @param   defaultAccount  The default account key to set the combo box to.
     */
    private void setComboFromDefaultAccount(String defaultAccount) {
        if (defaultAccount==null || defaultAccount.trim().length()==0) {
            defaultAcctCombo.setSelectedIndex(0);
        } else {
            defaultAcctCombo.setSelectedItem(m_defaultAccountTree.get(defaultAccount));
        }
    }

    /**
     * Sets the default account of the account element elem to the current value
     * of the combobox.
     * 
     * @param   elem    The account element to set the default account for.
     */
    private void setDefaultAccountFromCombo(CAccountElement elem) {
        // Default account
        String newValue = getDefaultAccountComboValue();
        CDefAcctTableModel tModel = (CDefAcctTableModel)defAcctTable.getModel();
        tModel.setAssignment(elem,newValue);
        elem.setDefaultAccount(newValue);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        mainToolBar = new javax.swing.JToolBar();
        saveButton = new javax.swing.JButton();
        mainPanel = new javax.swing.JPanel();
        mainSplitPanePanel = new javax.swing.JSplitPane();
        leftSplitPanePanel = new javax.swing.JPanel();
        accountTreeScrollPane = new javax.swing.JScrollPane();
        accountTree = new JTree(m_treeRoot);
        accountTreeBottomPanel = new javax.swing.JPanel();
        newAccountButton = new javax.swing.JButton();
        removeAccountButton = new javax.swing.JButton();
        rightSplitPanePanel = new javax.swing.JPanel();
        tabbedPanePanel = new javax.swing.JTabbedPane();
        tabbedPaneMainPanel = new javax.swing.JPanel();
        detailsPanel = new javax.swing.JPanel();
        docCtlCheck = new javax.swing.JCheckBox();
        summaryAcctLabel = new javax.swing.JLabel();
        summaryAcctCheck = new javax.swing.JCheckBox();
        defaultAccountLabel = new javax.swing.JLabel();
        defaultAcctCombo = new JComboBox(m_defaultAccountVector);
        acctParentLabel = new javax.swing.JLabel();
        balanceSheetLabel = new javax.swing.JLabel();
        balanceSheetText = new javax.swing.JTextField();
        balanceSheetNameLabel = new javax.swing.JLabel();
        balanceSheetNameText = new javax.swing.JTextField();
        us1120balanceSheetLabel = new javax.swing.JLabel();
        us1120balanceSheetText = new javax.swing.JTextField();
        us1120balanceSheetNameLabel = new javax.swing.JLabel();
        us1120balanceSheetNameText = new javax.swing.JTextField();
        pnlLabel = new javax.swing.JLabel();
        pnlText = new javax.swing.JTextField();
        pnlNameLabel = new javax.swing.JLabel();
        pnlNameText = new javax.swing.JTextField();
        us1120incomeStmtLabel = new javax.swing.JLabel();
        us1120incomeStmtText = new javax.swing.JTextField();
        us1120incomeStmtNameLabel = new javax.swing.JLabel();
        us1120incomeStmtNameText = new javax.swing.JTextField();
        cashFlowLabel = new javax.swing.JLabel();
        cashFlowText = new javax.swing.JTextField();
        cashFlowNameLabel = new javax.swing.JLabel();
        cashFlowNameText = new javax.swing.JTextField();
        acctParentText = new javax.swing.JTextField();
        docCtlLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        acctValueLabel = new javax.swing.JLabel();
        acctNameLabel = new javax.swing.JLabel();
        acctNameText = new javax.swing.JTextField();
        acctValueText = new javax.swing.JTextField();
        acctDescLabel = new javax.swing.JLabel();
        acctDescText = new javax.swing.JTextField();
        acctTypeLabel = new javax.swing.JLabel();
        acctTypeCombo = new JComboBox(CAccountType.m_accountTypes);
        acctSignLabel = new javax.swing.JLabel();
        acctSignCombo = new javax.swing.JComboBox();
        accountListPanel = new javax.swing.JPanel();
        accountListBgPanel = new javax.swing.JPanel();
        accountListScrollPane = new javax.swing.JScrollPane();
        accountListTable = new javax.swing.JTable();
        defAcctPanel = new javax.swing.JPanel();
        defAcctBasePanel = new javax.swing.JPanel();
        defAcctScrollPane = new javax.swing.JScrollPane();
        defAcctTable = new javax.swing.JTable();
        mainMenuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        fileSeparator = new javax.swing.JSeparator();
        closeMenuItem = new javax.swing.JMenuItem();
        actionMenu = new javax.swing.JMenu();
        purgeMenuItem = new javax.swing.JMenuItem();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        saveButton.setAction(actionSave);
        saveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/se/navitech/adempiere/icons/save-16.png")));
        saveButton.setText("Save");
        saveButton.setToolTipText("Save the file");
        mainToolBar.add(saveButton);

        getContentPane().add(mainToolBar, java.awt.BorderLayout.NORTH);

        mainPanel.setLayout(new java.awt.BorderLayout());

        mainSplitPanePanel.setDividerLocation(350);
        leftSplitPanePanel.setLayout(new java.awt.BorderLayout());

        leftSplitPanePanel.setMinimumSize(new java.awt.Dimension(250, 322));
        leftSplitPanePanel.setPreferredSize(new java.awt.Dimension(250, 322));
        accountTree.setCellRenderer(new CAccountTreeCellRenderer());
        accountTree.setRowHeight(24);
        accountTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                accountTreeValueChanged(evt);
            }
        });
        accountTree.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tableKeyPressed(evt);
            }
        });

        accountTreeScrollPane.setViewportView(accountTree);

        leftSplitPanePanel.add(accountTreeScrollPane, java.awt.BorderLayout.CENTER);

        newAccountButton.setAction(actionAdd);
        newAccountButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/se/navitech/adempiere/icons/new.png")));
        newAccountButton.setMnemonic('n');
        newAccountButton.setText("New account");
        newAccountButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

        removeAccountButton.setAction(actionDelete);
        removeAccountButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/se/navitech/adempiere/icons/remove.png")));
        removeAccountButton.setText("Remove account");
        removeAccountButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

        org.jdesktop.layout.GroupLayout accountTreeBottomPanelLayout = new org.jdesktop.layout.GroupLayout(accountTreeBottomPanel);
        accountTreeBottomPanel.setLayout(accountTreeBottomPanelLayout);
        accountTreeBottomPanelLayout.setHorizontalGroup(
            accountTreeBottomPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(accountTreeBottomPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(newAccountButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 107, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(removeAccountButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 112, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(114, Short.MAX_VALUE))
        );
        accountTreeBottomPanelLayout.setVerticalGroup(
            accountTreeBottomPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(accountTreeBottomPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(newAccountButton)
                .add(removeAccountButton))
        );
        leftSplitPanePanel.add(accountTreeBottomPanel, java.awt.BorderLayout.SOUTH);

        mainSplitPanePanel.setLeftComponent(leftSplitPanePanel);

        rightSplitPanePanel.setLayout(new java.awt.BorderLayout());

        tabbedPaneMainPanel.setLayout(new java.awt.BorderLayout());

        docCtlCheck.setText("Yes");

        summaryAcctLabel.setText("Summary account");
        summaryAcctLabel.setToolTipText("Yes indicates a summary account (i.e. cannot have balances or be posted to)");

        summaryAcctCheck.setText("Yes");

        defaultAccountLabel.setText("Default account");

        defaultAcctCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultAcctComboActionPerformed(evt);
            }
        });

        acctParentLabel.setText("Account parent");
        acctParentLabel.setToolTipText("Key of the parent account (needs to be a summary account)");

        balanceSheetLabel.setText("Balance Sheet");
        balanceSheetLabel.setToolTipText("The line key of the report line.");

        balanceSheetNameLabel.setText("Balance Sheet name");
        balanceSheetNameLabel.setToolTipText("The line name of the report line. The first occurence is used only for new lines.");

        us1120balanceSheetLabel.setText("US 1120 Balance Sheet");
        us1120balanceSheetLabel.setToolTipText("Line number of a US tax declaration report.");

        us1120balanceSheetNameLabel.setText("US 1120 Balance Sheet Name");

        pnlLabel.setText("Profit & Loss");

        pnlNameLabel.setText("Profit & Loss Name");

        us1120incomeStmtLabel.setText("US 1120 Income Stmt");

        us1120incomeStmtNameLabel.setText("US 1120 Income Stmt Name");

        cashFlowLabel.setText("Cash Flow");

        cashFlowNameLabel.setText("Cash Flow Name");

        acctParentText.setEditable(false);

        docCtlLabel.setText("Document controlled");
        docCtlLabel.setToolTipText("If document controlled, no manual posting is allowed.");

        org.jdesktop.layout.GroupLayout detailsPanelLayout = new org.jdesktop.layout.GroupLayout(detailsPanel);
        detailsPanel.setLayout(detailsPanelLayout);
        detailsPanelLayout.setHorizontalGroup(
            detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(detailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(detailsPanelLayout.createSequentialGroup()
                        .add(defaultAccountLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
                        .add(340, 340, 340))
                    .add(detailsPanelLayout.createSequentialGroup()
                        .add(balanceSheetLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
                        .add(291, 291, 291))
                    .add(detailsPanelLayout.createSequentialGroup()
                        .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                .add(cashFlowNameLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(cashFlowLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(us1120incomeStmtNameLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(us1120incomeStmtLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(pnlNameLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .add(pnlLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(pnlText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 221, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(pnlNameText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 221, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(us1120incomeStmtText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 221, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(us1120incomeStmtNameText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 221, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(cashFlowNameText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 221, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(cashFlowText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 221, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 191, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, detailsPanelLayout.createSequentialGroup()
                        .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(detailsPanelLayout.createSequentialGroup()
                                .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(detailsPanelLayout.createSequentialGroup()
                                        .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(detailsPanelLayout.createSequentialGroup()
                                                .add(docCtlLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                                                .add(23, 23, 23))
                                            .add(detailsPanelLayout.createSequentialGroup()
                                                .add(summaryAcctLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                                        .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(summaryAcctCheck, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 221, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(docCtlCheck, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 221, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                    .add(detailsPanelLayout.createSequentialGroup()
                                        .add(acctParentLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(balanceSheetText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 221, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(acctParentText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(defaultAcctCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 286, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                                    .add(detailsPanelLayout.createSequentialGroup()
                                        .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                            .add(balanceSheetNameText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 221, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(detailsPanelLayout.createSequentialGroup()
                                                .add(us1120balanceSheetLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(us1120balanceSheetText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 221, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                        .add(6, 6, 6)))
                                .add(429, 429, 429))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, detailsPanelLayout.createSequentialGroup()
                                .add(us1120balanceSheetNameLabel)
                                .add(15, 15, 15)
                                .add(us1120balanceSheetNameText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 221, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .add(47, 47, 47))
                    .add(balanceSheetNameLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 109, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(30, 30, 30))
        );
        detailsPanelLayout.setVerticalGroup(
            detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(detailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(docCtlLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(docCtlCheck, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(summaryAcctLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(summaryAcctCheck, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(defaultAccountLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(defaultAcctCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(acctParentLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(acctParentText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(balanceSheetLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(balanceSheetText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(8, 8, 8)
                .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(balanceSheetNameText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(balanceSheetNameLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(us1120balanceSheetLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(us1120balanceSheetText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(us1120balanceSheetNameLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(us1120balanceSheetNameText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(pnlText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(pnlLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(pnlNameLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(pnlNameText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(us1120incomeStmtLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(us1120incomeStmtText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(us1120incomeStmtNameLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(us1120incomeStmtNameText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cashFlowLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(cashFlowText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(detailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cashFlowNameLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(cashFlowNameText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(41, 41, 41))
        );
        tabbedPaneMainPanel.add(detailsPanel, java.awt.BorderLayout.CENTER);

        acctValueLabel.setText("Account value");

        acctNameLabel.setText("Account name");

        acctDescLabel.setText("Account description");

        acctTypeLabel.setText("Account type");

        acctSignLabel.setText("Account sign");

        acctSignCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", "Natural", "Debit", "Credit" }));

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(acctValueLabel)
                            .add(acctNameLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE)
                            .add(acctDescLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(acctTypeLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 66, Short.MAX_VALUE)
                        .add(34, 34, 34))
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(acctSignLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE)
                        .add(37, 37, 37)))
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(acctNameText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 221, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(acctValueText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 99, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(acctDescText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 221, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(acctTypeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 221, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(acctSignCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 221, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(84, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(acctValueLabel)
                    .add(acctValueText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(acctNameText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(acctNameLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(acctDescLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(acctDescText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(acctTypeLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(acctTypeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(acctSignLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(acctSignCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );
        tabbedPaneMainPanel.add(jPanel2, java.awt.BorderLayout.NORTH);

        tabbedPanePanel.addTab("Details", tabbedPaneMainPanel);

        accountListPanel.setLayout(new java.awt.BorderLayout());

        accountListBgPanel.setLayout(new java.awt.BorderLayout());

        accountListTable.setModel(new CAccountListTableModel(m_schemaFile.getSchema()));
        accountListScrollPane.setViewportView(accountListTable);

        accountListBgPanel.add(accountListScrollPane, java.awt.BorderLayout.CENTER);

        accountListPanel.add(accountListBgPanel, java.awt.BorderLayout.CENTER);

        tabbedPanePanel.addTab("List of accounts", accountListPanel);

        defAcctBasePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Assigned default accounts"));
        defAcctTable.setModel(m_defAcctTableModel);
        defAcctScrollPane.setViewportView(defAcctTable);

        org.jdesktop.layout.GroupLayout defAcctBasePanelLayout = new org.jdesktop.layout.GroupLayout(defAcctBasePanel);
        defAcctBasePanel.setLayout(defAcctBasePanelLayout);
        defAcctBasePanelLayout.setHorizontalGroup(
            defAcctBasePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(defAcctScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
        );
        defAcctBasePanelLayout.setVerticalGroup(
            defAcctBasePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(defAcctScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 513, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout defAcctPanelLayout = new org.jdesktop.layout.GroupLayout(defAcctPanel);
        defAcctPanel.setLayout(defAcctPanelLayout);
        defAcctPanelLayout.setHorizontalGroup(
            defAcctPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(defAcctPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(defAcctBasePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        defAcctPanelLayout.setVerticalGroup(
            defAcctPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(defAcctPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(defAcctBasePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        tabbedPanePanel.addTab("Default accounts", defAcctPanel);

        rightSplitPanePanel.add(tabbedPanePanel, java.awt.BorderLayout.CENTER);

        mainSplitPanePanel.setRightComponent(rightSplitPanePanel);

        mainPanel.add(mainSplitPanePanel, java.awt.BorderLayout.CENTER);

        getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);

        fileMenu.setText("File");
        saveMenuItem.setAction(actionSave);
        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setText("Save as ...");
        saveAsMenuItem.setToolTipText("Saves file with a different name");
        saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(saveAsMenuItem);

        fileMenu.add(fileSeparator);

        closeMenuItem.setMnemonic('C');
        closeMenuItem.setText("Close");
        closeMenuItem.setToolTipText("Close this file.");
        closeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(closeMenuItem);

        mainMenuBar.add(fileMenu);

        actionMenu.setText("Actions");
        purgeMenuItem.setText("Purge accounts...");
        purgeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                purgeMenuItemActionPerformed(evt);
            }
        });

        actionMenu.add(purgeMenuItem);

        mainMenuBar.add(actionMenu);

        setJMenuBar(mainMenuBar);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-782)/2, (screenSize.height-664)/2, 782, 664);
    }// </editor-fold>//GEN-END:initComponents

    /**
     *  Method called when the default account combo box value is changed.
     */    
    private void defaultAcctComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultAcctComboActionPerformed

        if (!m_updateProgress & m_currentAccount!=null) {
            // See if any other account has this default account
            CAccountElement elem = m_schemaFile.getSchema().getDefAcctAssignments().get(getDefaultAccountComboValue());
            if (elem!=null) {
                int result = JOptionPane.showConfirmDialog(this, 
                        "Account " + elem.getKey() + " is currently default account for\n" +
                        "Do you want to change this to " + m_currentAccount.getKey(),
                        "Change default account",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (result==JOptionPane.YES_OPTION) {
                    elem.setDefaultAccount("");
                } else {
                    setComboFromDefaultAccount(m_currentAccount.getDefaultAccount());
                    return;
                }
            }
            setDefaultAccountFromCombo(m_currentAccount);
            setModified(true);
        }
        
    }//GEN-LAST:event_defaultAcctComboActionPerformed
    
    private void tableKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tableKeyPressed
        
        if ((evt.getKeyCode() & evt.VK_DELETE)==evt.VK_DELETE) {
            actionDelete.actionPerformed(new ActionEvent(accountTree, 0, "Remove account"));
        }
        
    }//GEN-LAST:event_tableKeyPressed
    
    private void purgeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_purgeMenuItemActionPerformed
        
        purgeAccountSchema();
        
    }//GEN-LAST:event_purgeMenuItemActionPerformed

    /**
     * Purges the account schema from all non default accounts. The tree structure
     * is left intact.
     */
    private void purgeAccountSchema() {
        
        int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to remove all non-default accounts?",
                "Remove non-default accounts?", JOptionPane.YES_NO_OPTION);
        
        if (result==JOptionPane.YES_OPTION) {
            m_schemaFile.getSchema().makeMinimal();
            refreshAll();
        }
        
    }

    /**
     * Refreshes the entire tree.
     */
    private void refreshAll() {
        m_accountTree = m_schemaFile.getSchema().getAccountTree();
        m_treeRoot = createTreeNodes();
        accountTree.setModel(new javax.swing.tree.DefaultTreeModel(m_treeRoot));
    }
    
    private void closeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeMenuItemActionPerformed

        // See if the user wants to save the file
        if (m_modified) {
            int result = JOptionPane.showConfirmDialog(this, 
                    "The file has not been saved. Do you want to\n" +
                    "discard the changes?", "Close without save?", 
                    JOptionPane.OK_CANCEL_OPTION);
            if (result==JOptionPane.CANCEL_OPTION) {
                return;
            }
        }
        this.setVisible(false);
        this.dispose();
        
    }//GEN-LAST:event_closeMenuItemActionPerformed
    
    private void saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsMenuItemActionPerformed

        // Save if necessary
        if (m_currentAccount!=null) {
            getDetailsForm(m_currentAccount);
        }

        // Find the last used directory.
        File dir = CAdempiereAcctEdit.getCurrentDirectory();
        
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        if (dir!=null) {
            fc.setCurrentDirectory(dir);
        }
        fc.setDialogTitle("Save file as");
        int result = fc.showSaveDialog(this);
        if (result==fc.APPROVE_OPTION) {
            // Save the file
            try {
                m_schemaFile.saveFile(fc.getSelectedFile());
                setModified(false);
                m_file = fc.getSelectedFile();
                // Set name of root
                DefaultTreeModel treeModel = (DefaultTreeModel)accountTree.getModel();
                m_treeRoot.setUserObject(m_file.getName());
                treeModel.nodeChanged(m_treeRoot);
                setTitle(m_file.getAbsolutePath());
                actionSave.setEnabled(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage());
            }
        }
        
    }//GEN-LAST:event_saveAsMenuItemActionPerformed
    
    private void accountTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_accountTreeValueChanged

        // Save the last selected
        if (m_currentAccount!=null) {
            getDetailsForm(m_currentAccount);
        }
        
        // Find what node is selected
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)accountTree.getLastSelectedPathComponent();
        if (node!=null) {
            Object obj = node.getUserObject();
            if (obj instanceof CAccountElement) {
                CAccountElement selected = (CAccountElement)obj;
                m_currentAccount = selected;
                setDetailsForm(m_currentAccount);
            }
        }
        
    }//GEN-LAST:event_accountTreeValueChanged
    
    /**
     * Sets this section to modified
     */
    public void setModified(boolean flag) {
        if (!isVisible()) return;
        if (flag && !m_modified) {
            m_modified = true;
            setTitle(getTitle()+ " *");
            // Enable save button
            actionSave.setEnabled(true);
        }
        if (!flag) {
            m_modified = false;
            // Remove modified indicator
            if (getTitle()!=null && getTitle().endsWith(" *")) {
                setTitle(getTitle().substring(0, getTitle().length()-2));
            }
            // Disable save changes button
            actionSave.setEnabled(false);
        }
    }
    
    public void keyPressed(KeyEvent e) {
    }
    
    public void keyReleased(KeyEvent e) {
    }
    
    public void stateChanged(ChangeEvent e) {
        setModified(true);
    }
    
    public void keyTyped(KeyEvent e) {
        setModified(true);
    }
    
    public void focusGained(FocusEvent e) {
    }
    
    public void focusLost(FocusEvent e) {
        Component comp = e.getComponent();
        if (m_modified==true) {
            getDetailsForm(m_currentAccount);
            
            if (comp==acctNameText || comp==acctDescText) {
                // Update the node in the three
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)accountTree.getLastSelectedPathComponent();
                ((DefaultTreeModel)accountTree.getModel()).nodeChanged(node);
            }
            
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel accountListBgPanel;
    private javax.swing.JPanel accountListPanel;
    private javax.swing.JScrollPane accountListScrollPane;
    private javax.swing.JTable accountListTable;
    private javax.swing.JTree accountTree;
    private javax.swing.JPanel accountTreeBottomPanel;
    private javax.swing.JScrollPane accountTreeScrollPane;
    private javax.swing.JLabel acctDescLabel;
    private javax.swing.JTextField acctDescText;
    private javax.swing.JLabel acctNameLabel;
    private javax.swing.JTextField acctNameText;
    private javax.swing.JLabel acctParentLabel;
    private javax.swing.JTextField acctParentText;
    private javax.swing.JComboBox acctSignCombo;
    private javax.swing.JLabel acctSignLabel;
    private javax.swing.JComboBox acctTypeCombo;
    private javax.swing.JLabel acctTypeLabel;
    private javax.swing.JLabel acctValueLabel;
    private javax.swing.JTextField acctValueText;
    private javax.swing.JMenu actionMenu;
    private javax.swing.JLabel balanceSheetLabel;
    private javax.swing.JLabel balanceSheetNameLabel;
    private javax.swing.JTextField balanceSheetNameText;
    private javax.swing.JTextField balanceSheetText;
    private javax.swing.JLabel cashFlowLabel;
    private javax.swing.JLabel cashFlowNameLabel;
    private javax.swing.JTextField cashFlowNameText;
    private javax.swing.JTextField cashFlowText;
    private javax.swing.JMenuItem closeMenuItem;
    private javax.swing.JPanel defAcctBasePanel;
    private javax.swing.JPanel defAcctPanel;
    private javax.swing.JScrollPane defAcctScrollPane;
    private javax.swing.JTable defAcctTable;
    private javax.swing.JLabel defaultAccountLabel;
    private javax.swing.JComboBox defaultAcctCombo;
    private javax.swing.JPanel detailsPanel;
    private javax.swing.JCheckBox docCtlCheck;
    private javax.swing.JLabel docCtlLabel;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JSeparator fileSeparator;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel leftSplitPanePanel;
    private javax.swing.JMenuBar mainMenuBar;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JSplitPane mainSplitPanePanel;
    private javax.swing.JToolBar mainToolBar;
    private javax.swing.JButton newAccountButton;
    private javax.swing.JLabel pnlLabel;
    private javax.swing.JLabel pnlNameLabel;
    private javax.swing.JTextField pnlNameText;
    private javax.swing.JTextField pnlText;
    private javax.swing.JMenuItem purgeMenuItem;
    private javax.swing.JButton removeAccountButton;
    private javax.swing.JPanel rightSplitPanePanel;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JButton saveButton;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JCheckBox summaryAcctCheck;
    private javax.swing.JLabel summaryAcctLabel;
    private javax.swing.JPanel tabbedPaneMainPanel;
    private javax.swing.JTabbedPane tabbedPanePanel;
    private javax.swing.JLabel us1120balanceSheetLabel;
    private javax.swing.JLabel us1120balanceSheetNameLabel;
    private javax.swing.JTextField us1120balanceSheetNameText;
    private javax.swing.JTextField us1120balanceSheetText;
    private javax.swing.JLabel us1120incomeStmtLabel;
    private javax.swing.JLabel us1120incomeStmtNameLabel;
    private javax.swing.JTextField us1120incomeStmtNameText;
    private javax.swing.JTextField us1120incomeStmtText;
    // End of variables declaration//GEN-END:variables
    
}
