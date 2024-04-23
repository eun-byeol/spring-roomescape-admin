package roomescape.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.dto.ReservationResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationControllerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DisplayName("전체 예약 조회")
    @Test
    void findAllReservations() {
        jdbcTemplate.update("insert into reservation_time (start_at) values (?)", "15:40");
        String insertSql = "insert into reservation (name, date, time_id) values (?, ?, ?)";
        jdbcTemplate.update(insertSql, "브라운", "2023-08-05", 1L);

        List<ReservationResponse> reservations = RestAssured.given().log().all()
            .when().get("/reservations")
            .then().log().all()
            .statusCode(200).extract()
            .jsonPath().getList(".", ReservationResponse.class);

        Integer count = countReservations();
        assertThat(reservations).hasSize(count);
    }

    @DisplayName("예약 추가 후 조회")
    @Test
    void insertAndSearchReservation() {
        String insertSql = "insert into reservation_time (start_at) values (?)";
        jdbcTemplate.update(insertSql, "15:40");

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", "2023-08-05");
        reservation.put("timeId", 1);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(reservation)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(201);

        RestAssured.given().log().all()
            .when().get("/reservations")
            .then().log().all()
            .statusCode(200)
            .body("size()", is(1));
    }

    @DisplayName("예약 추가 후 삭제")
    @Test
    void insertAndRemoveReservation() {
        String insertSql = "insert into reservation_time (start_at) values (?)";
        jdbcTemplate.update(insertSql, "15:40");

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", "2023-08-05");
        reservation.put("timeId", 1);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(reservation)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(201)
            .header("Location", "/reservations/1");

        Integer count = countReservations();
        assertThat(count).isEqualTo(1);

        RestAssured.given().log().all()
            .when().delete("/reservations/1")
            .then().log().all()
            .statusCode(204);

        Integer countAfterDelete = countReservations();
        assertThat(countAfterDelete).isZero();
    }

    private Integer countReservations() {
        String selectSql = "SELECT count(1) from reservation";
        return jdbcTemplate.queryForObject(selectSql, Integer.class);
    }
}
