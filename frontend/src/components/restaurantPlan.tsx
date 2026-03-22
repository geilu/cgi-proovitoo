import TableObject from "./tableObject.tsx";
import {useEffect, useState} from "react";
import {RestaurantTable} from "../types/RestaurantTable.ts";

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

    return (
        <div className="flex flex-row flex-wrap gap-[1em]">
            {tables.map(table =>
            <TableObject table={table} key={table.id} onClick={() => onSelect(table)} />
            )}
        </div>
    )
}