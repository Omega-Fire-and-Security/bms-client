package fadulousbms;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.auxilary.Session;
import fadulousbms.exceptions.LoginException;
import fadulousbms.managers.*;
import fadulousbms.model.Screens;
import javafx.application.Preloader;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;

/**
 * Created by ghost on 2017/06/11.
 */
public class BMSPreloader extends Preloader
{
    ProgressBar bar;
    Stage stage;
    int shadowSize = 50;
    TextField txtUsr, txtPwd;

    /*public void login()
    {
        try
        {
            String usr = txtUsr.getText(), pwd=txtPwd.getText();
            if(usr!=null && pwd!=null)
            {
                try
                {
                    Session session = RemoteComms.auth(usr, pwd);
                    SessionManager ssn_mgr = SessionManager.getInstance();
                    ssn_mgr.addSession(session);

                    //stage.close();
                }catch(ConnectException ex)
                {
                    JOptionPane.showMessageDialog(null, ex.getMessage() + ", \nis the server up? are you connected to the network?", "Login failure", JOptionPane.ERROR_MESSAGE);
                    IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage() + ", \nis the server up? are you connected to the network?");
                } catch (LoginException ex)
                {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Login failure", JOptionPane.ERROR_MESSAGE);
                    IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
                    //Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else{
                JOptionPane.showMessageDialog(null, "Invalid entry.", "Login failure", JOptionPane.ERROR_MESSAGE);
                IO.log(getClass().getName(), IO.TAG_ERROR, "invalid input.");
            }
        } catch (IOException ex)
        {
            IO.logAndAlert(getClass().getName(), ex.getMessage(), IO.TAG_ERROR);
            //Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }*/

    private Scene createPreloaderScene()
    {
        //StackPane stackPane = new StackPane();

        bar = new ProgressBar();

        ImageView logo = new ImageView();
        try
        {
            BufferedImage buff_image = ImageIO.read(fadulousbms.FadulousBMS.class.getResourceAsStream("images/logo.png"));
            if(buff_image!=null)
            {
                Image image = SwingFXUtils.toFXImage(buff_image, null);
                logo.setImage(image);
                logo.setFitHeight(50);
                logo.setFitWidth(150);
            }else IO.logAndAlert("Error", "Buffered image is null.", IO.TAG_ERROR);
        } catch (IOException e)
        {
            IO.logAndAlert("IO Error", e.getMessage(), IO.TAG_ERROR);
            e.printStackTrace();
        }

        Pane logo_cont = new Pane(logo);
        logo_cont.setId("logo_cont");

        Label lblMsg = new Label("Loading...");
        VBox vbxBottom = new VBox();
        //VBox.setVgrow(bar, Priority.ALWAYS);
        //HBox.setHgrow(bar, Priority.ALWAYS);
        vbxBottom.getChildren().addAll(bar, lblMsg);
        vbxBottom.setAlignment(Pos.CENTER);
        vbxBottom.setStyle("-fx-border-insets:0px 0px 0px 10px;");
        vbxBottom.setStyle("-fx-background-insets:0px 0px 0px 10px;");

        BorderPane main_pane = new BorderPane(createShadowPane());
        main_pane.setRight(logo_cont);
        main_pane.setBottom(vbxBottom);

        //scene.setStyle("-fx-background-color:rgba(255,255,255,0.5);" +
        //        "-fx-background-insets:50;");

        //Button btnLogin = new Button("Login");

        //login_container.getChildren().addAll(usr_cont, pwd_cont, btnLogin);

        //btnLogin.setOnAction(event -> login());

        /*BorderPane borderPane_login = new BorderPane();
        VBox login_container = new VBox();

        HBox usr_cont = new HBox(new Label("Username"));
        TextField txtUsr = new TextField();
        usr_cont.getChildren().add(txtUsr);

        HBox pwd_cont = new HBox(new Label("Password"));
        TextField txtPwd = new TextField();
        pwd_cont.getChildren().add(txtPwd);

        login_container.getChildren().addAll(usr_cont, pwd_cont);
        borderPane_login.setCenter(login_container);

        stackPane.getChildren().addAll(borderPane_login);*/

        return new Scene(main_pane, 300, 150);
    }

    // Create a shadow effect as a halo around the pane and not within
    // the pane's content area.
    private Pane createShadowPane()
    {
        Pane shadowPane = new Pane();

        Rectangle innerRect = new Rectangle();
        Rectangle outerRect = new Rectangle();
        shadowPane.layoutBoundsProperty().addListener(
                (observable, oldBounds, newBounds) -> {
                    innerRect.relocate(
                            newBounds.getMinX() + shadowSize,
                            newBounds.getMinY() + shadowSize
                    );
                    innerRect.setWidth(newBounds.getWidth() - shadowSize * 2);
                    innerRect.setHeight(newBounds.getHeight() - shadowSize * 2);

                    outerRect.setWidth(newBounds.getWidth());
                    outerRect.setHeight(newBounds.getHeight());

                    Shape clip = Shape.subtract(outerRect, innerRect);
                    shadowPane.setClip(clip);
                }
        );

        return shadowPane;
    }

    public void start(Stage stage) throws Exception
    {
        this.stage = stage;
        stage.setWidth(500);
        stage.setHeight(300);
        //stage.initStyle(StageStyle.TRANSPARENT);
        Scene scene = createPreloaderScene();
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(BMSPreloader.class.getResource("styles/splash.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void handleProgressNotification(ProgressNotification pn)
    {
        bar.setProgress(pn.getProgress());
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification evt)
    {
        if (evt.getType() == StateChangeNotification.Type.BEFORE_START)
        {
            /*try
            {
                Thread.sleep(5000);
            } catch (InterruptedException e)
            {
                System.err.println(e.getMessage());
            }*/
            stage.hide();
        }
    }
}
