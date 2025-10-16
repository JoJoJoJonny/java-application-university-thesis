package server_group.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import server_group.dto.NoticeDTO;
import server_group.model.Notice;
import server_group.repository.NoticeRepository;
import server_group.service.NoticeService;

import java.util.List;

@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    private final NoticeService noticeService;
    private final NoticeEventPublisher noticeEventPublisher;

    public NoticeController(NoticeService noticeService, NoticeEventPublisher noticeEventPublisher) {
        this.noticeService = noticeService;
        this.noticeEventPublisher = noticeEventPublisher;
    }

    //per observer pattern
    @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE', 'ACCOUNTANT')")
    @GetMapping("/all")
    public ResponseEntity<List<NoticeDTO>> getAllNotices(@RequestParam String userEmail) {
        List<NoticeDTO> result = noticeService.getAllNoticesFlat(userEmail);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE', 'ACCOUNTANT')")
    @PostMapping("/add")
    public ResponseEntity<NoticeDTO> createNotice(@RequestBody NoticeDTO dto) {
        Notice saved = noticeService.saveFromDto(dto);
        NoticeDTO savedDTO = NoticeDTO.fromEntity(saved, dto.getCreatorEmail());

        //per observer pattern
        noticeEventPublisher.publishEvent(savedDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedDTO);
    }

    @PreAuthorize("hasAnyRole('MANAGER')")
    @DeleteMapping("delete/{id}")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {
        boolean deleted = noticeService.deleteNotice(id);
        if (deleted) {
            //per observer pattern
            noticeEventPublisher.publishEvent("deleted: " + id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    //per observer pattern
    @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE', 'ACCOUNTANT')")
    @GetMapping(value = "/stream", produces = "text/event-stream")
    public SseEmitter streamNotices() {
        return noticeEventPublisher.registerClient();
    }










}
