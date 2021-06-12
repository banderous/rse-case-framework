package uk.gov.hmcts.unspec.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccf.XUI;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address {
  @XUI(label = "Address line 1")
  private String address1;
  @XUI(label = "Address line 2")
  private String address2;
  @XUI(label = "Address line 3")
  private String address3;
  @XUI(label = "Postcode")
  private String postcode;
}
