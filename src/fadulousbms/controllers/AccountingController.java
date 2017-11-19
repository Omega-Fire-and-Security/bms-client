package fadulousbms.controllers;

import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.PDF;
import fadulousbms.managers.AssetManager;
import fadulousbms.managers.ResourceManager;
import fadulousbms.managers.ScreenManager;
import fadulousbms.managers.SessionManager;
import fadulousbms.model.Employee;
import fadulousbms.model.Screens;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.ZoneId;
import java.util.ResourceBundle;

/**
 * Created by ghost on 2017/02/02.
 */
public class AccountingController extends ScreenController implements Initializable
{
    @Override
    public void refreshView()
    {
    }

    @Override
    public void refreshModel()
    {
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
    }

    @FXML
    public void purchasesClick()
    {
        final ScreenManager screenManager = ScreenManager.getInstance();
        ScreenManager.getInstance().showLoadingScreen(param ->
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(screenManager.loadScreen(Screens.PURCHASES.getScreen(),getClass().getResource("../views/"+Screens.PURCHASES.getScreen())))
                        {
                            Platform.runLater(() ->
                                    screenManager.setScreen(Screens.PURCHASES.getScreen()));
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load purchases screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                    }
                }
            }).start();
            return null;
        });
    }

    @FXML
    public void invoicesClick()
    {
        final ScreenManager screenManager = ScreenManager.getInstance();
        ScreenManager.getInstance().showLoadingScreen(param ->
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(screenManager.loadScreen(Screens.INVOICES.getScreen(),getClass().getResource("../views/"+Screens.INVOICES.getScreen())))
                        {
                            Platform.runLater(() ->
                                    screenManager.setScreen(Screens.INVOICES.getScreen()));
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load invoices screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                    }
                }
            }).start();
            return null;
        });
    }

    @FXML
    public void generalJournalClick()
    {
        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME + " - General Journal Period");
        stage.setAlwaysOnTop(true);
        //stage.setMinWidth(350);
        //stage.setMinHeight(170);

        DatePicker dateStart = new DatePicker();
        DatePicker dateEnd = new DatePicker();
        VBox vBox = new VBox();
        VBox.getVgrow(vBox);

        HBox hbox = new HBox(new Label("Start date:"), dateStart);
        HBox.setHgrow(hbox, Priority.ALWAYS);
        HBox.setHgrow(hbox.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(hbox.getChildren().get(1), Priority.ALWAYS);
        ((Label)hbox.getChildren().get(0)).setMinWidth(120);
        hbox.setSpacing(20);
        vBox.getChildren().add(hbox);

        hbox = new HBox(new Label("End date:"), dateEnd);
        HBox.setHgrow(hbox, Priority.ALWAYS);
        HBox.setHgrow(hbox.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(hbox.getChildren().get(1), Priority.ALWAYS);
        ((Label)hbox.getChildren().get(0)).setMinWidth(120);
        hbox.setSpacing(20);
        vBox.getChildren().add(hbox);
        vBox.setSpacing(20);

        Button btnSubmit = new Button("Generate");
        btnSubmit.setDefaultButton(true);
        btnSubmit.setMinWidth(80);
        btnSubmit.setMinHeight(50);

        vBox.getChildren().add(btnSubmit);
        stage.setScene(new Scene(vBox));
        stage.show();

        btnSubmit.setOnAction(event ->
        {
            if(dateStart.getValue()==null)
            {
                IO.logAndAlert("Invalid start date", "Please choose a valid starting date.", IO.TAG_ERROR);
                return;
            }
            if(dateEnd.getValue()==null)
            {
                IO.logAndAlert("Invalid end date", "Please choose a valid ending date.", IO.TAG_ERROR);
                return;
            }
            long date_start =  dateStart.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            long date_end = dateEnd.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            try
            {
                PDF.createGeneralJournalPdf(date_start, date_end);
            } catch (IOException e)
            {
                IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
            }
            //stage.close();
        });
        /*final ScreenManager screenManager = ScreenManager.getInstance();
        ScreenManager.getInstance().showLoadingScreen(param ->
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(screenManager.loadScreen(Screens.JOURNALS.getScreen(),getClass().getResource("../views/"+Screens.JOURNALS.getScreen())))
                        {
                            Platform.runLater(() ->
                                    screenManager.setScreen(Screens.JOURNALS.getScreen()));
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load journals screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                        e.printStackTrace();
                    }
                }
            }).start();
            return null;
        });
        /*try
        {
            PDF.createGeneralJournalPdf();
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }*/
    }

    @FXML
    public void generalLedgerClick()
    {
        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME + " - General Journal Ledger");
        stage.setAlwaysOnTop(true);
        //stage.setMinWidth(350);
        //stage.setMinHeight(170);

        DatePicker dateStart = new DatePicker();
        DatePicker dateEnd = new DatePicker();
        VBox vBox = new VBox();
        VBox.getVgrow(vBox);

        HBox hbox = new HBox(new Label("Start date:"), dateStart);
        HBox.setHgrow(hbox, Priority.ALWAYS);
        HBox.setHgrow(hbox.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(hbox.getChildren().get(1), Priority.ALWAYS);
        ((Label)hbox.getChildren().get(0)).setMinWidth(120);
        hbox.setSpacing(20);
        vBox.getChildren().add(hbox);

        hbox = new HBox(new Label("End date:"), dateEnd);
        HBox.setHgrow(hbox, Priority.ALWAYS);
        HBox.setHgrow(hbox.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(hbox.getChildren().get(1), Priority.ALWAYS);
        ((Label)hbox.getChildren().get(0)).setMinWidth(120);
        hbox.setSpacing(20);
        vBox.getChildren().add(hbox);
        vBox.setSpacing(20);

        Button btnSubmit = new Button("Generate");
        btnSubmit.setDefaultButton(true);
        btnSubmit.setMinWidth(80);
        btnSubmit.setMinHeight(50);

        vBox.getChildren().add(btnSubmit);
        stage.setScene(new Scene(vBox));
        stage.show();

        btnSubmit.setOnAction(event ->
        {
            if(dateStart.getValue()==null)
            {
                IO.logAndAlert("Invalid start date", "Please choose a valid starting date.", IO.TAG_ERROR);
                return;
            }
            if(dateEnd.getValue()==null)
            {
                IO.logAndAlert("Invalid end date", "Please choose a valid ending date.", IO.TAG_ERROR);
                return;
            }
            long date_start =  dateStart.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            long date_end = dateEnd.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            try
            {
                PDF.createGeneralLedgerPdf(date_start, date_end);
            } catch (IOException e)
            {
                IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
            }
            //stage.close();
        });
        /*final ScreenManager screenManager = ScreenManager.getInstance();
        ScreenManager.getInstance().showLoadingScreen(param ->
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(screenManager.loadScreen(Screens.JOURNALS.getScreen(),getClass().getResource("../views/"+Screens.JOURNALS.getScreen())))
                        {
                            Platform.runLater(() ->
                                    screenManager.setScreen(Screens.JOURNALS.getScreen()));
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load journals screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                        e.printStackTrace();
                    }
                }
            }).start();
            return null;
        });
        /*try
        {
            PDF.createGeneralJournalPdf();
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }*/
    }

    @FXML
    public void additionalRevenueClick()
    {
        final ScreenManager screenManager = ScreenManager.getInstance();
        ScreenManager.getInstance().showLoadingScreen(param ->
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(screenManager.loadScreen(Screens.ADDITIONAL_REVENUE.getScreen(),getClass().getResource("../views/"+Screens.ADDITIONAL_REVENUE.getScreen())))
                        {
                            Platform.runLater(() ->
                                    screenManager.setScreen(Screens.ADDITIONAL_REVENUE.getScreen()));
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load additional revenue screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                    }
                }
            }).start();
            return null;
        });
    }

    //Menu bar
    @FXML
    public void createAssetClick()
    {
        final ScreenManager screenManager = ScreenManager.getInstance();
        ScreenManager.getInstance().showLoadingScreen(param ->
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(screenManager.loadScreen(Screens.NEW_ASSET.getScreen(),getClass().getResource("../views/"+Screens.NEW_ASSET.getScreen())))
                        {
                            Platform.runLater(() ->
                                    screenManager.setScreen(Screens.NEW_ASSET.getScreen()));
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load asset creation screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                    }
                }
            }).start();
            return null;
        });
    }

    @FXML
    public void newExpenseClick()
    {
        final ScreenManager screenManager = ScreenManager.getInstance();
        ScreenManager.getInstance().showLoadingScreen(param ->
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(screenManager.loadScreen(Screens.NEW_EXPENSE.getScreen(),getClass().getResource("../views/"+Screens.NEW_EXPENSE.getScreen())))
                        {
                            Platform.runLater(() ->
                                    screenManager.setScreen(Screens.NEW_EXPENSE.getScreen()));
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load expense creation screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                    }
                }
            }).start();
            return null;
        });
    }

    @FXML
    public void newRevenueClick()
    {
        final ScreenManager screenManager = ScreenManager.getInstance();
        ScreenManager.getInstance().showLoadingScreen(param ->
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(screenManager.loadScreen(Screens.NEW_REVENUE.getScreen(),getClass().getResource("../views/"+Screens.NEW_REVENUE.getScreen())))
                        {
                            Platform.runLater(() ->
                                    screenManager.setScreen(Screens.NEW_REVENUE.getScreen()));
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load additional revenue creation screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                    }
                }
            }).start();
            return null;
        });
    }

    @FXML
    public void newResource()
    {
        ResourceManager.getInstance().newResourceWindow(param ->
        {
            new Thread(() ->
            {
                refreshModel();
                Platform.runLater(() -> refreshView());
            }).start();
            return null;
        });
    }

    @FXML
    public void newResourceType()
    {
        ResourceManager.getInstance().newResourceTypeWindow(param ->
        {
            new Thread(() ->
            {
                refreshModel();
                Platform.runLater(() -> refreshView());
            }).start();
            return null;
        });
    }
}
