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

    if (loading) return (
        <div className="bg-[#d6dae3] rounded-xl p-[1em] w-[20em]">
            <p>Loading...</p>
        </div>
    );
    if (error) return (
        <div className="bg-[#d6dae3] rounded-xl p-[1em] w-[20em]">
            <p>{error}</p>
        </div>
    );

    const filterParts = filterTime ? filterTime.split("T") : [];
    const filteredDateKey = filterParts[0]?.match(/^\d{4}-\d{2}-\d{2}$/)
        ? filterParts[0]
        : null;

    const visibleEntries = Array.from(groupByDate(reservations).entries())
        .filter(([key]) => !filteredDateKey || key === filteredDateKey);

    return (
        <div className="bg-[#d6dae3] rounded-3xl p-6 flex flex-col gap-4 h-full w-[20em]">

            <div className="bg-[#c8cdd8] rounded-2xl px-4 py-3 flex flex-col gap-0.5">
                <span className="text-xs font-semibold text-[#5a6070] uppercase tracking-widest">Selected Table</span>
                <span className="text-[#2e3340] font-semibold text-sm">
                    Table #{table.tableNumber} &mdash; Seats {table.capacity} people
                </span>
            </div>

            <div className="flex flex-col gap-3 flex-1 overflow-y-auto">
                {visibleEntries.length === 0 && (
                    <div className="bg-[#c8cdd8] rounded-2xl px-4 py-3">
                        <p className="text-sm text-[#5a6070]">
                            {filteredDateKey ? `No reservations on ${formatKey(filteredDateKey)}` : "No reservations"}
                        </p>
                    </div>
                )}
                {visibleEntries.map(([key, dayReservations]) => (
                    <div key={key} className="bg-[#c8cdd8] rounded-2xl px-4 py-3 flex flex-col gap-2">
                        <span className="text-xs font-semibold text-[#5a6070] uppercase tracking-widest">
                            {formatKey(key)}
                        </span>
                        {dayReservations.map(reservation => (
                            <div key={reservation.id}
                                 className="flex items-center justify-between bg-[#bcc2ce] rounded-xl px-3 py-2">
                                <span className="text-sm font-medium text-[#2e3340]">
                                    {formatTime(reservation.startTime)}
                                </span>
                                <span className="text-xs text-[#5a6070]">→</span>
                                <span className="text-sm font-medium text-[#2e3340]">
                                    {formatTime(reservation.endTime)}
                                </span>
                            </div>
                        ))}
                    </div>
                ))}
            </div>
        </div>
    );
}