import {RestaurantTable} from "../types/RestaurantTable.ts";
// @ts-ignore
import TableObj from "../resources/tableObj.svg";
// @ts-ignore
import TableObjRecommended from "../resources/tableObjRecommended.svg";

export default function TableObject({table, onClick, dimmed, recommended} : Readonly<{
    table: RestaurantTable,
    onClick?: () => void
    dimmed?: boolean,
    recommended?: boolean
    }>) {

    return (
        <button
            id={`table-${table.id}`}
            className={`table-obj relative inline-flex items-center justify-center bg-transparent border-0 p-0 cursor-pointer
        ${dimmed ? "opacity-30" : "opacity-100"}`}
            onClick={onClick}
        >
            <img src={recommended ? TableObjRecommended : TableObj} alt="table" />
            <span className="absolute font-bold">{table.capacity}</span>
        </button>
    )
}