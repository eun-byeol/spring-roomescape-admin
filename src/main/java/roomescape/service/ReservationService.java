package roomescape.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;

public class ReservationService {

    private final List<Reservation> reservations = new ArrayList<>(
        List.of(
            new Reservation(1L, "jojo", "2024-05-20", new ReservationTime(1L, "10:00")),
            new Reservation(2L, "waterfall", "2024-05-21", new ReservationTime(2L, "11:00")),
            new Reservation(3L, "mia", "2024-05-22", new ReservationTime(3L, "12:00"))
        )
    );
    private final Long initialSize = reservations.size() + 1L;
    private final AtomicLong reservationId = new AtomicLong(initialSize);
    private final AtomicLong timeId = new AtomicLong(initialSize);

    public List<ReservationResponse> findAll() {
        return reservations.stream()
            .map(ReservationResponse::from)
            .toList();
    }

    public ReservationResponse create(ReservationRequest request) {
        ReservationTime reservationTime = new ReservationTime(
            timeId.getAndIncrement(),
            request.time()
        );

        Reservation reservation = new Reservation(
            reservationId.getAndIncrement(),
            request.name(),
            request.date(),
            reservationTime
        );

        reservations.add(reservation);
        return ReservationResponse.from(reservation);
    }

    public void delete(Long id) {
        reservations.removeIf(reservation -> reservation.getId().equals(id));
    }
}
