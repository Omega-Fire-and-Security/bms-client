/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.model;

/**
 *
 * @author ghost
 */
public enum Screens 
{
    HOME("Homescreen.fxml"),
    LOGIN("Login.fxml"),
    OPERATIONS("Operations.fxml"),
    OPERATIONS_PRODUCTION("Operations_production.fxml"),
    OPERATIONS_SALES("Operations_sales.fxml"),
    OPERATIONS_FACILITIES("Operations_facilities.fxml"),
    OPERATIONS_CLIENTS("Operations_facilities.fxml"),
    SAFETY("Safety.fxml"),
    SAFETY_FILES("SafetyFiles.fxml"),
    SETTINGS("Settings.fxml"),
    CREATE_ACCOUNT("Create_account.fxml"),
    RESET_PWD("ResetPassword.fxml"),
    NEW_QUOTE("NewQuote.fxml"),
    REJECTED_QUOTES("RejectedQuotes.fxml"),
    QUOTES("Quotes.fxml"),
    VIEW_QUOTE("View_quote.fxml"),
    SALES("Sales.fxml"),
    JOBS("Jobs.fxml"),
    VIEW_JOB("View_job.fxml"),
    CLIENTS("Clients.fxml"),
    NEW_CLIENT("NewClient.fxml"),
    SUPPLIERS("Suppliers.fxml"),
    NEW_SUPPLIER("NewSupplier.fxml"),
    RESOURCES("Stock.fxml"),
    NEW_RESOURCE("NewResource.fxml"),
    FACILITIES("Facilities.fxml"),
    HR("HR.fxml"),
    EMPLOYEES("Employees.fxml"),
    LEAVE("Leave.fxml"),
    OVERTIME("Overtime.fxml"),
    PAYROLL("Payroll.fxml"),
    POLICY("Policy.fxml"),
    ACCOUNTING("Accounting.fxml"),
    PURCHASES("Purchases.fxml"),
    ASSETS("Assets.fxml"),
    NEW_ASSET("NewAsset.fxml"),
    INVOICES("Invoices.fxml"),
    EXPENSES("Expenses.fxml"),
    NEW_EXPENSE("NewExpense.fxml"),
    JOURNALS("Journals.fxml"),
    ADDITIONAL_REVENUE("Revenue.fxml"),
    NEW_REVENUE("NewRevenue.fxml"),
    PURCHASE_ORDER("PurchaseOrders.fxml"),
    NEW_PURCHASE_ORDER("NewPurchaseOrder.fxml"),
    VIEW_PURCHASE_ORDER("ViewPurchaseOrder.fxml"),
    NEW_REQUISITION("NewRequisition.fxml");

    private String screen;
    
    Screens(String screen){
        this.screen = screen;
    }
    
    public String getScreen()
    {
        return screen;
    }
}