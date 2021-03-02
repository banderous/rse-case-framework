package uk.gov.hmcts.unspec.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccf.XUI;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ConfirmService {
    @XUI(label = "Enter your name")
    private String name;
    @XUI(label = "Enter your role")
    private String role;
}
