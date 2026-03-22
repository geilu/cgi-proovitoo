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
            if (parsedGroupSize !== null && parsedGroupSize > 0) params.set("groupSize", String(parsedGroupSize));
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
    };

    return (
        <div className="bg-[#d6dae3] rounded-3xl p-6 flex flex-col gap-6 h-full">
            <h1 className="font-semibold text-[#3a3f4a] text-lg tracking-wide">Filters</h1>

            <div className="flex flex-col gap-3 flex-1">

                <div className="bg-[#c8cdd8] rounded-2xl px-4 py-3 flex flex-col gap-1">
                    <label htmlFor="filter-date-field" className="text-xs font-semibold text-[#5a6070] uppercase tracking-widest">
                        Reservation Date
                    </label>
                    <input id="filter-date-field" type="date" value={date} onChange={e => setDate(e.target.value)}
                        className="bg-transparent text-[#2e3340] font-medium text-sm outline-none w-full cursor-pointer"
                    />
                </div>

                <div className="bg-[#c8cdd8] rounded-2xl px-4 py-3 flex flex-col gap-1">
                    <label htmlFor="filter-time-field" className="text-xs font-semibold text-[#5a6070] uppercase tracking-widest">
                        Reservation Time
                    </label>
                    <input id="filter-time-field" type="time" value={timeOfDay} onChange={e => setTimeOfDay(e.target.value)}
                        className="bg-transparent text-[#2e3340] font-medium text-sm outline-none w-full cursor-pointer"
                    />
                </div>

                <div className="bg-[#c8cdd8] rounded-2xl px-4 py-3 flex flex-col gap-1">
                    <label htmlFor="filter-group-size" className="text-xs font-semibold text-[#5a6070] uppercase tracking-widest">
                        Group Size
                    </label>
                    <input id="filter-group-size" type="number" min={1} value={groupSize} placeholder="e.g. 3" onChange={e => setGroupSize(e.target.value)}
                        className="bg-transparent text-[#2e3340] font-medium text-sm outline-none w-full placeholder:text-[#9099aa]"
                    />
                </div>

                <div className="bg-[#c8cdd8] rounded-2xl px-4 py-3 flex flex-col gap-2">
                    <span className="text-xs font-semibold text-[#5a6070] uppercase tracking-widest">
                        Zone Preferences
                    </span>
                    <div className="flex flex-col gap-1.5">
                        {ZONES.map(zone => {
                            const active = zones.includes(zone.value);
                            return (
                                <label htmlFor="zone-preference-filter" key={zone.value}
                                    onClick={() => toggleZone(zone.value)}
                                    className={`
                                        flex items-center justify-between px-3 py-2 rounded-xl cursor-pointertransition-all duration-150 select-none
                                        ${active ? "bg-[#3a3f4a] text-white" : "bg-[#bcc2ce] text-[#3a3f4a] hover:bg-[#b0b7c5]"}
                                    `}
                                >
                                    <span className="text-sm font-medium">{zone.label}</span>
                                    <span className={`w-4 h-4 rounded-md border-2 flex items-center justify-center transition-all
                                        ${active ? "border-white bg-white" : "border-[#8a93a3]"}
                                    `}>
                                        {active && (
                                            <svg className="w-2.5 h-2.5 text-[#3a3f4a]" fill="none" viewBox="0 0 10 10">
                                                <path d="M1.5 5l2.5 2.5 4.5-4.5" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"/>
                                            </svg>
                                        )}
                                    </span>
                                </label>
                            );
                        })}
                    </div>
                </div>
            </div>

            {error && (
                <p className="text-red-500 text-xs font-medium text-center">{error}</p>
            )}

            <button
                onClick={applyFilters}
                disabled={loading}
                className="
                    w-full py-3 rounded-2xl font-semibold text-sm tracking-wide
                    bg-green-400 text-white
                    hover:bg-green-600 active:scale-[0.98]
                    disabled:opacity-50 disabled:cursor-not-allowed
                    transition-all duration-150
                "
            >
                {loading ? "Applying…" : "Apply Filters"}
            </button>
        </div>
    );
}