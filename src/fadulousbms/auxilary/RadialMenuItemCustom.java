package fadulousbms.auxilary;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;

public class RadialMenuItemCustom extends RadialMenuItem
{
    //private Text menuItemText = TextBuilder.create().text("").build();
    private Text menuItemText = new Text("");
    private Double textScaleX;
    private Double textScaleY;
    private Double textTranslateX;
    private Double textTranslateY;

    public RadialMenuItemCustom(final double menuSize, final String text, final Node graphic, final Boolean renderGraphic, final EventHandler<ActionEvent> actionHandler)
    {
        super(menuSize, graphic, actionHandler);
        super.text = text;
        //menuItemText = TextBuilder.create()
        menuItemText.setFill(Color.BLUE);
        menuItemText.setManaged(false);
        menuItemText.setTextOrigin(VPos.CENTER);
        menuItemText.setOnMouseEntered(t ->
        {
            menuItemText.setStroke(Color.POWDERBLUE);
            menuItemText.setScaleX(textScaleX + (textScaleX*0.25));
            menuItemText.setScaleY(textScaleY + (textScaleY*0.25));
        });
        menuItemText.setOnMouseExited(t ->
        {
            //menuItemText.setStroke(Color.BLUE);
            menuItemText.setScaleX(textScaleX);
            menuItemText.setScaleY(textScaleY);
        });

        menuItemText.setText(super.text);
        textScaleX = menuItemText.getScaleX();
        textScaleY = menuItemText.getScaleY();
        super.getChildren().add(menuItemText);
        menuItemText.textProperty().bind(new SimpleStringProperty(super.text));
        if(renderGraphic!=null)
            menuItemText.setVisible(renderGraphic);
        if(renderGraphic!=null)
            this.graphic.setVisible(renderGraphic);
        this.redraw();
    }

    @Override
    protected void redraw()
    {
        super.redraw();
        if(null != menuItemText)
        {
            calculateTextXY();
            menuItemText.setTranslateX(textTranslateX);
            menuItemText.setTranslateY(textTranslateY);
        }
    }

    private void calculateTextXY()
    {
        final double graphicAngle = super.startAngle.get() + (super.menuSize / 2.0);
        final double radiusValue = this.radius.get();
        final double innerRadiusValue = this.innerRadius.get();
        final double graphicRadius = innerRadiusValue + (radiusValue - innerRadiusValue) / 2.0;
        final double textRadius = graphicRadius + (radiusValue - graphicRadius) / 2.0;
        textTranslateX =  textRadius * Math.cos(Math.toRadians(graphicAngle)) - (menuItemText.getBoundsInParent().getWidth()/2.0);

        if (!this.clockwise.get()) {
            textTranslateY = -textRadius * Math.sin(Math.toRadians(graphicAngle)) - (menuItemText.getBoundsInParent().getHeight()/2.0);
        } else {
            textTranslateY = textRadius * Math.sin(Math.toRadians(graphicAngle)) - (menuItemText.getBoundsInParent().getHeight()/2.0);
        }
    }
}
