/*
 * CAccountElement.java
 *
 * Created on den 29 december 2005, 20:59
 *
 * Represents an account element.
 *
 */

package se.navitech.adempiere;

import java.util.*;

/**
 *
 * @author Daniel Norin
 */
public class CAccountElement implements Comparable {
    
    /** Creates a new instance of CAccountElement */
    public CAccountElement() {
    }

    public CAccountElement(StringBuffer[] cols) throws Exception {
        if (cols.length<19) {
            throw new Exception("There are only " + cols.length + " columns. Expected 19.");
        }
        // Make sure no columns are null
        for (int i=0; i<19; i++) {
            if (cols[i]==null) cols[i]=new StringBuffer("");
        }
        int n=0;
        setKey(cols[n++].toString());
        setName(cols[n++].toString());
        setDescription(cols[n++].toString());
        setType(cols[n++].toString());
        setSign(cols[n++].toString());
        setDocument("Yes".equalsIgnoreCase(cols[n++].toString()));
        setSummary("Yes".equalsIgnoreCase(cols[n++].toString()));
        setDefaultAccount(cols[n++].toString());
        setParentKey(cols[n++].toString());
        setBalanceSheet(cols[n++].toString());
        setBalanceSheetName(cols[n++].toString());
        setUs1120sheet(cols[n++].toString());
        setUs1120sheetName(cols[n++].toString());
        setProfitLoss(cols[n++].toString());
        setProfitLossName(cols[n++].toString());
        setUs1120incomeStmt(cols[n++].toString());
        setUs1120incomeStmtName(cols[n++].toString());
        setCashFlow(cols[n++].toString());
        setCashFlowName(cols[n++].toString());
    }
    
    
    public String toString() {
        return("[ " + key + " ] " + name);
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
     * Holds value of property description.
     */
    private String description;

    /**
     * Getter for property description.
     * @return Value of property description.
     */
    public String getDescription() {

        return this.description;
    }

    /**
     * Setter for property description.
     * @param description New value of property description.
     */
    public void setDescription(String description) {

        this.description = description;
    }

    /**
     * Holds value of property type.
     */
    private String type;

    /**
     * Getter for property type.
     * @return Value of property type.
     */
    public String getType() {

        return this.type;
    }

    /**
     * Setter for property type.
     * @param type New value of property type.
     */
    public void setType(String type) {

        this.type = type;
    }

    /**
     * Holds value of property sign.
     */
    private String sign;

    /**
     * Getter for property sign.
     * @return Value of property sign.
     */
    public String getSign() {

        return this.sign;
    }

    /**
     * Setter for property sign.
     * @param sign New value of property sign.
     */
    public void setSign(String sign) {

        this.sign = sign;
    }

    /**
     * Holds value of property document.
     */
    private boolean document;

    /**
     * Getter for property document.
     * @return Value of property document.
     */
    public boolean isDocument() {

        return this.document;
    }

    /**
     * Setter for property document.
     * @param document New value of property document.
     */
    public void setDocument(boolean document) {

        this.document = document;
    }

    /**
     * Holds value of property summary.
     */
    private boolean summary;

    /**
     * Getter for property summary.
     * @return Value of property summary.
     */
    public boolean isSummary() {

        return this.summary;
    }

    /**
     * Setter for property summary.
     * @param summary New value of property summary.
     */
    public void setSummary(boolean summary) {

        this.summary = summary;
    }

    /**
     * Holds value of property defaultAccount.
     */
    private String defaultAccount;

    /**
     * Getter for property defaultAccount.
     * @return Value of property defaultAccount.
     */
    public String getDefaultAccount() {

        return this.defaultAccount;
    }

    /**
     * Setter for property defaultAccount.
     * @param defaultAccount New value of property defaultAccount.
     */
    public void setDefaultAccount(String defaultAccount) {

        this.defaultAccount = defaultAccount;
    }

    /**
     * Holds value of property parentKey.
     */
    private String parentKey;

    /**
     * Getter for property parentKey.
     * @return Value of property parentKey.
     */
    public String getParentKey() {

        return this.parentKey;
    }

    /**
     * Setter for property parentKey.
     * @param parentKey New value of property parentKey.
     */
    public void setParentKey(String parentKey) {

        this.parentKey = parentKey;
    }

    /**
     * @return  True if this account has a parent
     */
    public boolean hasParentKey() {
        return(parentKey!=null && parentKey.trim().length()>0);
    }
    
    /**
     * Holds value of property balanceSheet.
     */
    private String balanceSheet;

    /**
     * Getter for property balanceSheet.
     * @return Value of property balanceSheet.
     */
    public String getBalanceSheet() {

        return this.balanceSheet;
    }

    /**
     * Setter for property balanceSheet.
     * @param balanceSheet New value of property balanceSheet.
     */
    public void setBalanceSheet(String balanceSheet) {

        this.balanceSheet = balanceSheet;
    }

    /**
     * Holds value of property balanceSheetName.
     */
    private String balanceSheetName;

    /**
     * Getter for property balanceSheetName.
     * @return Value of property balanceSheetName.
     */
    public String getBalanceSheetName() {

        return this.balanceSheetName;
    }

    /**
     * Setter for property balanceSheetName.
     * @param balanceSheetName New value of property balanceSheetName.
     */
    public void setBalanceSheetName(String balanceSheetName) {

        this.balanceSheetName = balanceSheetName;
    }

    /**
     * Holds value of property us1120sheet.
     */
    private String us1120sheet;

    /**
     * Getter for property us1120sheet.
     * @return Value of property us1120sheet.
     */
    public String getUs1120sheet() {

        return this.us1120sheet;
    }

    /**
     * Setter for property us1120sheet.
     * @param us1120sheet New value of property us1120sheet.
     */
    public void setUs1120sheet(String us1120sheet) {

        this.us1120sheet = us1120sheet;
    }

    /**
     * Holds value of property us1120sheetName.
     */
    private String us1120sheetName;

    /**
     * Getter for property us1120sheetName.
     * @return Value of property us1120sheetName.
     */
    public String getUs1120sheetName() {

        return this.us1120sheetName;
    }

    /**
     * Setter for property us1120sheetName.
     * @param us1120sheetName New value of property us1120sheetName.
     */
    public void setUs1120sheetName(String us1120sheetName) {

        this.us1120sheetName = us1120sheetName;
    }

    /**
     * Holds value of property profitLoss.
     */
    private String profitLoss;

    /**
     * Getter for property profitLoss.
     * @return Value of property profitLoss.
     */
    public String getProfitLoss() {

        return this.profitLoss;
    }

    /**
     * Setter for property profitLoss.
     * @param profitLoss New value of property profitLoss.
     */
    public void setProfitLoss(String profitLoss) {

        this.profitLoss = profitLoss;
    }

    /**
     * Holds value of property profitLossName.
     */
    private String profitLossName;

    /**
     * Getter for property profitLossName.
     * @return Value of property profitLossName.
     */
    public String getProfitLossName() {

        return this.profitLossName;
    }

    /**
     * Setter for property profitLossName.
     * @param profitLossName New value of property profitLossName.
     */
    public void setProfitLossName(String profitLossName) {

        this.profitLossName = profitLossName;
    }

    /**
     * Holds value of property us1120incomeStmt.
     */
    private String us1120incomeStmt;

    /**
     * Getter for property us1120incomeStmt.
     * @return Value of property us1120incomeStmt.
     */
    public String getUs1120incomeStmt() {

        return this.us1120incomeStmt;
    }

    /**
     * Setter for property us1120incomeStmt.
     * @param us1120incomeStmt New value of property us1120incomeStmt.
     */
    public void setUs1120incomeStmt(String us1120incomeStmt) {

        this.us1120incomeStmt = us1120incomeStmt;
    }

    /**
     * Holds value of property us1120incomeStmtName.
     */
    private String us1120incomeStmtName;

    /**
     * Getter for property us1120incomeStmtName.
     * @return Value of property us1120incomeStmtName.
     */
    public String getUs1120incomeStmtName() {

        return this.us1120incomeStmtName;
    }

    /**
     * Setter for property us1120incomeStmtName.
     * @param us1120incomeStmtName New value of property us1120incomeStmtName.
     */
    public void setUs1120incomeStmtName(String us1120incomeStmtName) {

        this.us1120incomeStmtName = us1120incomeStmtName;
    }

    /**
     * Holds value of property cashFlow.
     */
    private String cashFlow;

    /**
     * Getter for property cashFlow.
     * @return Value of property cashFlow.
     */
    public String getCashFlow() {

        return this.cashFlow;
    }

    /**
     * Setter for property cashFlow.
     * @param cashFlow New value of property cashFlow.
     */
    public void setCashFlow(String cashFlow) {

        this.cashFlow = cashFlow;
    }

    /**
     * Holds value of property cashFlowName.
     */
    private String cashFlowName;

    /**
     * Getter for property cashFlowName.
     * @return Value of property cashFlowName.
     */
    public String getCashFlowName() {

        return this.cashFlowName;
    }

    /**
     * Setter for property cashFlowName.
     * @param cashFlowName New value of property cashFlowName.
     */
    public void setCashFlowName(String cashFlowName) {

        this.cashFlowName = cashFlowName;
    }

    public int compareTo(Object o) {
        return(this.getKey().compareTo(o.toString()));
    }
    
}
