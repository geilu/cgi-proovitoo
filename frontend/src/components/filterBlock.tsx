export default function FilterBlock() {
    return (
        <>
            <h1 className="font-bold text-xl text-center">Filters</h1>

            <div className="flex flex-col justify-evenly h-full">
                <div className="flex flex-col gap-[1em]">
                    <span id="filter-field-container">
                        <label htmlFor="reservation-time-filter" className="w-full text-end font-bold">Reservation Time</label>
                        <input id="reservation-time-filter" type="datetime-local" className="border-[2px] p-[0.1em]"/>
                    </span>

                    <span id="filter-field-container">
                        <label htmlFor="group-size-filter" className="w-full text-end font-bold">Group Size</label>
                        <input id="group-size-filter" type="number" className="border-[2px] p-[0.1em]" />
                    </span>

                    <span id="filter-field-container">
                        <label htmlFor="zone-preference-filter" className="w-full text-end font-bold">Zone Preferences</label>
                        <select id="zone-preference-filter" className="border-[2px] p-[0.1em]">
                            <option value="QUIET">Quiet Area</option>
                            <option value="CHILDREN">Children Area</option>
                            <option value="TERRACE">Terrace Area</option>
                        </select>
                    </span>
                </div>

                <button className="rounded-xl bg-red-500 p-[1em]">Apply filters</button>
            </div>
        </>
    )
}