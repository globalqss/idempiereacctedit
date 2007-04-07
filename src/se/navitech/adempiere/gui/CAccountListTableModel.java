/*
 * CAccountListTableModel.java
 *
 * Created on den 4 februari 2006, 12:01
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package se.navitech.adempiere.gui;


import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import se.navitech.adempiere.CAccountElement;
import se.navitech.adempiere.CAccountSchema;

/**
 *
 * @author Daniel Norin
 */
public class CAccountListTableModel extends AbstractTableModel {
    
    private CAccountSchema  m_accountSchema;
    private SortedMap<String,CAccountElement>   m_elements;
    private Vector<String>                      m_keys;
    
    public String[] cols = {"Account id", "Name", "Type", "Sign", "Default account"};
    
    /** Creates a new instance of CAccountListTableModel */
    public CAccountListTableModel(CAccountSchema accountSchema) {
        m_accountSchema = accountSchema;
        refreshValuesFromSchema();
    }

    public void refreshValuesFromSchema() {
        m_elements = new TreeMap<String, CAccountElement>();
        // Only show the leafs
        SortedMap<String, CAccountElement> allElements = m_accountSchema.getElements();
        CAccountElement elem;
        for (Iterator<CAccountElement> it = allElements.values().iterator(); it.hasNext();) {
            elem = it.next();
            if (!elem.isSummary()) {
                m_elements.put(elem.getKey(), elem);
            }
        }
        m_keys = new Vector<String>();
        for (Iterator<String> it = m_elements.keySet().iterator(); it.hasNext();) {
            m_keys.add(it.next());
        }
        fireTableDataChanged();        
    }
    
    public String getColumnName(int col) {
        return(cols[col]);
    }
    
    public int getColumnCount() {
        return(cols.length);
    }

    public int getRowCount() {
        return(m_elements.size());
    }

    public Object getValueAt(int row, int col) {
        CAccountElement elem = m_elements.get(m_keys.get(row));
        switch (col) {
            case 0: return(elem.getKey());
            case 1: return(elem.getName());
            case 2: return(elem.getType());
            case 3: return(elem.getSign());
            case 4: return(elem.getDefaultAccount());
        }
        return null;
    }
    
}
