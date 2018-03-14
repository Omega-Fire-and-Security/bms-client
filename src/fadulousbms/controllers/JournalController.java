package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RadialMenuItemCustom;
import fadulousbms.managers.AssetManager;
import fadulousbms.managers.ScreenManager;
import fadulousbms.managers.SessionManager;
import fadulousbms.model.Employee;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * Created by ghost on 2017/02/02.
 */
public class JournalController extends ScreenController implements Initializable
{
    @FXML
    BorderPane bpDatePickerContainer;
    @FXML
    VBox vBox;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        try
        {
            AssetManager.getInstance().initialize();
            defaultProfileImage = ImageIO.read(new File("images/profile.png"));
            Image image = SwingFXUtils.toFXImage(defaultProfileImage, null);
            this.getProfileImageView().setImage(image);
        }catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    @Override
    public void refreshView()
    {
        Date date = new Date(System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month = calendar.get(Calendar.MONTH);

        //VBox vBox = new VBox();
        HBox hBox = new HBox();
        int col = 0;
        for(int day=0;day<=31;day++)
        {
            if(col>=7)
            {
                vBox.getChildren().add(hBox);
                hBox = new HBox();
                col=0;
            }
            BorderPane bp = new BorderPane();
            bp.setMinWidth(100);
            bp.setMinHeight(90);
            bp.setStyle("-fx-background-color:#343434;");
            hBox.getChildren().add(bp);
        }
        //bpDatePickerContainer.getChildren().add(vBox);
    }

    @Override
    public void refreshModel(Callback callback)
    {
        //execute callback
        if(callback!=null)
            callback.call(null);
    }

    @Override
    public void forceSynchronise()
    {
        refreshModel(param ->
        {
            Platform.runLater(() -> refreshView());
            return null;
        });

    }

    public static RadialMenuItem[] getDefaultContextMenu()
    {
        //RadialMenuItem level1Item = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "level 1 item 1", null, null, null);//RadialMenuItem(menuSize, "level 1 item", null, null);
        return ScreenController.getDefaultContextMenu();
    }
}
