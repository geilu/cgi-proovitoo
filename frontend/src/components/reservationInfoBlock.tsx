import {RestaurantTable} from "../types/RestaurantTable.ts";
import {useEffect, useState} from "react";
import {Reservation} from "../types/Reservation.ts";

function formatReservationTime(isoString: string): { date: string; time: string } {
    const date = new Date(isoString);
    return {
        date: date.toLocaleDateString(undefined, { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' }),
        time: date.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' }),
    };
}

function groupByDate(reservations: Reservation[]): Map<string, Reservation[]> {
    const map = new Map<string, Reservation[]>();
    for (const r of reservations) {
        const { date } = formatReservationTime(r.startTime);
        if (!map.has(date)) map.set(date, []);
        map.get(date)!.push(r);
    }
    return map;
}

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

    const grouped = groupByDate(reservations);

    return (
        <div className="flex flex-col text-left gap-[2em]">
            <p className="font-bold text-xl">Table #{table.tableNumber} - Seats {table.capacity} people</p>
            {grouped.size === 0 && <p className="text-gray-400">No reservations</p>}
            {Array.from(grouped.entries()).map(([date, dayReservations]) => (
                <div key={date} className="flex flex-col gap-2">
                    <b>Reserved on {date}:</b>
                    {dayReservations.map(reservation => {
                        const { time: startTime } = formatReservationTime(reservation.startTime);
                        const { time: endTime } = formatReservationTime(reservation.endTime);
                        return (
                            <p key={reservation.id}>{startTime} - {endTime}</p>
                        );
                    })}
                </div>
            ))}
        </div>
    );
}