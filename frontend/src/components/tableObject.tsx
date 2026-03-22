import {RestaurantTable} from "../types/RestaurantTable.ts";

export default function TableObject({table, onClick} : Readonly<{ table: RestaurantTable, onClick?: () => void }>) {
    return (
        <button className="bg-green-500 p-[2em] rounded-xl" onClick={onClick}>
            {table.tableNumber}
        </button>
    )
}