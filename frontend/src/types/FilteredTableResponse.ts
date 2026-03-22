import {RestaurantTable} from "./RestaurantTable.ts";

export interface FilteredTableResponse {
    filteredTables: RestaurantTable[];
    recommendedTable: RestaurantTable | null;
}