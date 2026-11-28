package com.rumi.notification.infrastructure.web;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GatewayUserTest {

    private static final UUID USER_ID = UUID.fromString("c1d2e3f4-a5b6-4c7d-8e9f-0a1b2c3d4e5f");
    private static final UUID BUILDING_ID = UUID.fromString("7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d");
    private static final UUID OTHER_BUILDING_ID = UUID.fromString("1f2e3d4c-5b6a-4978-8695-a4b3c2d1e0f9");

    private static GatewayUser user(String role, String buildingIds) {
        return GatewayUser.from(USER_ID.toString(), role, buildingIds);
    }

    @Test
    void readsTheUserAndTheCommaSeparatedBuildingsOfTheToken() {
        GatewayUser user = user("RESIDENT", " " + BUILDING_ID + " ," + OTHER_BUILDING_ID + ",");

        assertThat(user.userId()).isEqualTo(USER_ID);
        assertThat(user.role()).isEqualTo("RESIDENT");
        assertThat(user.buildingIds()).containsExactlyInAnyOrder(BUILDING_ID, OTHER_BUILDING_ID);
    }

    @Test
    void aResidentOnlyReachesTheBuildingsOfTheToken() {
        GatewayUser user = user("RESIDENT", BUILDING_ID.toString());

        assertThatCode(() -> user.requireAccessTo(BUILDING_ID)).doesNotThrowAnyException();
        assertThatThrownBy(() -> user.requireAccessTo(OTHER_BUILDING_ID))
                .isInstanceOf(BuildingAccessDeniedException.class)
                .hasMessage("You do not have access to building " + OTHER_BUILDING_ID);
    }

    @Test
    void anAdministratorOnlyReachesTheBuildingsOfTheToken() {
        GatewayUser user = user("ADMINISTRATOR", BUILDING_ID.toString());

        assertThatCode(() -> user.requireAccessTo(BUILDING_ID)).doesNotThrowAnyException();
        assertThatThrownBy(() -> user.requireAccessTo(OTHER_BUILDING_ID))
                .isInstanceOf(BuildingAccessDeniedException.class);
    }

    @Test
    void aUserWithoutBuildingsReachesNone() {
        assertThatThrownBy(() -> user("ADMINISTRATOR", null).requireAccessTo(BUILDING_ID))
                .isInstanceOf(BuildingAccessDeniedException.class);
        assertThatThrownBy(() -> user("RESIDENT", " ").requireAccessTo(BUILDING_ID))
                .isInstanceOf(BuildingAccessDeniedException.class);
    }

    @Test
    void aRoleOutsideTheBuildingRolesReachesNoBuilding() {
        assertThatThrownBy(() -> user("PLATFORM_ADMIN", BUILDING_ID.toString()).requireAccessTo(BUILDING_ID))
                .isInstanceOf(BuildingAccessDeniedException.class);
    }

    @Test
    void onlyTheAdministratorOfTheBuildingCanChangeIt() {
        assertThatCode(() -> user("ADMINISTRATOR", BUILDING_ID.toString()).requireAdministratorOf(BUILDING_ID))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> user("RESIDENT", BUILDING_ID.toString()).requireAdministratorOf(BUILDING_ID))
                .isInstanceOf(BuildingAccessDeniedException.class)
                .hasMessage("Only the administrator of the building can do this");
        assertThatThrownBy(() -> user("ADMINISTRATOR", OTHER_BUILDING_ID.toString())
                .requireAdministratorOf(BUILDING_ID))
                .isInstanceOf(BuildingAccessDeniedException.class)
                .hasMessage("You do not have access to building " + BUILDING_ID);
    }

    @Test
    void aRequestWithoutUserIdOrRoleHasNoIdentity() {
        assertThatThrownBy(() -> GatewayUser.from(null, null, null))
                .isInstanceOf(MissingGatewayIdentityException.class)
                .hasMessage("The request has no user identity from the API gateway");
        assertThatThrownBy(() -> GatewayUser.from(null, "ADMINISTRATOR", BUILDING_ID.toString()))
                .isInstanceOf(MissingGatewayIdentityException.class);
        assertThatThrownBy(() -> GatewayUser.from(USER_ID.toString(), " ", BUILDING_ID.toString()))
                .isInstanceOf(MissingGatewayIdentityException.class);
    }

    @Test
    void rejectsABuildingHeaderThatIsNotAListOfIds() {
        assertThatThrownBy(() -> user("RESIDENT", "tower-a"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Header X-Building-Id must hold building ids");
    }

    @Test
    void rejectsAUserIdHeaderThatIsNotAnId() {
        assertThatThrownBy(() -> GatewayUser.from("not-a-uuid", "RESIDENT", BUILDING_ID.toString()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Header X-User-Id must hold a user id");
    }
}
