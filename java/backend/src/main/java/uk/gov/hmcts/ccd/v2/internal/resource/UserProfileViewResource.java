package uk.gov.hmcts.ccd.v2.internal.resource;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.hateoas.RepresentationModel;
import uk.gov.hmcts.ccd.domain.model.aggregated.UserProfile;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class UserProfileViewResource extends RepresentationModel {

    @JsonUnwrapped
    private UserProfile userProfile;

    public UserProfileViewResource(@NonNull UserProfile userProfile) {
        copyProperties(userProfile);

    }

    private void copyProperties(UserProfile userProfile) {
        this.userProfile = userProfile;
    }
}
