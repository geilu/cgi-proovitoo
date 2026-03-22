import {useState} from 'react';

import FilterBlock from "./components/filterBlock.tsx";
import ReservationInfoBlock from "./components/reservationInfoBlock.tsx";
import RestaurantPlan from "./components/restaurantPlan.tsx";

import {RestaurantTable} from "./types/RestaurantTable.ts";
import {FilteredTableResponse} from "./types/FilteredTableResponse.ts";

function App() {
    document.body.style.backgroundColor = "white";

    const [selectedTable, setSelectedTable] = useState<RestaurantTable | null>(null);
    const [filterResult, setFilterResult] = useState<FilteredTableResponse | null>(null);

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
        <div className="flex flex-row flex-1 w-full justify-center gap-[2em] mt-[5em]">
            <div id="grid-container" className="flex p-[3em] bg-green-400 rounded-xl">
                <RestaurantPlan onSelect={setSelectedTable}
                                filteredIds={filterResult?.filteredTables.map(t => t.id) ?? null}
                                recommendedId={filterResult?.recommendedTable?.id ?? null}/>
            </div>
            <div id="layout-right" className="flex flex-col gap-[2em]">
                <div id="filter-container" className="bg-red-400 rounded-xl h-full w-full p-[2em]">
                    <FilterBlock onFilter={setFilterResult} />
                </div>
                <div id="reservation-info-container" className="bg-blue-400 rounded-xl h-full w-full p-[2em] text-center content-center">
                    {loadReservationInfo()}
                </div>
            </div>
        </div>
    )
}

export default App