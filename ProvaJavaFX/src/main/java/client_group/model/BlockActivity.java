package client_group.model;

import client_group.dto.GanttBlockDTO;
import com.flexganttfx.model.activity.MutableActivityBase;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

public class BlockActivity extends MutableActivityBase<GanttBlockDTO> {

    public BlockActivity(GanttBlockDTO dto) {
        super(dto.getStepName());

        setUserObject(dto);

        setStartTime(Date.from(dto.getActualStart().atStartOfDay(ZoneId.systemDefault()).toInstant()).toInstant());
        setEndTime(Date.from(dto.getActualEnd().atStartOfDay(ZoneId.systemDefault()).toInstant()).toInstant());
    }

    public GanttBlockDTO getDTO() {
        return getUserObject();
    }

    public Long getOrderId() {
        return getDTO().getOrderId();
    }
}
