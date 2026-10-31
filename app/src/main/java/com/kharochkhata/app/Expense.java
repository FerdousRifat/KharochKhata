package com.kharochkhata.app;

/**
 * Model class: one expense record.
 *
 * Firebase Realtime Database turns this object into JSON and back again.
 * For that it needs: a public empty constructor, and public getters/setters.
 */
public class Expense {

    private String id;          // the database key (also stored inside the record)
    private String title;       // what the money was spent on
    private double amount;      // how much
    private String category;    // Food, Transport, ...
    private String note;        // optional note
    private String photo;       // optional receipt photo as a small Base64 JPEG text
    private long dateMillis;    // the day of the expense, as milliseconds since 1970

    /** Required by Firebase. */
    public Expense() {
    }

    public Expense(String id, String title, double amount, String category,
                   String note, String photo, long dateMillis) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.note = note;
        this.photo = photo;
        this.dateMillis = dateMillis;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public long getDateMillis() {
        return dateMillis;
    }

    public void setDateMillis(long dateMillis) {
        this.dateMillis = dateMillis;
    }
}
