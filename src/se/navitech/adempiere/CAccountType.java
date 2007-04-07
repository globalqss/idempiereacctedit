/*
 * CAccountType.java
 *
 * Created on den 9 januari 2006, 20:22
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package se.navitech.adempiere;

import java.util.*;

/**
 *
 * @author Daniel Norin
 */
public class CAccountType {

    public static Vector<String> m_accountTypes = new Vector();
    
    static {
        m_accountTypes.add("");
        m_accountTypes.add("Asset");
        m_accountTypes.add("Liability");
        m_accountTypes.add("Owner's equity");
        m_accountTypes.add("Expense");
        m_accountTypes.add("Revenue");
        m_accountTypes.add("Memo");
    }
    
    /** Creates a new instance of CAccountType */
    public CAccountType() {
    }

    /**
     * Holds value of property key.
     */
    private String key;

    /**
     * Getter for property key.
     * @return Value of property key.
     */
    public String getKey() {

        return this.key;
    }

    /**
     * Setter for property key.
     * @param key New value of property key.
     */
    public void setKey(String key) {

        this.key = key;
    }

    /**
     * Holds value of property name.
     */
    private String name;

    /**
     * Getter for property name.
     * @return Value of property name.
     */
    public String getName() {

        return this.name;
    }

    /**
     * Setter for property name.
     * @param name New value of property name.
     */
    public void setName(String name) {

        this.name = name;
    }

    /**
     * Holds value of property compiereKey.
     */
    private String compiereKey;

    /**
     * Getter for property compiereKey.
     * @return Value of property compiereKey.
     */
    public String getCompiereKey() {

        return this.compiereKey;
    }

    /**
     * Setter for property compiereKey.
     * @param compiereKey New value of property compiereKey.
     */
    public void setCompiereKey(String compiereKey) {

        this.compiereKey = compiereKey;
    }
    
}
