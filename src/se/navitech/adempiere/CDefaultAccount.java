/*
 * CDefaultAccount.java
 *
 * Created on den 2 februari 2006, 19:00
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package se.navitech.adempiere;

/**
 *
 * @author Daniel Norin
 */
public class CDefaultAccount implements Comparable {
    
    /** Creates a new instance of CDefaultAccount */
    public CDefaultAccount() {
    }

    public CDefaultAccount(String key, String name) {
        m_defaultAcct = key;
        m_name = name;
    }
    
    public String toString() {
        if (m_defaultAcct==null || m_defaultAcct.trim().length()==0) {
            return("");
        }
        return(m_defaultAcct + " : " + m_name);
    }
    
    /**
     * Holds value of property defaultAcct.
     */
    private String m_defaultAcct;

    /**
     * Getter for property defaultAcct.
     * @return Value of property defaultAcct.
     */
    public String getDefaultAcct() {

        return m_defaultAcct;
    }

    /**
     * Setter for property defaultAcct.
     * @param defaultAcct New value of property defaultAcct.
     */
    public void setDefaultAcct(String defaultAcct) {

        m_defaultAcct = defaultAcct;
    }

    /**
     * Holds value of property getName.
     */
    private String m_name;

    /**
     * Getter for property getName.
     * @return Value of property getName.
     */
    public String getName() {

        return m_name;
    }

    /**
     * Setter for property getName.
     * @param getName New value of property getName.
     */
    public void setName(String name) {

        m_name = name;
    }

    public int compareTo(Object o) {
        return(this.toString().compareTo(o.toString()));
    }
    
}
