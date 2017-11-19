/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.managers;

import java.io.IOException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.Stack;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RadialMenuItemCustom;
import fadulousbms.controllers.ScreenController;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.*;
import javafx.util.Callback;
import javafx.util.Duration;
import jfxtras.labs.scene.control.radialmenu.RadialContainerMenuItem;
import jfxtras.labs.scene.control.radialmenu.RadialMenu;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;

/**
 *
 * @author ghost
 */
public class ScreenManager extends StackPane
{
    private RadialMenu radialMenu;
    private boolean show;
    private double lastOffsetValue;
    private double lastInitialAngleValue;
    private double gestureAngle = 0;
    public Double menuSize = 55.0;
    public Double containerSize = 30.0;
    public Double initialAngle = 0.0;//-90.0
    public Double innerRadius = 50.0;
    public Double radius = 150.0;
    public Double offset = 5.0;
    private Stack<AbstractMap.SimpleEntry<String, Node>> screens = new Stack<>();
    private Stack<AbstractMap.SimpleEntry<String, ScreenController>> controllers = new Stack<>();
    private ScreenController focused;
    private String focused_id;
    private String previous_id;
    private Node loading_screen;
    private ScreenController loading_screen_ctrl;
    private Node screen = null;
    private static ScreenManager screenManager = new ScreenManager();
    private Label lblScreenName;

