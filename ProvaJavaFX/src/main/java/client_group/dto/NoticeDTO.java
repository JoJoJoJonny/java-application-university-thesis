package client_group.dto;


public class NoticeDTO {
    private Long id;
    private String creatorEmail;
    private String creatorFullName;
    private String subject;
    private String description;
    private String category; // uno tra: MyNotice, DelayNotice, EmployeesNotice, etc.

    public NoticeDTO() {
    }

    public NoticeDTO(String creatorEmail, String subject, String description) {
        this.creatorEmail = creatorEmail;
        this.subject = subject;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCreatorEmail() {
        return creatorEmail;
    }

    public void setCreatorEmail(String creatorEmail) {
        this.creatorEmail = creatorEmail;
    }

    public String getCreatorFullName() {
        return creatorFullName;
    }

    public void setCreatorFullName(String creatorFullName) {
        this.creatorFullName = creatorFullName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
