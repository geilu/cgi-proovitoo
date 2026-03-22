import {SetStateAction, useState} from 'react';

import FilterBlock from "./components/filterBlock.tsx";
import ReservationInfoBlock from "./components/reservationInfoBlock.tsx";
import RestaurantPlan from "./components/restaurantPlan.tsx";
import ReservationForm from "./components/reservationForm.tsx";

import {RestaurantTable} from "./types/RestaurantTable.ts";
import {FilteredTableResponse} from "./types/FilteredTableResponse.ts";
import {User} from "./types/User.ts";

export const testUser: User = {
    id: 1,
    firstName: "Aadu",
    lastName: "Beedu",
}

function App() {
    document.body.style.backgroundColor = "white";

    const [selectedTable, setSelectedTable] = useState<RestaurantTable | null>(null);
    const [filterResult, setFilterResult] = useState<FilteredTableResponse | null>(null);
    const [formOpen, setFormOpen] = useState<boolean>(false);

    const handleSelect = (table: RestaurantTable | null) => {
        setSelectedTable(table);
        document.querySelectorAll('.table-obj').forEach(el => el.classList.remove('active'));

        if (table) {
            document.getElementById(`table-${table.id}`)?.classList.add('active');
        }
    }

    const loadReservationInfo = () => {
        if (selectedTable === null) {
            return (
            <p className="font-bold text-lg">Select a table to see its reservation info</p>
            )
        } else {
            return (
            <ReservationInfoBlock table={selectedTable} filterTime={filterResult?.selectedTime} />
            )
        }
    }

    return(
        <>
            <div className="flex flex-row flex-1 w-full justify-center gap-[2em] mt-[5em] overflow-visible">
                <div id="layout-left" className="flex flex-col gap-[1em]">

                    <div id="legend" className="flex flex-row justify-evenly p-[0.5em]">
                        <div className="flex flex-row gap-[0.5em] text-center">
                            <div className="p-[1em] px-[1.3em] bg-gray-400 rounded-lg text-xs font-bold">4</div>
                            <p className="mt-[0.5em]">Available (4 seats)</p>
                        </div>
                        <div className="flex flex-row gap-[0.5em] text-center">
                            <div className="p-[1em] px-[1.3em] bg-gray-400 rounded-lg opacity-30 text-xs font-bold">4</div>
                            <p className="mt-[0.5em]">Filtered out</p>
                        </div>
                        <div className="flex flex-row gap-[0.5em] text-center">
                            <div className="p-[1em] px-[1.3em] bg-green-500 rounded-lg text-xs font-bold">4</div>
                            <p className="mt-[0.5em]">Recommended</p>
                        </div>
                    </div>

                    <div id="grid-container" className="flex p-[1.5em] h-[40em] bg-gray-300 rounded-xl">
                        <RestaurantPlan onSelect={handleSelect}
                                        filteredIds={filterResult?.filteredTables.map(t => t.id) ?? null}
                                        recommendedId={filterResult?.recommendedTable?.id ?? null}/>
                    </div>
                </div>

                <div id="layout-right" className="flex flex-col gap-[2em]">
                    <div id="filter-container" className="bg-red-400 rounded-xl h-full w-full p-[2em]">
                        <FilterBlock onFilter={setFilterResult} />
                    </div>
                    <div id="reservation-info-container" className="bg-blue-400 rounded-xl h-full w-full p-[2em] text-center content-center">
                        {loadReservationInfo()}
                    </div>
                    <button className="p-[2em] rounded-xl bg-blue-500"
                                onClick={() => setFormOpen(true)}>make reservation</button>
                </div>
            </div>
            <ReservationForm isOpen={formOpen} selectedTable={selectedTable} filterResult={filterResult} user={testUser} onCancel={() => setFormOpen(false)} />
        </>
    )
}

export default App