package com.fariza.mpezexpenses.Model;

public class ModelExpenses {
    String expenseId, recordBy, expenses, expensesAmount, date, payer, expensesFile, totalMembers , eachPay, uriFile, timestamp;

    public ModelExpenses() {
    }

    public ModelExpenses(String expenseId, String recordBy, String expenses, String expensesAmount, String date, String payer, String expensesFile, String totalMembers, String eachPay, String uriFile, String timestamp) {
        this.expenseId = expenseId;
        this.recordBy = recordBy;
        this.expenses = expenses;
        this.expensesAmount = expensesAmount;
        this.date = date;
        this.payer = payer;
        this.expensesFile = expensesFile;
        this.totalMembers = totalMembers;
        this.eachPay = eachPay;
        this.uriFile = uriFile;
        this.timestamp = timestamp;
    }

    public String getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(String expenseId) {
        this.expenseId = expenseId;
    }

    public String getRecordBy() {
        return recordBy;
    }

    public void setRecordBy(String recordBy) {
        this.recordBy = recordBy;
    }

    public String getExpenses() {
        return expenses;
    }

    public void setExpenses(String expenses) {
        this.expenses = expenses;
    }

    public String getExpensesAmount() {
        return expensesAmount;
    }

    public void setExpensesAmount(String expensesAmount) {
        this.expensesAmount = expensesAmount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPayer() {
        return payer;
    }

    public void setPayer(String payer) {
        this.payer = payer;
    }

    public String getTotalMembers() {
        return totalMembers;
    }

    public void setTotalMembers(String totalMembers) {
        this.totalMembers = totalMembers;
    }


    public String getEachPay() {
        return eachPay;
    }

    public void setEachPay(String eachPay) {
        this.eachPay = eachPay;
    }

    public String getExpensesFile() {
        return expensesFile;
    }

    public void setExpensesFile(String expensesFile) {
        this.expensesFile = expensesFile;
    }

    public String getUriFile() {
        return uriFile;
    }

    public void setUriFile(String uriFile) {
        this.uriFile = uriFile;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}