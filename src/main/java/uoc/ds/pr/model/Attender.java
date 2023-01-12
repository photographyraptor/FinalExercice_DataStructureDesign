package uoc.ds.pr.model;

public class Attender {
    private String phone;
    private String name;

    public Attender(String phone, String name) {
        this.phone = phone;
        this.name = name;
    }

    public String getPhone() {
        return this.phone;
    }

    public String getName() {
        return this.name;
    }
}
