/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.*;
import fadulousbms.managers.*;
import fadulousbms.model.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Callback;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 *
 * @author ghost
 */
public abstract class ScreenController
{
    @FXML
    private ImageView img_profile;
    @FXML
    private Label user_name;
    @FXML
    protected Button btnSearch;
    @FXML
    protected Label lblSearch;
    public static BufferedImage defaultProfileImage;
    @FXML
    private Circle shpServerStatus;
    @FXML
    private Label lblOutput;
    @FXML
    private BorderPane loading_pane;

    public ScreenController()
    {
    }

    public abstract void refreshView();

    public abstract void refreshModel(Callback callback);

    public void refreshStatusBar(String msg)
    {
        try
        {
            boolean ping = RemoteComms.pingServer();
            Platform.runLater(() ->
            {
                if(shpServerStatus!=null && lblOutput!=null)
                {
                    shpServerStatus.setStroke(Color.TRANSPARENT);
                    if (ping)
                        shpServerStatus.setFill(Color.LIME);
                    else shpServerStatus.setFill(Color.RED);
                    lblOutput.setText(msg);
                }
            });
        } catch (IOException e)
        {
            if(Globals.DEBUG_ERRORS.getValue().equalsIgnoreCase("on"))
                System.out.println(getClass().getName() + ">" + IO.TAG_ERROR + ">" + "could not refresh status bar: "+e.getMessage());
            Platform.runLater(() ->
            {
                if(shpServerStatus!=null && lblOutput!=null)
                {
                    shpServerStatus.setFill(Color.RED);
                    lblOutput.setText(msg);
                }
            });
        }
    }

    @FXML
    public abstract void forceSynchronise();

    @FXML
    public void newClient()
    {
        ClientManager.getInstance().newClientPopOver(ScreenManager.getInstance(), new_client_id ->
        {
            new Thread(() ->
                    refreshModel(c->{Platform.runLater(() -> refreshView());return null;})).start();
            return null;
        });
    }

    @FXML
    public void newEmployee()
    {
        EmployeeManager.getInstance().newEmployee(ScreenManager.getInstance(), employee_id ->
        {
            new Thread(() ->
                    refreshModel(cb->{Platform.runLater(() -> refreshView());return null;})).start();
            return null;
        });
    }

