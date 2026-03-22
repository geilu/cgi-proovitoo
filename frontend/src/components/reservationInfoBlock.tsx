import {RestaurantTable} from "../types/RestaurantTable.ts";
import {useEffect, useState} from "react";
import {Reservation} from "../types/Reservation.ts";

export default function ReservationInfoBlock( {table} : Readonly<{ table: RestaurantTable }>) {

    const [reservations, setReservations] = useState<Reservation[]>([])
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchReservations = async () => {
            setLoading(true);
            try {
                const reservations = await fetch(`/api/reservations/oftable?table=${table.id}`);
                if (!reservations.ok) throw new Error("Failed to fetch reservations for table");
                const data: Reservation[] = await reservations.json();
                setReservations(data);
            } catch (err) {
                setError("Could not load reservations");
            } finally {
                setLoading(false);
            }
        };

        fetchReservations();
    }, [table.id]);

    if (loading) return <p>Loading...</p>;
    if (error) return <p>{error}</p>;

    return (
        <>
            <p>Table #{table.tableNumber}</p>
            {reservations.map(reservation => (
                <div key={reservation.id}>
                    <p>{reservation.startTime} - {reservation.endTime}</p>
                </div>
            ))}
        </>
    )
}