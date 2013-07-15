/*
 * CAccountTreeCellRenderer.java
 *
 * Created on den 9 februari 2006, 09:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package se.navitech.adempiere.gui;

import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import se.navitech.adempiere.CAccountElement;

/**
 *
 * @author Daniel Norin
 */
public class CAccountTreeCellRenderer extends DefaultTreeCellRenderer {
    /**
	 * 
	 */
	private static final long serialVersionUID = -2714424722193416943L;

	ImageIcon m_accountGroupIcon;
    ImageIcon m_accountIcon;
    DefaultMutableTreeNode node;

    private static Font normal = new Font("Dialog", Font.PLAIN, 12);
    
    /** Creates a new instance of CAccountTreeCellRenderer */
    public CAccountTreeCellRenderer() {
        readIcons();
    }

    public Image getImage(String name) {
        // Load image from file
        java.net.URL url = ClassLoader.getSystemClassLoader().getResource(name);
        Image image = Toolkit.getDefaultToolkit().getImage(url);
        return(image);
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
                                                  boolean expanded, boolean leaf, int row,
                                                  boolean hasFocus) {
          
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        node = (DefaultMutableTreeNode)value;
        Object obj = node.getUserObject();
        setFont(normal);
        if (obj instanceof CAccountElement) {
            CAccountElement elem = (CAccountElement)obj;
            try {
                setText(elem.toString());
                setToolTipText(elem.getDescription());
            } catch (Exception e) {
                setText(e.getMessage());
            }
            if (elem.isSummary()) {
                setIcon(m_accountGroupIcon);
            } else {
                setIcon(m_accountIcon);
            }
        }
        return(this);
    }
    
    /**
     * Reads the icons to use with this renderer
     */
    private void readIcons() {
        // Create a mediatracker
        MediaTracker tracker = new MediaTracker(this);
        // Load image from file
        Image accountGroupImage = getImage("se/navitech/adempiere/icons/accountGroupIcon.gif");
        Image accountImage = getImage("se/navitech/adempiere/icons/accountIcon.gif");
        
        tracker.addImage(accountGroupImage, 0);
        tracker.addImage(accountImage, 1);
        try {
            tracker.waitForAll();
        } catch (InterruptedException ie) {
        }
        m_accountGroupIcon = new ImageIcon(accountGroupImage);
        m_accountIcon = new ImageIcon(accountImage);
    }
    
}
