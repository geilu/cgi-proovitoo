import {RestaurantTable} from "./RestaurantTable.ts";
import {User} from "./User.ts";

export interface Reservation {
    id: number,
    user: User,
    guestCount: number,
    startTime: string,
    endTime: string,
    restaurantTable: RestaurantTable,
    status: string
}