    private ScreenManager()
    {
        super();
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../views/loading.fxml"));
            loading_screen = loader.load();
            loading_screen_ctrl = loader.getController();
            //loading_screen_ctrl.setParent(this);
        } catch (IOException e)
        {
            e.printStackTrace();
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    public static ScreenManager getInstance()
    {
        return screenManager;
    }


    /**
     * Method to load a single ScreenController into memory.
     * @param id ScreenController identifier
     * @param path Path to the FXML view for the ScreenController.
     * @return true if successfully added new screen, false otherwise.
     * @throws IOException
     */
    public boolean  loadScreen(String id, URL path) throws IOException
    {
        FXMLLoader loader = new FXMLLoader(path);
        Parent screen = loader.load();

        ScreenController screen_controller = loader.getController();
        //screen_controller.setParent(this);

        controllers.push(new AbstractMap.SimpleEntry<>(id, screen_controller));
        screens.push(new AbstractMap.SimpleEntry<>(id, screen));
        IO.log(getClass().getName(), IO.TAG_INFO, "loaded screen: "+id);
        return true;
    }

    public AbstractMap.SimpleEntry<String, ScreenController> peekScreenControllers()
    {
        if(controllers!=null)
            return controllers.peek();
        else return null;
    }

    public Node peekScreens()
    {
        if(screens!=null)
            return screens.peek().getValue();
        else return null;
    }

    public void setPreviousScreen() throws IOException
    {
        if(screens.size()>1)
        {
            screens.pop().getValue();
            controllers.pop().getValue();
            setScreen("previous");
        }
    }

    public void createRadialMenu()
    {
        Color slightlyTrans = new Color(1.0,1.0,1.0,0.6);

        /*final LinearGradient transBackground = LinearGradientBuilder
                .create()
                .startX(0)
                .startY(0)
                .endX(1.0)
                .endY(1.0)
                .cycleMethod(CycleMethod.NO_CYCLE)
                .stops(StopBuilder.create().offset(0.0).color(slightlyTrans)
                                .build(),
                        StopBuilder.create().offset(0.6)
                                .color(slightlyTrans).build())
                .build();

        final LinearGradient backgroundMouseOn = LinearGradientBuilder
                .create()
                .startX(0)
                .startY(0)
                .endX(1.0)
                .endY(1.0)
                .cycleMethod(CycleMethod.NO_CYCLE)
                .stops(StopBuilder.create().offset(0.0).color(Color.LIGHTGREY)
                                .build(),
                        StopBuilder.create().offset(0.8)
                                .color(Color.LIGHTGREY.darker()).build())
                .build();*/

        radialMenu = new RadialMenu(initialAngle, innerRadius, radius, offset, Color.DARKCYAN, Color.CYAN,
                Color.DARKGREY.darker().darker(), Color.DARKGREY.darker(),
                false, RadialMenu.CenterVisibility.ALWAYS, null);
        radialMenu.setTranslateX(400);
        radialMenu.setTranslateY(400);
        //radialMenu.setRotate(-90);

        RadialContainerMenuItem level1Container = new RadialContainerMenuItem(containerSize, "Level 1 Container", null);
        RadialMenuItem level1Item = new RadialMenuItemCustom(menuSize, "level 1 item 1", null, null, null);//RadialMenuItem(menuSize, "level 1 item", null, null);
        RadialMenuItem level1Item2 = new RadialMenuItemCustom(menuSize, "level 1 item 2", null, null, null);//RadialMenuItem(menuSize, "level 1 item", null, null);
        RadialMenuItem level1Item3 = new RadialMenuItemCustom(menuSize, "level 1 item 3", null, null, null);//RadialMenuItem(menuSize, "level 1 item", null, null);

        RadialContainerMenuItem level2Container = new RadialContainerMenuItem(containerSize, "Level 2 Container", null);
        RadialMenuItem level2Item = new RadialMenuItemCustom(menuSize, "level 2 item", null, null, null);

        RadialContainerMenuItem level3Container = new RadialContainerMenuItem(containerSize, "Level 3 Container", null);
        RadialMenuItem level3Item = new RadialMenuItemCustom(menuSize, "level 3 item", null, null, null);

        RadialMenuItem level4Item = new RadialMenuItemCustom(menuSize, "level 4 item", null, null, null);

        //Add all your items in a nested order
        level1Container.addMenuItem(level2Item);
        level1Container.addMenuItem(level2Container);

        level2Container.addMenuItem(level3Item);
        level2Container.addMenuItem(level3Container);

        level3Container.addMenuItem(level4Item);

        //Add top level items/containers to actual RadialMenu component
        radialMenu.addMenuItem(level1Item);
        radialMenu.addMenuItem(level1Item2);
        radialMenu.addMenuItem(level1Item3);
        radialMenu.addMenuItem(level1Container);
        //radialMenu.addMenuItem(level3Container);
    }

    public void setLblScreenName(Label screenName)
    {
        this.lblScreenName=screenName;
    }
    /*private void hideRadialMenu()
    {
        final FadeTransition fade = FadeTransitionBuilder.create()
                .node(this.radialMenu).fromValue(1).toValue(0)
                .duration(Duration.millis(300))
                .onFinished(new EventHandler<ActionEvent>() {

                    @Override
                    public void handle(final ActionEvent arg0) {
                        setVisible(false);
                    }
                }).build();

        final ParallelTransition transition = ParallelTransitionBuilder
                .create().children(fade).build();

        transition.play();
    }*/

    private void showRadialMenu(final double x, final double y)
    {
        if (radialMenu.isVisible())
        {
            lastInitialAngleValue = radialMenu.getInitialAngle();
            lastOffsetValue = radialMenu.getOffset();
            radialMenu.setVisible(false);
        }
        radialMenu.setTranslateX(x);
        radialMenu.setTranslateY(y);
        radialMenu.setVisible(true);

        /*final FadeTransition fade = FadeTransitionBuilder.create()
                .node(radialMenu).duration(Duration.millis(400))
                .fromValue(0).toValue(1.0).build();

        final Animation offset = new Timeline(new KeyFrame(Duration.ZERO,
                new KeyValue(radialMenu.offsetProperty(), 0)),
                new KeyFrame(Duration.millis(300), new KeyValue(radialMenu
                        .offsetProperty(), lastOffsetValue)));

        final Animation angle = new Timeline(new KeyFrame(Duration.ZERO,
                new KeyValue(radialMenu.initialAngleProperty(),
                        lastInitialAngleValue + 20)), new KeyFrame(
                Duration.millis(300), new KeyValue(
                radialMenu.initialAngleProperty(),
                lastInitialAngleValue)));

        final ParallelTransition transition = ParallelTransitionBuilder
                .create().children(fade, offset, angle).build();

        transition.play();*/
    }

    /**
     * Method to add a ScreenController object to ScreenManager
     * @param id ScreenController identifier
     */
    public void setScreen(final String id)
    {
        if(screens.peek()!=null)
                    screen = screens.peek().getValue();
        if(screen!=null)
        {
            ScreenController controller = null;
            //update UI of current view
            if(controllers.peek()!=null)
                    controller = controllers.peek().getValue();

            if(controller!=null)
            {
                if(focused_id!=null)
                {
                    if (!focused_id.equals(previous_id))
                    {
                        previous_id = focused_id;
                        IO.log(getClass().getName(), IO.TAG_INFO, "set previous screen to: " + previous_id);
                    }
                }

                focused_id = id;
                focused = controller;
                //screen.setOpacity(1);
                focused.refreshModel();
                Platform.runLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(getChildren().setAll(new Node[]{}))//remove all screens
                        {
                            focused.refreshStatusBar("Welcome back" + (SessionManager.getInstance()
                                    .getActiveEmployee() != null ? " " + SessionManager.getInstance()
                                    .getActiveEmployee() + "!" : "!"));
                            focused.refreshView();//refresh the screen every time it's loaded
                            IO.log(getClass().getName(), IO.TAG_INFO, "set screen: " + id);

                            if(lblScreenName!=null)
                                lblScreenName.setText(focused_id.split("\\.")[0]);
                            getChildren().add(screen);
                            createRadialMenu();
                            showRadialMenu(300,250);
                            getChildren().add(radialMenu);
                        }else
                        {
                            IO.logAndAlert(getClass().getName(), "Could not remove StackPane children.", IO.TAG_ERROR);
                        }
                    }
                });
            }


            /*final DoubleProperty opacity =  opacityProperty();
            Timeline fade = new Timeline(new KeyFrame(Duration.ONE, new KeyValue(opacity, 0.0)),
                    new KeyFrame(Duration.millis(20),new KeyValue(opacity, 1.0)));
            fade.play();*/
        }else{
            IO.logAndAlert(getClass().getName(), "ScreenController ["+id+"] not loaded to memory.", IO.TAG_ERROR);
        }
    }

    public void showLoadingScreen(Callback callback)
    {
        if(getChildren().setAll(new Node[]{}))//remove all screens
        {
            loading_screen_ctrl.refreshStatusBar("Loading data, please wait...");
            loading_screen_ctrl.refreshView();
            getChildren().add(loading_screen);
            /*if(focused!=null)
            {
                focused.getLoadingPane().setVisible(true);
                //System.out.println(focused.getLoadingPane()==null);
            }*/
            callback.call(null);
        }
    }

    public ScreenController getFocused()
    {
        return this.focused;
    }

    public String getFocused_id()
    {
        return this.focused_id;
    }
    
    public Node removeScreen(Node screen)
    {
        //screens
        for(Node n: getChildren())
        {
            if(n==screen)
            {
                if(getChildren().remove(n))
                    return n;
                else
                    return null;
            }
        }
        return null;
    }
}
