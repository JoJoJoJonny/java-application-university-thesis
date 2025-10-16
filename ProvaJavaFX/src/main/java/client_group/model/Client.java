package client_group.model;

public class Client {

    private String piva;
    private String companyName;
    private String email;
    private String phone;

    public Client() {}

    public Client(String piva, String companyName, String email, String phone) {
        this.piva = piva;
        this.companyName = companyName;
        this.email = email;
        this.phone = phone;
    }

    public String getPiva() {
        return piva;
    }

    public void setPiva(String piva) {
        this.piva = piva;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return companyName + " (" + piva + ")";
    }
}
