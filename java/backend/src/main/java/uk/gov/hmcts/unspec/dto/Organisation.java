package uk.gov.hmcts.unspec.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Organisation extends Party {
    private String name;

    @Override
    public String name() {
        return name;
    }
}
