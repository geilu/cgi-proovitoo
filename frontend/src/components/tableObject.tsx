import {RestaurantTable} from "../types/RestaurantTable.ts";

export default function TableObject({table, onClick, dimmed, recommended} : Readonly<{
    table: RestaurantTable,
    onClick?: () => void
    dimmed?: boolean,
    recommended?: boolean
    }>) {
    return (
        <button className={`bg-green-500 py-[1em] px-[1.5em] rounded-xl 
        ${recommended ? "bg-yellow-500" : "bg-green-500"}
        ${dimmed ? "opacity-30" : "opacity-100"}`}
                onClick={onClick}>
            <span>{table.capacity}</span>
        </button>
    )
}