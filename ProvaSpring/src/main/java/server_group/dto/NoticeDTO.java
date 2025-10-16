package server_group.dto;


import server_group.model.Notice;

public class NoticeDTO {
    private Long id;
    private String creatorEmail;
    private String creatorFullName;
    private String subject;
    private String description;
    private String category; // uno tra: MyNotice, DelayNotice, EmployeesNotice, etc.

    public static NoticeDTO fromEntity(Notice notice, String currentUserEmail) {
        NoticeDTO dto = new NoticeDTO();
        dto.id = notice.getId();
        dto.creatorEmail = notice.getCreator().getEmail();
        dto.creatorFullName = notice.getCreator().getName() + " " + notice.getCreator().getSurname();
        dto.subject = notice.getSubject();
        dto.description = notice.getDescription();

        // calcolo categoria
        if (notice.getSubject().contains("Delay")) {
            dto.category = "DelayNotice";
        } else if (notice.getCreator().getEmail().equals(currentUserEmail)) {
            dto.category = "PersonalNotice";
        } else {
            switch (notice.getCreator().getRole().name()) {
                case "EMPLOYEE" -> dto.category = "EmployeeNotice";
                case "MANAGER" -> dto.category = "ManagerNotice";
                case "ACCOUNTANT" -> dto.category = "AccountantNotice";
                default -> dto.category = "Other";
            }
        }

        return dto;
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
