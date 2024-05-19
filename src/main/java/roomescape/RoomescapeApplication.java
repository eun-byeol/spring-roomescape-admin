package roomescape;

import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.staticFiles;

import com.google.gson.Gson;
import java.nio.file.Files;
import java.nio.file.Paths;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.dto.StandardResponse;
import roomescape.dto.StatusResponse;
import roomescape.service.ReservationService;

public class RoomescapeApplication {

    public static void main(String[] args) {
        staticFiles.location("public");

        ReservationService reservationService = new ReservationService();

        // 정적 페이지 api
        get("/admin", (req, res) -> {
            res.type("text/html");
            return new String(
                Files.readAllBytes(Paths.get("src/main/resources/templates/admin/index.html")));
        });

        get("/admin/reservation", (req, res) -> {
            res.type("text/html");
            return new String(Files.readAllBytes(
                Paths.get("src/main/resources/templates/admin/reservation-legacy.html")));
        });

        get("/admin/time", (req, res) -> {
            res.type("text/html");
            return new String(
                Files.readAllBytes(Paths.get("src/main/resources/templates/admin/time.html")));
        });

        // rest api
        get("/reservations", (req, res) -> {
            res.type("application/json");
            return new Gson().toJson(
                new StandardResponse(
                    StatusResponse.SUCCESS,
                    new Gson().toJsonTree(reservationService.findAll())
                )
            );
        });

        post("/reservations", (request, response) -> {
            response.type("application/json");

            ReservationRequest reservationRequest = new Gson().fromJson(request.body(),
                ReservationRequest.class);
            ReservationResponse reservationResponse = reservationService.create(reservationRequest);

            return new Gson().toJson(
                new StandardResponse(
                    StatusResponse.SUCCESS,
                    new Gson().toJsonTree(reservationResponse)
                )
            );
        });

        delete("/reservations/:id", (request, response) -> {
            response.type("application/json");

            reservationService.delete(Long.parseLong(request.params(":id")));
            return new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS, "user deleted"));
        });
    }
}
