package com.rumi.notification.infrastructure.web;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GatewayUserTest {

    private static final UUID BUILDING_ID = UUID.fromString("7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d");
    private static final UUID OTHER_BUILDING_ID = UUID.fromString("1f2e3d4c-5b6a-4978-8695-a4b3c2d1e0f9");

    @Test
    void readsTheCommaSeparatedBuildingsOfTheToken() {
        GatewayUser user = GatewayUser.from("RESIDENT", " " + BUILDING_ID + " ," + OTHER_BUILDING_ID + ",");

        assertThat(user.buildingIds()).containsExactlyInAnyOrder(BUILDING_ID, OTHER_BUILDING_ID);
    }

    @Test
    void aResidentOnlyReachesTheBuildingsOfTheToken() {
        GatewayUser user = GatewayUser.from("RESIDENT", BUILDING_ID.toString());

        assertThatCode(() -> user.requireAccessTo(BUILDING_ID)).doesNotThrowAnyException();
        assertThatThrownBy(() -> user.requireAccessTo(OTHER_BUILDING_ID))
                .isInstanceOf(BuildingAccessDeniedException.class);
    }

    @Test
    void aResidentWithoutBuildingsReachesNone() {
        assertThatThrownBy(() -> GatewayUser.from("RESIDENT", null).requireAccessTo(BUILDING_ID))
                .isInstanceOf(BuildingAccessDeniedException.class);
    }

    @Test
    void administratorsAndRequestsWithoutGatewayHeadersAreNotRestricted() {
        assertThatCode(() -> GatewayUser.from("ADMINISTRATOR", OTHER_BUILDING_ID.toString())
                .requireAccessTo(BUILDING_ID)).doesNotThrowAnyException();
        assertThatCode(() -> GatewayUser.from(null, null).requireAccessTo(BUILDING_ID))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsABuildingHeaderThatIsNotAListOfIds() {
        assertThatThrownBy(() -> GatewayUser.from("RESIDENT", "tower-a"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Header X-Building-Id must hold building ids");
    }

    @Test
    void readsTheUserIdOfTheToken() {
        UUID userId = UUID.randomUUID();

        assertThat(GatewayUser.parseUserId(userId.toString())).isEqualTo(userId);
        assertThat(GatewayUser.parseUserId(null)).isNull();
        assertThat(GatewayUser.parseUserId(" ")).isNull();
    }
}
