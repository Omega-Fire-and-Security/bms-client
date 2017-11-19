package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.managers.AssetManager;
import fadulousbms.managers.SessionManager;
import fadulousbms.model.Employee;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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

    @Override
    public void refreshView()
    {
        Employee e = SessionManager.getInstance().getActiveEmployee();
        if(e!=null)
            this.getUserNameLabel().setText(e.getFirstname() + " " + e.getLastname());
        else IO.log(getClass().getName(), IO.TAG_ERROR, "No active sessions.");

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
    public void refreshModel()
    {

    }

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

        /*switch (month)
        {
            case
        }*/
    }


}
