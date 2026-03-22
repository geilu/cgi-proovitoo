import {RestaurantTable} from "../types/RestaurantTable.ts";

export default function TableObject({table, onClick} : Readonly<{ table: RestaurantTable, onClick?: () => void }>) {
    return (
        <button className="bg-green-500 py-[1em] px-[1.5em] rounded-xl" onClick={onClick}>
            <span>{table.capacity}</span>
        </button>
    )
}