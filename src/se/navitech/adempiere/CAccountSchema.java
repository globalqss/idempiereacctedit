/*
 * CAccountSchema.java
 *
 * Created on den 29 december 2005, 21:45
 *
 * Contains an account schema. The account schema consists of one or more
 * account elements.
 *
 */

package se.navitech.adempiere;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

/**
 *
 * @author Daniel Norin
 */
public class CAccountSchema {
    
    // Contains all account elements
    private SortedMap<String,CAccountElement>   m_elements;
    private SortedMap<String,SortedSet<String>> m_tree;
    private SortedSet<String>                   m_root;
    private SortedMap<String,CAccountElement>   m_defAcctAssignments;
    private SortedMap<CAccountElement,String>   m_defAcctAssignmentsReverse;
    private SortedMap<String,CDefaultAccount>   m_defaultAccounts;
    
    /** Creates a new instance of CAccountSchema */
    public CAccountSchema(SortedMap<String,CDefaultAccount> defaultAccounts) {
        m_elements = new TreeMap<String, CAccountElement>();
        m_root = new TreeSet<String>();
        m_tree = new TreeMap<String, SortedSet<String>>();
        m_defAcctAssignments = new TreeMap<String,CAccountElement>();
        m_defAcctAssignmentsReverse = new TreeMap<CAccountElement,String>();
        m_defaultAccounts = defaultAccounts;
    }
    
    /**
     * Adds an account element to the schema
     */
    public void addAccountElement(CAccountElement acct) {
        SortedSet<String> kids;
        m_elements.put(acct.getKey(), acct);
        String defAcct = acct.getDefaultAccount();
        if (defAcct!=null && defAcct.trim().length()>0 && acct!=null) {
            m_defAcctAssignments.put(defAcct.trim(), acct);
            m_defAcctAssignmentsReverse.put(acct, defAcct.trim());
        }
        if (!acct.hasParentKey()) {
            m_root.add(acct.getKey());
        } else {
            // It has parent
            // Get sorted map for parent
            kids = m_tree.get(acct.getParentKey());
            if (kids==null) {
                kids = new TreeSet<String>();
            }
            kids.add(acct.getKey());
            m_tree.put(acct.getParentKey(), kids);
        }
        
    }
    
    /**
     * Removes an account element from the schema. If the element has leaves,
     * the leaves will be removed also.
     */
    public void removeAccountElement(CAccountElement acct) {
        // Check if the account element is a default account,
        // if so, remove it.
        String defAcct = m_defAcctAssignmentsReverse.get(acct);
        if (defAcct!=null) {
            m_defAcctAssignments.remove(defAcct);
            m_defAcctAssignmentsReverse.remove(acct);
        }
        
        // See if it is in the root list
        m_root.remove(acct.getKey());

        // Remove from sibling list if it has a parent
        if (acct.getParentKey()!=null) {
            // Get parent
            SortedSet<String> siblings = m_tree.get(acct.getParentKey());
            // Remove the account from the sibling list
            if (siblings!=null) {
                siblings.remove(acct.getKey());
                m_tree.put(acct.getParentKey(), siblings);
            }
        }

        // Check if children needs to be removed
        SortedSet<String> children = m_tree.get(acct.getKey());
        if (children!=null && children.size()>0) {
            Vector<CAccountElement> childrenV = new Vector<CAccountElement>();
            // Create a vector for removal.
            for (Iterator<String> it=children.iterator(); it.hasNext();) {
                childrenV.add(m_elements.get(it.next()));
            }
            // Iterate through the vector to remove this elements from the structure
            for (int i=0; i<childrenV.size(); i++) {
                removeAccountElement(childrenV.get(i));
            }
        }
        
        // Remove from elements
        m_elements.remove(acct.getKey());
        
    }
    
    /**
     * @return An account element
     */
    public CAccountElement getAccountElement(String key) {
        return(m_elements.get(key));
    }
    
    /**
     * @return  Root keys
     */
    public SortedSet<String> getRootKeys() {
        return(m_root);
    }

    /**
     * @return  Tree
     */
    public SortedMap<String, SortedSet<String>> getAccountTree() {
        return(m_tree);
    }
    
    /**
     * @return  Number of elements in the account schema
     */
    public int getElementCount() {
        return(m_elements.size());
    }
    
    /**
     * @return  All account elements
     */
    public SortedMap<String, CAccountElement> getElements() {
        return(m_elements);
    }
    
    /**
     * Default accounts map
     */
    public SortedMap<String, CAccountElement> getDefAcctAssignments() {
        return(m_defAcctAssignments);
    }
    
    /**
     * Default accounts
     */
    public SortedMap<String, CDefaultAccount> getDefaultAccounts() {
        return(m_defaultAccounts);
    }
    
    /**
     * @return  Children of the given account key. Can be null if there are no
     *          children.
     */
    public SortedSet<String> getChildren(String accountKey) {
        SortedSet<String> children = m_tree.get(accountKey);
        return(children);
    }
    
    /**
     * @return  True if the given account has any children
     */
    public boolean hasChildren(String accountKey) {
        SortedSet<String> children = getChildren(accountKey);
        return(children!=null && children.size()>0);
    }
    
    /**
     * Sets the type of the account elements recursively
     *
     * @param   elementKey
     * @param   type
     */
    public void setTypeRecursive(String elementKey, String type) {
        SortedSet<String> children = m_tree.get(elementKey);
        setTypeRecursive(children, type);
    }
    
    
    private void setTypeRecursive(SortedSet<String> children, String type) {
        String accountKey;
        if (children==null) return;
        for (Iterator<String> it = children.iterator(); it.hasNext();) {
            accountKey = it.next();
            SortedSet<String> grandChildren = m_tree.get(accountKey);
            if (grandChildren!=null && grandChildren.size()>0) {
                setTypeRecursive(grandChildren, type);
            }
            m_elements.get(accountKey).setType(type);
        }
    }
    
    /**
     * Removes all account leaves that is not tied to a default account
     */
    public void makeMinimal() {
        // Find all leaf accounts that has no default account and remove them
        Vector<CAccountElement> removeThese = new Vector<CAccountElement>();
        CAccountElement elem;
        for (Iterator<CAccountElement> it = m_elements.values().iterator(); it.hasNext(); ) {
            elem = it.next();
            if (!elem.isSummary() && (elem.getDefaultAccount()==null || elem.getDefaultAccount().trim().length()==0)) {
                removeThese.add(elem);
            }
        }
        
        // Remove the accounts on the list
        for (int i=0; i<removeThese.size(); i++) {
            removeAccountElement(removeThese.get(i));
        }
    }
    
}
