import {useState} from "react";
import {FilteredTableResponse} from "../types/FilteredTableResponse.ts";

const ZONES = [
    { value: "QUIET", label: "Quiet Area" },
    { value: "CHILDREN", label: "Children Area" },
    { value: "TERRACE", label: "Terrace Area" },
];

const TODAY = new Date().toLocaleDateString('en-CA');

function toISODateTime(localDatetime: string): string {
    const normalized = /^\d{4}-\d{2}-\d{2}$/.test(localDatetime)
        ? localDatetime + "T00:00"
        : localDatetime;
    return new Date(normalized).toISOString();
}

export default function FilterBlock({ onFilter }: Readonly<{ onFilter: (result: FilteredTableResponse) => void }>) {
    const [date, setDate] = useState(TODAY);
    const [timeOfDay, setTimeOfDay] = useState("");
    const [groupSize, setGroupSize] = useState("");
    const [zones, setZones] = useState<string[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const today = new Date().toLocaleDateString('en-CA'); // YYYY-MM-DD
    const selectedTime = date && timeOfDay
        ? `${date}T${timeOfDay}`
        : date
            ? date
            : timeOfDay
                ? `${today}T${timeOfDay}`  // default to today when only time is picked
                : "";

    const parsedGroupSize = groupSize === "" ? null : parseInt(groupSize, 10);

    const toggleZone = (zone: string) =>
        setZones(prev => prev.includes(zone) ? prev.filter(z => z !== zone) : [...prev, zone]);

    const applyFilters = async () => {
        setLoading(true);
        setError(null);
        try {
            const params = new URLSearchParams();
            if (selectedTime) params.set("time", toISODateTime(selectedTime));
            if (parsedGroupSize !== null && parsedGroupSize > 0)params.set("groupSize", String(parsedGroupSize));
            zones.forEach(z => params.append("userPreferences", z));

            const response = await fetch(`/api/tables/filtered?${params}`);
            if (!response.ok) throw new Error("Failed to fetch filtered tables");
            const data: FilteredTableResponse = await response.json();
            data.selectedTime = selectedTime;
            data.guestCount = parsedGroupSize ?? 2;
            onFilter(data);
        } catch {
            setError("Could not apply filters");
        } finally {
            setLoading(false);
        }
    }

    return (
        <>
            <h1 className="font-bold text-xl text-center">Filters</h1>

            <div className="flex flex-col justify-evenly h-full">
                <div className="flex flex-col gap-[1em]">
                    <span id="filter-field-container">
                        <label htmlFor="reservation-date-filter" className="w-full text-end font-bold">Reservation Date</label>
                        <input id="reservation-date-filter" type="date" className="border-[2px] p-[0.1em]"
                            value={date} onChange={e => setDate(e.target.value)}/>
                    </span>

                    <span id="filter-field-container">
                        <label htmlFor="reservation-time-filter" className="w-full text-end font-bold">Reservation Time</label>
                        <input id="reservation-time-filter" type="time" className="border-[2px] p-[0.1em]"
                            value={timeOfDay} onChange={e => setTimeOfDay(e.target.value)} />
                    </span>

                    <span id="filter-field-container">
                        <label htmlFor="group-size-filter" className="w-full text-end font-bold">Group Size</label>
                        <input id="group-size-filter" type="number" min={1} className="border-[2px] p-[0.1em]"
                            value={groupSize} placeholder="3" onChange={e => setGroupSize(e.target.value)} />
                    </span>

                    <span id="filter-field-container">
                        <label htmlFor="zone-preference-filter" className="w-full text-end font-bold">Zone Preferences</label>
                        {ZONES.map(zone => (
                            <label key={zone.value} className="flex items-center gap-2 justify-end cursor-pointer">
                                {zone.label}
                                <input type="checkbox" checked={zones.includes(zone.value)}
                                       onChange={() => toggleZone(zone.value)} />
                            </label>
                        ))}
                    </span>
                </div>

                {error && <p className="text-red-500 text-sm">{error}</p>}
                <button onClick={applyFilters} disabled={loading} className="rounded-xl bg-red-500 p-[1em]">{loading ? "Applying..." : "Apply Filters"}</button>
            </div>
        </>
    )
}