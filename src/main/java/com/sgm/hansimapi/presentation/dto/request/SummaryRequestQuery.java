package com.sgm.hansimapi.presentation.dto.request;

import com.sgm.hansimapi.domain.TimeWindow;
import lombok.Setter;

// Spring MVC @ModelAttribute 바인딩을 위해 @Setter 필요
@Setter
public class SummaryRequestQuery {

    // yyyy-MM-dd 형식 (예: 2026-02-10). 미입력 시 오늘 하루로 설정
    private String startDate;
    private String endDate;

    public TimeWindow toTimeWindow() {
        return TimeWindow.from(startDate, endDate);
    }

    public String getStartDate() { return startDate; }
    public String getEndDate()   { return endDate; }
}
