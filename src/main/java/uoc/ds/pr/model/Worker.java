package uoc.ds.pr.model;

import java.time.LocalDate;

public class Worker {
    private String dni;
    private String name;
    private String surname;
    private LocalDate birthday;
    private String roleId;

    public Worker(String dni, String name, String surname, LocalDate birthDay, String roleId) {
        this.dni = dni;
        this.name = name;
        this.surname = surname;
        this.birthday = birthDay;
        this.roleId = roleId;
    }

    public String getDni() {
        return this.dni;
    }

    public String getName() {
        return this.name;
    }

    public String getSurname() {
        return this.surname;
    }
    
    public LocalDate getBirthDay() {
        return this.birthday;
    }

    public String getRoleId() {
        return this.roleId;
    }
}
