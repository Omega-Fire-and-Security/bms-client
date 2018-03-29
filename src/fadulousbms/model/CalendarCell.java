package fadulousbms.model;

import fadulousbms.FadulousBMS;
import javafx.scene.layout.BorderPane;

import java.time.LocalDate;

/**
 * Created by th3gh0st on 2018/03/20.
 * @author th3gh0st
 */

public class CalendarCell extends BorderPane
{
    private LocalDate date;

    public CalendarCell()
    {
        super();
        date = LocalDate.now();

        styleSelf();
    }

    public CalendarCell(LocalDate date)
    {
        super();
        this.date = date;

        styleSelf();
    }

    public CalendarCell(int year, int month, int day)
    {
        this.date = LocalDate.of(year, month, day);

        styleSelf();
    }

    public LocalDate getDate()
    {
        return date;
    }

    public void setDate(LocalDate date)
    {
        this.date = date;
    }

    /**
     * Method to style calendar cell - add background and hover effects classes
     */
    public void styleSelf()
    {
        this.getStylesheets().add(FadulousBMS.class.getResource("styles/home.css").toExternalForm());
        this.getStyleClass().add("calendarButton");
    }

    /**
     * Method to style calendar cell of current day
     */
    public void styleAsCurrentDay()
    {
        this.getStyleClass().add("calendarButtonActive");
    }
}
