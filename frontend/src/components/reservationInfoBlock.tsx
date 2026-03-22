import {RestaurantTable} from "../types/RestaurantTable.ts";
import {useEffect, useState} from "react";
import {Reservation} from "../types/Reservation.ts";

function formatTime(isoString: string): string {
    // regex checks if the string from input is purely a date (no time specified), append T00:00 then
    const normalized = /^\d{4}-\d{2}-\d{2}$/.test(isoString) ? isoString + "T00:00" : isoString;
    return new Date(normalized).toLocaleTimeString(undefined, {hour: '2-digit', minute: '2-digit'});
}

function groupByDate(reservations: Reservation[]): Map<string, Reservation[]> {
    const map = new Map<string, Reservation[]>();
    for (const r of reservations) {
        const dateKey = new Date(r.startTime).toLocaleDateString('en-CA'); // YYYY-MM-DD
        if (!map.has(dateKey)) map.set(dateKey, []);
        map.get(dateKey)!.push(r);
    }
    return map;
}

function formatKey(key: string): string {
    return new Date(key + "T00:00").toLocaleDateString(undefined, {weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'});
}

interface ReservationInfoBlockProps {
    table: RestaurantTable;
    filterTime?: string | null;
}

export default function ReservationInfoBlock( {table, filterTime} : Readonly<ReservationInfoBlockProps>) {

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
            } catch {
                setError("Could not load reservations");
            } finally {
                setLoading(false);
            }
        };

        fetchReservations();
    }, [table.id]);

    if (loading) return <p>Loading...</p>;
    if (error) return <p>{error}</p>;

    const filterParts = filterTime ? filterTime.split("T") : [];
    const filteredDateKey = filterParts[0]?.match(/^\d{4}-\d{2}-\d{2}$/)
        ? filterParts[0]
        : null;

    const visibleEntries = Array.from(groupByDate(reservations).entries())
        .filter(([key]) => !filteredDateKey || key === filteredDateKey);

    return (
        <div className="flex flex-col text-left gap-[2em]">
            <p className="font-bold text-xl">Table #{table.tableNumber} - Seats {table.capacity} people</p>
            {visibleEntries.length === 0 && (<p>{filteredDateKey ? `No reservations on ${formatKey(filteredDateKey)}` : "No reservations"}</p>)}
            {visibleEntries.map(([key, dayReservations]) => (
                <div key={key} className="flex flex-col gap-2">
                    <b>Reserved on {formatKey(key)} during:</b>
                    {dayReservations.map(reservation => {
                        return (
                            <p key={reservation.id}>{formatTime(reservation.startTime)} - {formatTime(reservation.endTime)}</p>
                        );
                    })}
                </div>
            ))}
        </div>
    );
}