    @FXML
    public static void showLogin()
    {
        try
        {
            if(ScreenManager.getInstance().loadScreen(Screens.LOGIN.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.LOGIN.getScreen())))
                ScreenManager.getInstance().setScreen(Screens.LOGIN.getScreen());
            else IO.log("ScreenController", IO.TAG_ERROR, "could not load login screen.");
        } catch (IOException e)
        {
            IO.log("ScreenController", IO.TAG_ERROR, e.getMessage());
        }
    }

    @FXML
    public static void showMain()
    {
        try
        {
            if(ScreenManager.getInstance().loadScreen(Screens.HOME.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.HOME.getScreen())))
                ScreenManager.getInstance().setScreen(Screens.HOME.getScreen());
            else IO.log("ScreenController", IO.TAG_ERROR, "could not load home screen.");
        } catch (IOException e)
        {
            IO.log("ScreenController", IO.TAG_ERROR, e.getMessage());
        }
    }

    @FXML
    public static void previousScreen()
    {
        try
        {
            ScreenManager.getInstance().setPreviousScreen();
        } catch (IOException e)
        {
            IO.log("ScreenController", IO.TAG_ERROR, e.getMessage());
        }
    }

    @FXML
    public void createAccount()
    {
        try
        {
            if(ScreenManager.getInstance().loadScreen(Screens.CREATE_ACCOUNT.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.CREATE_ACCOUNT.getScreen())))
                ScreenManager.getInstance().setScreen(Screens.CREATE_ACCOUNT.getScreen());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load account creation screen.");
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    @FXML
    public void newQuote()
    {
        QuoteManager.getInstance().setSelected(null);
        try
        {
            if(ScreenManager.getInstance().loadScreen(Screens.NEW_QUOTE.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.NEW_QUOTE.getScreen())))
                ScreenManager.getInstance().setScreen(Screens.NEW_QUOTE.getScreen());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load new quotes screen.");
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    @FXML
    public void comingSoon()
    {
        IO.logAndAlert("Coming Soon", "This feature is currently being implemented.", IO.TAG_INFO);
    }

    public static RadialMenuItem[] getDefaultContextMenu()
    {
        RadialMenuItem menuClose = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "Close", null, null, event -> ScreenManager.getInstance().hideContextMenu());
        RadialMenuItem menuBack = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "Back", null, null, event -> previousScreen());
        RadialMenuItem menuForward = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "Forward", null, null, event -> showMain());
        RadialMenuItem menuHome = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "Home", null, null, event -> showMain());
        RadialMenuItem menuLogin = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "Login", null, null, event -> showLogin());
        /*RadialMenuItem menuExcel = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "Read Excel File", null, null, event -> {
            try
            {
                EmployeeManager.getInstance().parseXLSX("quote_01.xlsx");
                //processAllSheets("quote_01.xlsx");
            } catch (OfficeXmlFileException e)
            {
                //load file using XSSF

            } catch (Exception e)
            {
                e.printStackTrace();
                IO.log(ScreenController.class.getName(), IO.TAG_ERROR, e.getMessage());
            }
        });*/
        RadialMenuItem suppliers_pdf_parser = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "Parse Sage One Suppliers PDF", null, null, event -> SupplierManager
                .parseClientSupplierPDF("parser/suppliers.pdf", new Callback()
                {
                    @Override
                    public Object call(Object param)
                    {
                        if(SessionManager.getInstance().getActive()!=null)
                        {
                            if(!SessionManager.getInstance().getActive().isExpired())
                            {
                                //get parsed attributes
                                if(param==null)
                                {
                                    IO.logAndAlert("Error", "Invalid response from PDF parser", IO.TAG_ERROR);
                                    return null;
                                }
                                String[] args = (String[]) param;
                                String balance = args[0];
                                String org = args[1];
                                String contact = args[2];
                                String category = args[3];
                                boolean active =Boolean.parseBoolean(args[4]);
                                String tel = args[5];

                                //create Supplier
                                Supplier supplier = new Supplier();
                                supplier.setSupplier_name(org);//.replaceAll("[^\\p{ASCII}]", "")
                                supplier.setCreator(SessionManager.getInstance().getActive().getUsr());
                                supplier.setAccount_name(supplier.getSupplier_name().toLowerCase().replaceAll("\\s", ""));
                                if(contact!=null)
                                    supplier.setContact_email(contact);//.replaceAll("[^\\p{ASCII}]", "")
                                //Normalizer.normalize(contact, Normalizer.Form.NFD)
                                supplier.setWebsite("");
                                supplier.setDate_partnered(System.currentTimeMillis());
                                supplier.setSpeciality(category);//.replaceAll("[^\\p{ASCII}]", "")
                                supplier.setActive(active);
                                supplier.setFax("");
                                supplier.setTel(tel);//.replaceAll("[^\\p{ASCII}]", "")
                                supplier.setPostal_address("");
                                supplier.setPhysical_address("");
                                supplier.setRegistration_number("");
                                supplier.setVat_number("");

                                IO.log(getClass().getName(),IO.TAG_INFO, "############new Client"+supplier.getJSONString());

                                try
                                {
                                    SupplierManager.getInstance().putObject(supplier, arg ->
                                    {
                                        if(arg!=null)
                                            IO.logAndAlert("Success", "Successfully created new Supplier: ["+param+"]{"+supplier.getSupplier_name()+"}", IO.TAG_INFO);
                                        else IO.logAndAlert("Error", "Could not create new Supplier", IO.TAG_ERROR);
                                        return null;
                                    });
                                }
                                catch (IOException e)
                                {
                                    IO.logAndAlert("Error", e.getMessage(), IO.TAG_ERROR);
                                    e.printStackTrace();
                                }
                            } else IO.logAndAlert("Error: Session Expired", "Active session is has expired.\nPlease log inx.", IO.TAG_ERROR);
                        } else IO.logAndAlert("Error: Invalid Session", "Active session is invalid.\nPlease log in.", IO.TAG_ERROR);
                        return null;
                    }
                }));
        RadialMenuItem clients_pdf_parser = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "Parse Sage One Clients PDF", null, null, event -> ClientManager
                .parseClientSupplierPDF("parser/clients.pdf", new Callback()
                {
                    @Override
                    public Object call(Object param)
                    {
                        if(SessionManager.getInstance().getActive()!=null)
                        {
                            if(!SessionManager.getInstance().getActive().isExpired())
                            {
                                if(param==null)
                                {
                                    IO.logAndAlert("Error", "Invalid response from PDF parser", IO.TAG_ERROR);
                                    return null;
                                }
                                String[] args = (String[]) param;
                                String balance = args[0];
                                String org = args[1];
                                String contact = args[2];
                                String category = args[3];
                                boolean active =Boolean.parseBoolean(args[4]);
                                String tel = args[5];

                                //create Client
                                Client client = new Client();
                                client.setClient_name(org);//.replaceAll("[^\\p{ASCII}]", "")
                                client.setCreator(SessionManager.getInstance().getActive().getUsr());
                                client.setAccount_name(client.getClient_name().toLowerCase().replaceAll("\\s", ""));
                                if(contact!=null)
                                    client.setContact_email(contact);//.replaceAll("[^\\p{ASCII}]", "")
                                //Normalizer.normalize(contact, Normalizer.Form.NFD)
                                client.setWebsite("");
                                client.setDate_partnered(System.currentTimeMillis());
                                client.setActive(active);
                                client.setFax("");
                                client.setTel(tel);//.replaceAll("[^\\p{ASCII}]", "")
                                client.setPostal_address("");
                                client.setPhysical_address("");
                                client.setRegistration_number("");
                                client.setVat_number("");

                                IO.log(getClass().getName(),IO.TAG_INFO, "############new Client"+client.getJSONString());

                                try
                                {
                                    ClientManager.getInstance().putObject(client, arg ->
                                    {
                                        if(arg!=null)
                                            IO.logAndAlert("Success", "Successfully created new Client: ["+param+"]{"+client.getClient_name()+"}", IO.TAG_INFO);
                                        else IO.logAndAlert("Error", "Could not create new Client", IO.TAG_ERROR);
                                        return null;
                                    });
                                } catch (IOException e)
                                {
                                    IO.logAndAlert("Error", e.getMessage(), IO.TAG_ERROR);
                                    e.printStackTrace();
                                }
                            } else IO.logAndAlert("Error: Session Expired", "Active session is has expired.\nPlease log inx.", IO.TAG_ERROR);
                        } else IO.logAndAlert("Error: Invalid Session", "Active session is invalid.\nPlease log in.", IO.TAG_ERROR);
                        return null;
                    }
                }));

        RadialMenuItem regal_pricelist_parser = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "Parse Regal Price-list PDF", null, null, event -> ApplicationObjectManager
                .parseRegalPDF("parser/regal_pricelist_01-02-2018.pdf", new Callback()
                {
                    @Override
                    public Object call(Object param)
                    {
                        if(SessionManager.getInstance().getActive()!=null)
                        {
                            if(!SessionManager.getInstance().getActive().isExpired())
                            {
                                if(param==null)
                                {
                                    IO.logAndAlert("Error", "Invalid response from PDF parser", IO.TAG_ERROR);
                                    return null;
                                }
                                Resource resource = (Resource) param;
                                //create new resource type
                                ResourceType resourceType = new ResourceType(resource.getResource_type(), resource.getResource_type());
                                resourceType.setCreator(SessionManager.getInstance().getActive().getUsr());
                                try
                                {
                                    ResourceManager.getInstance().putObject(resourceType, res_type_id ->
                                    {
                                        if(res_type_id !=null)
                                        {
                                            if (res_type_id instanceof String)
                                            {
                                                String type_id = (String) res_type_id;
                                                //set real resource type ID
                                                resource.setResource_type(type_id.replaceAll("\"", "").trim());
                                                //set supplier_id to regal's ID
                                                resource.setSupplier_id("regal");
                                                //set resource creator
                                                resource.setCreator(SessionManager.getInstance().getActive().getUsr());
                                                //set date acquired
                                                resource.setDate_acquired(System.currentTimeMillis());
                                                //set quantity
                                                resource.setQuantity(1);
                                                //set unit
                                                resource.setUnit("ea");

                                                //create resource
                                                try
                                                {
                                                    ResourceManager.getInstance().putObject(resource, res_id ->
                                                    {
                                                        if (res_id != null)
                                                            IO.logAndAlert("Success", "Successfully created new material: " + resource.getResource_description() + "!", IO.TAG_INFO);
                                                        else
                                                            IO.logAndAlert("Error", "Could not create a new material", IO.TAG_ERROR);
                                                        return null;
                                                    });
                                                } catch (IOException e)
                                                {
                                                    IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                                }
                                            } else IO.log(getClass().getName(), IO.TAG_WARN, "invalid resource_type_id returned by server.");
                                        }
                                        return null;
                                    });
                                } catch (IOException e)
                                {
                                    IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                }
                            } else IO.logAndAlert("Error: Session Expired", "Active session is has expired.\nPlease log inx.", IO.TAG_ERROR);
                        } else IO.logAndAlert("Error: Invalid Session", "Active session is invalid.\nPlease log in.", IO.TAG_ERROR);
                        return null;
                    }
                }));

        RadialMenuItem reditron_pricelist_parser = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "Parse Reditron Price-list PDF", null, null, event -> ApplicationObjectManager
                .parseReditronPDF("parser/reditron_June2017Pricelist.pdf", new Callback()
                {
                    @Override
                    public Object call(Object param)
                    {
                        if(SessionManager.getInstance().getActive()!=null)
                        {
                            if(!SessionManager.getInstance().getActive().isExpired())
                            {
                                if(param==null)
                                {
                                    IO.logAndAlert("Error", "Invalid response from PDF parser", IO.TAG_ERROR);
                                    return null;
                                }
                                Resource resource = (Resource) param;
                                //create new resource type
                                ResourceType resourceType = new ResourceType(resource.getResource_type(), resource.getResource_type());
                                resourceType.setCreator(SessionManager.getInstance().getActive().getUsr());
                                try
                                {
                                    ResourceManager.getInstance().putObject(resourceType, res_type_id ->
                                    {
                                        if(res_type_id !=null)
                                        {
                                            if (res_type_id instanceof String)
                                            {
                                                String type_id = (String) res_type_id;
                                                //set real resource type ID
                                                resource.setResource_type(type_id.replaceAll("\"", "").trim());
                                                //set supplier_id to regal's ID
                                                resource.setSupplier_id("reditron");
                                                //set resource creator
                                                resource.setCreator(SessionManager.getInstance().getActive().getUsr());
                                                //set date acquired
                                                resource.setDate_acquired(System.currentTimeMillis());
                                                //set quantity
                                                resource.setQuantity(1);
                                                //set unit
                                                resource.setUnit("ea");

                                                //create resource
                                                try
                                                {
                                                    ResourceManager.getInstance().putObject(resource, res_id ->
                                                    {
                                                        if (res_id != null)
                                                            IO.logAndAlert("Success", "Successfully created new material: " + resource.getResource_description() + "!", IO.TAG_INFO);
                                                        else
                                                            IO.logAndAlert("Error", "Could not create a new material", IO.TAG_ERROR);
                                                        return null;
                                                    });
                                                } catch (IOException e)
                                                {
                                                    IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                                }
                                            } else IO.log(getClass().getName(), IO.TAG_WARN, "invalid resource_type_id returned by server.");
                                        }
                                        return null;
                                    });
                                } catch (IOException e)
                                {
                                    IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                }
                            } else IO.logAndAlert("Error: Session Expired", "Active session is has expired.\nPlease log inx.", IO.TAG_ERROR);
                        } else IO.logAndAlert("Error: Invalid Session", "Active session is invalid.\nPlease log in.", IO.TAG_ERROR);
                        return null;
                    }
                }));

        RadialMenuItem adi_hikvision_pricelist_parser = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "Parse ADI HikVision Price-list XLSX", null, null, event -> ApplicationObjectManager
                .parseADIHikVisionPricelistXLSX("parser/ADI Hikvision  Peripheral Jan 2018 v25.xlsx", new Callback()
                {
                    @Override
                    public Object call(Object param)
                    {
                        if(SessionManager.getInstance().getActive()!=null)
                        {
                            if(!SessionManager.getInstance().getActive().isExpired())
                            {
                                if(param==null)
                                {
                                    IO.logAndAlert("Error", "Invalid response from PDF parser", IO.TAG_ERROR);
                                    return null;
                                }
                                Resource resource = (Resource) param;
                                //create new resource type
                                ResourceType resourceType = new ResourceType(resource.getResource_type(), resource.getResource_type());
                                resourceType.setCreator(SessionManager.getInstance().getActive().getUsr());
                                try
                                {
                                    ResourceManager.getInstance().putObject(resourceType, res_type_id ->
                                    {
                                        if(res_type_id !=null)
                                        {
                                            if (res_type_id instanceof String)
                                            {
                                                String type_id = (String) res_type_id;
                                                //set real resource type ID
                                                resource.setResource_type(type_id.replaceAll("\"", "").trim());
                                                //set supplier_id to regal's ID
                                                resource.setSupplier_id("adi");
                                                //set resource creator
                                                resource.setCreator(SessionManager.getInstance().getActive().getUsr());
                                                //set date acquired
                                                resource.setDate_acquired(System.currentTimeMillis());
                                                //set quantity
                                                resource.setQuantity(1);
                                                //set unit
                                                resource.setUnit("ea");

                                                //create resource
                                                try
                                                {
                                                    ResourceManager.getInstance().putObject(resource, res_id ->
                                                    {
                                                        if (res_id != null)
                                                            IO.logAndAlert("Success", "Successfully created new material: " + resource.getResource_description() + "!", IO.TAG_INFO);
                                                        else
                                                            IO.logAndAlert("Error", "Could not create a new material", IO.TAG_ERROR);
                                                        return null;
                                                    });
                                                } catch (IOException e)
                                                {
                                                    IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                                }
                                            } else IO.log(getClass().getName(), IO.TAG_WARN, "invalid resource_type_id returned by server.");
                                        }
                                        return null;
                                    });
                                } catch (IOException e)
                                {
                                    IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                }
                            } else IO.logAndAlert("Error: Session Expired", "Active session is has expired.\nPlease log inx.", IO.TAG_ERROR);
                        } else IO.logAndAlert("Error: Invalid Session", "Active session is invalid.\nPlease log in.", IO.TAG_ERROR);
                        return null;
                    }
                }));

        return new RadialMenuItem[]{menuClose, menuBack, menuForward, menuHome, menuLogin, suppliers_pdf_parser, clients_pdf_parser, regal_pricelist_parser, reditron_pricelist_parser, adi_hikvision_pricelist_parser};
    }

    //public abstract RadialMenuItem[] getContextMenu();

    public ImageView getProfileImageView()
    {
        return this.img_profile;
    }

    public Label getUserNameLabel()
    {
        return this.user_name;
    }

    public BorderPane getLoadingPane()
    {
        return this.loading_pane;
    }
}
