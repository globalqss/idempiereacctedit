/*
 * CDefAcctTableModel.java
 *
 * Created on den 7 februari 2006, 10:35
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package se.navitech.adempiere.gui;

import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import se.navitech.adempiere.CAccountElement;
import se.navitech.adempiere.CAccountSchema;
import se.navitech.adempiere.CDefaultAccount;

/**
 *
 * @author Daniel Norin
 */
public class CDefAcctTableModel extends AbstractTableModel {
    
    private CAccountSchema  m_accountSchema;
    private SortedMap<String, CDefaultAccount>          m_defAccts;
    private SortedMap<String,CAccountElement>  m_defAcctAssign;
    private Vector<CDefaultAccount>                     m_elements;
    private Vector<CDefaultAccount>                     m_keys;

    public String[] cols = {"Default account", "Name", "Account id", "Description"};
    
    /** Creates a new instance of CDefAcctTableModel */
    public CDefAcctTableModel(CAccountSchema accountSchema) {
        m_accountSchema = accountSchema;
        refreshValuesFromSchema();
    }

    public void refreshValuesFromSchema() {
        m_defAccts = m_accountSchema.getDefaultAccounts();
        m_defAcctAssign = m_accountSchema.getDefAcctAssignments();
        m_elements = new Vector<CDefaultAccount>(m_defAccts.values());
        fireTableDataChanged();
    }
    
    public void setAssignment(CAccountElement elem, String defAcct) {
        if (defAcct==null || defAcct.trim().length()==0) {
            // Remove assignment
            String previous = elem.getDefaultAccount();
            if (previous!=null) {
                m_defAcctAssign.remove(previous);
                refreshValuesFromSchema();
            }
        } else {
            // Add assignment
            m_defAcctAssign.put(defAcct, elem);
            refreshValuesFromSchema();
        }
    }
    
    public String getColumnName(int col) {
        return(cols[col]);
    }
    
    public int getRowCount() {
        return(m_elements.size());
    }

    public int getColumnCount() {
        return(cols.length);
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        CDefaultAccount defAcct = m_elements.get(rowIndex);
        CAccountElement elem;
        switch(columnIndex) {
            case 0: return(defAcct.getDefaultAcct());
            case 1: return(defAcct.getName());
            case 2:
            case 3:
                elem = m_defAcctAssign.get(defAcct.getDefaultAcct());
                if (elem==null) return("");
                if (columnIndex==2) return(elem.getKey());
                if (columnIndex==3) return(elem.getName());
        }
        return(null);
    }
    
}
