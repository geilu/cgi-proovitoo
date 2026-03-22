import { useState, useEffect } from "react";
import { RestaurantTable } from "../types/RestaurantTable.ts";
import { FilteredTableResponse } from "../types/FilteredTableResponse.ts";
import { User } from "../types/User.ts";

// AI assistance was used for the functionality of this component

interface ReservationFormProps {
    isOpen: boolean;
    selectedTable: RestaurantTable | null;
    filterResult: FilteredTableResponse | null;
    user: User;
    onCancel?: () => void;
}

interface FormErrors {
    startTime?: string;
    guestCount?: string;
}

function prefillDate(value: string | undefined): string {
    if (!value) return "";
    const d = new Date(value);
    return Number.isNaN(d.getTime()) ? "" : d.toISOString().slice(0, 10);
}

function prefillTime(value: string | undefined): string {
    if (!value) return "";
    if (/^\d{2}:\d{2}$/.test(value)) return value;
    const d = new Date(value);
    return Number.isNaN(d.getTime()) ? "" : d.toTimeString().slice(0, 5);
}

export default function ReservationForm({
                                            isOpen,
                                            selectedTable,
                                            filterResult,
                                            user,
                                            onCancel,
                                        }: Readonly<ReservationFormProps>) {
    const [date, setDate]           = useState(() => prefillDate(filterResult?.selectedTime));
    const [startTime, setStartTime] = useState(() => prefillTime(filterResult?.selectedTime));
    const [guestCount, setGuestCount] = useState(0);
    const [errors, setErrors]       = useState<FormErrors>({});
    const [submitted, setSubmitted] = useState(false);
    const [loading, setLoading]     = useState(false);
    const [apiError, setApiError]   = useState<string | null>(null);

    useEffect(() => {
        setSubmitted(false);
        setApiError(null);
        setErrors({});
    }, [selectedTable?.id]);

    useEffect(() => { // prefilling data from selected filters
        if (!isOpen) return;
        setDate(prefillDate(filterResult?.selectedTime));
        setStartTime(prefillTime(filterResult?.selectedTime));
        setGuestCount(filterResult?.guestCount ?? 2);
    }, [isOpen]);

    function validate(): boolean {
        const next: FormErrors = {};
        const now = new Date();

        if (!startTime) {
            next.startTime = "Start time is required.";
        } else if (date && new Date(`${date}T${startTime}`) < now) {
            next.startTime = "Start time cannot be in the past.";
        }

        if (!guestCount || guestCount < 1) {
            next.guestCount = "Guest count must be at least 1.";
        } else if (selectedTable && guestCount > selectedTable.capacity) {
            next.guestCount = `Exceeds capacity of ${selectedTable.capacity}.`;
        }

        setErrors(next);
        return Object.keys(next).length === 0;
    }

    async function handleSubmit() {
        if (!selectedTable || !date || !validate()) return;

        const start = new Date(`${date}T${startTime}`);
        const end = new Date(start.getTime() + 2 * 60 * 60 * 1000);

        const body = {
            user,
            guestCount,
            startTime: start.toISOString(),
            endTime: end.toISOString(),
            restaurantTable: selectedTable,
            status: "PENDING",
        };

        setLoading(true);
        setApiError(null);
        try {
            const res = await fetch("/api/reservations", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(body),
            });
            if (res.status === 409) {
                setApiError("Table already reserved for this time. Please choose a different time or table.");
                return;
            }
            if (!res.ok) {
                const data = await res.json().catch(() => ({}));
                throw new Error(data?.message ?? `Request failed (${res.status})`);
            }
            setSubmitted(true);
        } catch (err) {
            setApiError(err instanceof Error ? err.message : "Something went wrong. Please try again.");
        } finally {
            setLoading(false);
        }
    }

    const panelCls = `p-6 h-[55vh] bg-[#d6dae3] fixed left-0 bottom-0 flex flex-col gap-4 justify-center
        transition-transform duration-300 ease-in-out w-[24em] shadow-2xl rounded-2xl
        ${isOpen ? "translate-x-0" : "-translate-x-full"}`;

    if (submitted) {
        return (
            <div className={panelCls}>
                <div className="bg-[#c8cdd8] rounded-2xl px-4 py-3 flex flex-col gap-1 text-center">
                    <span className="text-xs font-semibold text-[#5a6070] uppercase tracking-widest">Confirmed</span>
                    <span className="text-[#2e3340] font-semibold text-sm">
                    Table {selectedTable?.tableNumber} · {date} · {startTime}–{
                        (() => {
                            const end = new Date(new Date(`${date}T${startTime}`).getTime() + 2 * 60 * 60 * 1000);
                            return end.toTimeString().slice(0, 5);
                        })()
                    } · {guestCount} guests
                </span>
                </div>

                {onCancel && (
                    <button
                        type="button"
                        onClick={onCancel}
                        className="w-full py-3 rounded-2xl font-semibold text-sm tracking-wide bg-[#c8cdd8] text-[#3a3f4a] hover:bg-[#bcc2ce] active:scale-[0.98] transition-all duration-150"
                    >
                        Close
                    </button>
                )}
            </div>
        );
    }

    return (
        <div className={panelCls}>
            <h1 className="font-semibold text-[#3a3f4a] text-lg tracking-wide">Make Reservation</h1>

            <div className="flex flex-col gap-3">

                <div className="bg-[#c8cdd8] rounded-2xl px-4 py-3 flex flex-col gap-1">
                    <label htmlFor="form-selected-table-field" className="text-xs font-semibold text-[#5a6070] uppercase tracking-widest">Table</label>
                    <input
                        id="form-selected-table-field"
                        value={selectedTable ? `#${selectedTable.tableNumber} (${selectedTable.capacity} seats)` : "Select a table from the plan"}
                        readOnly
                        className="bg-transparent text-[#2e3340] font-medium text-sm outline-none w-full"
                    />
                </div>

                <div className="bg-[#c8cdd8] rounded-2xl px-4 py-3 flex flex-col gap-1">
                    <label htmlFor="form-reservation-date-field" className="text-xs font-semibold text-[#5a6070] uppercase tracking-widest">Date</label>
                    <input
                        id="form-reservation-date-field"
                        type="date"
                        value={date}
                        min={new Date().toISOString().slice(0, 10)}
                        onChange={e => setDate(e.target.value)}
                        className="bg-transparent text-[#2e3340] font-medium text-sm outline-none w-full cursor-pointer"
                    />
                </div>

                <div className="bg-[#c8cdd8] rounded-2xl px-4 py-3 flex flex-col gap-1">
                    <label htmlFor="form-reservation-time-field" className="text-xs font-semibold text-[#5a6070] uppercase tracking-widest">Time</label>
                    <input
                        id="form-reservation-time-field"
                        type="time"
                        value={startTime}
                        onChange={e => setStartTime(e.target.value)}
                        className="bg-transparent text-[#2e3340] font-medium text-sm outline-none w-full cursor-pointer"
                    />
                    {errors.startTime && <p className="text-xs text-red-500 mt-0.5">{errors.startTime}</p>}
                </div>

                <div className="bg-[#c8cdd8] rounded-2xl px-4 py-3 flex flex-col gap-1">
                    <label htmlFor="form-guest-count-field" className="text-xs font-semibold text-[#5a6070] uppercase tracking-widest">Guests</label>
                    <input
                        id="form-guest-count-field"
                        type="number"
                        min={1}
                        max={selectedTable?.capacity ?? 20}
                        value={guestCount}
                        onChange={e => setGuestCount(Number(e.target.value))}
                        className="bg-transparent text-[#2e3340] font-medium text-sm outline-none w-full"
                    />
                    {errors.guestCount && <p className="text-xs text-red-500 mt-0.5">{errors.guestCount}</p>}
                </div>

            </div>

            {apiError && (
                <p className="text-xs text-red-500 font-medium text-center">{apiError}</p>
            )}

            <div className="flex gap-3">
                {onCancel && (
                    <button
                        type="button"
                        onClick={onCancel}
                        disabled={loading}
                        className="w-full py-3 rounded-2xl font-semibold text-sm tracking-wide bg-[#c8cdd8] text-[#3a3f4a] hover:bg-[#bcc2ce] active:scale-[0.98] disabled:opacity-50 transition-all duration-150"
                    >
                        Cancel
                    </button>
                )}
                <button
                    type="button"
                    onClick={handleSubmit}
                    disabled={loading || !selectedTable}
                    className="w-full py-3 rounded-2xl font-semibold text-sm tracking-wide bg-green-400 text-white hover:bg-green-600 active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-150"
                >
                    {loading ? "Confirming…" : "Confirm"}
                </button>
            </div>
        </div>
    );
}