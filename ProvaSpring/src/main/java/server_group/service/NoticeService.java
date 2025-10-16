package server_group.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import server_group.dto.NoticeDTO;
import server_group.model.CustomUser;
import server_group.model.Notice;
import server_group.repository.CustomUserRepository;
import server_group.repository.NoticeRepository;

import java.util.List;

@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final CustomUserRepository customUserRepository;

    public NoticeService(NoticeRepository noticeRepository, CustomUserRepository customUserRepository) {
        this.noticeRepository = noticeRepository;
        this.customUserRepository = customUserRepository;
    }

    public List<NoticeDTO> getAllNoticesFlat(String currentUserEmail) {
        List<Notice> allNotices = noticeRepository.findAll();
        return allNotices.stream()
                .map(notice -> NoticeDTO.fromEntity(notice, currentUserEmail))
                .toList();
    }

    public Notice saveNotice(Notice notice) {
        return noticeRepository.save(notice);
    }

    public Notice saveFromDto(NoticeDTO dto) {
        if (dto.getCreatorEmail() == null || dto.getCreatorEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("creatorEmail is required");
        }

        CustomUser creator = customUserRepository.findByEmail(dto.getCreatorEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Creator user not found: " + dto.getCreatorEmail()));

        Notice notice = new Notice();
        notice.setCreator(creator);
        notice.setSubject(dto.getSubject());
        notice.setDescription(dto.getDescription());

        Notice saved = noticeRepository.save(notice);

        return saved;
    }

    public boolean deleteNotice(Long id) {
        if (noticeRepository.existsById(id)) {
            noticeRepository.deleteById(id);
            return true;
        }
        return false;
    }

}
