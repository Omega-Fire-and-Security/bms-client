package fadulousbms.model;

import javafx.scene.layout.BorderPane;

import java.time.LocalDate;

public class CalendarCell extends BorderPane
{
    private LocalDate date;

    public CalendarCell()
    {
        date = LocalDate.now();
    }

    public CalendarCell(LocalDate date)
    {
        this.date = date;
    }

    public CalendarCell(int year, int month, int day)
    {
        this.date = LocalDate.of(year, month, day);
    }

    public LocalDate getDate()
    {
        return date;
    }

    public void setDate(LocalDate date)
    {
        this.date = date;
    }
}
