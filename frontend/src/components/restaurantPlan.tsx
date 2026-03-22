import TableObject from "./tableObject.tsx";
import {useEffect, useState} from "react";
import {RestaurantTable} from "../types/RestaurantTable.ts";
import GridBlock from "./gridBlock.tsx";

export default function RestaurantPlan({ onSelect } : Readonly<{ onSelect: (item: RestaurantTable) => void }>) {

    const [tables, setTables] = useState<RestaurantTable[]>([])
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState<boolean>(false);

    useEffect(() => {
        const fetchTables = async () => {
            setLoading(true)
            try {
            const tables = await fetch(`/api/tables/all`);
            if (!tables.ok) throw new Error("Couldn't fetch tables");
            const data = await tables.json();
            setTables(data);
            } catch (err) {
                setError("Couldn't load tables")
            } finally {
                setLoading(false);
            }
        };
        fetchTables();
    }, []);

    if (loading) return <p>Loading...</p>
    if (error) return <p>{error}</p>

    const tableMap = new Map(tables.map(table => [`${table.x},${table.y}`, table]));

    const cells = []
    for (let row = 6; row > 0; row--) {
        for (let col = 1; col <= 6; col++) {
            const table = tableMap.get(`${col},${row}`);
            cells.push(
                table ? <TableObject key={`${col},${row}`} table={table} onClick={() => onSelect(table)} /> : <GridBlock key={`${col},${row}`} />
            )
        }
    }

    return (
        <div className="grid grid-cols-6 gap-10">
            {cells}
        </div>
    )
}