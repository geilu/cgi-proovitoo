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

    const inputCls = "border-[2px] rounded px-2 py-1 text-sm w-full";
    const errorCls = "col-span-2 text-xs text-red-600 -mt-3 text-right";

    const panelCls = `p-[2em] h-[100vh] content-center bg-gray-300 fixed right-0 top-0 ml-auto mr-0
        transition-transform duration-300 ease-in-out
        ${isOpen ? "translate-x-0" : "translate-x-full"}`;

    if (submitted) {
        return (
            <div className={panelCls}>
                <div className="text-center flex flex-col gap-3">
                    <h1 className="text-2xl font-bold">Reservation confirmed!</h1>
                    <p className="text-sm text-gray-600">
                        Table {selectedTable?.tableNumber} · {date} · {startTime}–{
                        (() => {
                            const end = new Date(new Date(`${date}T${startTime}`).getTime() + 2 * 60 * 60 * 1000);
                            return end.toTimeString().slice(0, 5);
                        })()
                    } · {guestCount} guests
                    </p>
                </div>
            </div>
        );
    }

    return (
        <div className={panelCls}>
            <h1 className="text-2xl font-bold text-center mb-[1em]">Make reservation</h1>
            <div className="grid grid-cols-2 w-full gap-4 text-center items-center">

                <label className="font-bold" htmlFor="form-selected-table-fied">Table</label>
                <input
                    id="form-selected-table-field"
                    className={inputCls}
                    value={selectedTable ? `#${selectedTable.tableNumber} (${selectedTable.capacity} seats)` : "Select a table from the plan"}
                    readOnly
                />

                <label className="font-bold" htmlFor="form-reservation-date-field">Date</label>
                <input
                    id="form-reservation-date-field"
                    className={inputCls}
                    type="date"
                    value={date}
                    min={new Date().toISOString().slice(0, 10)}
                    onChange={e => setDate(e.target.value)}
                />

                <label className="font-bold" htmlFor="form-reservation-time-field">Time</label>
                <input
                    id="form-reservation-time-field"
                    className={inputCls}
                    type="time"
                    value={startTime}
                    onChange={e => setStartTime(e.target.value)}
                />
                {errors.startTime && <p className={errorCls}>{errors.startTime}</p>}

                <label className="font-bold" htmlFor="form-guest-count-field">Guests</label>
                <input
                    id="form-guest-count-field"
                    className={inputCls}
                    type="number"
                    min={1}
                    max={selectedTable?.capacity ?? 20}
                    value={guestCount}
                    onChange={e => setGuestCount(Number(e.target.value))}
                />
                {errors.guestCount && <p className={errorCls}>{errors.guestCount}</p>}

            </div>

            {apiError && (
                <p className="text-sm text-red-600 bg-red-100 border border-red-300 rounded px-3 py-2 mt-4">
                    {apiError}
                </p>
            )}

            <div className="flex gap-3 mt-[2em]">
                {onCancel && (
                    <button
                        type="button"
                        onClick={onCancel}
                        disabled={loading}
                        className="font-bold bg-gray-400 p-[2em] w-full rounded-xl disabled:opacity-50"
                    >
                        Cancel
                    </button>
                )}
                <button
                    type="button"
                    onClick={handleSubmit}
                    disabled={loading || !selectedTable}
                    className="font-bold bg-gray-400 p-[2em] w-full rounded-xl disabled:opacity-50"
                >
                    {loading ? "Confirming…" : "Confirm"}
                </button>
            </div>
        </div>
    );
}