package com.resto.reservation.entity.responseobjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.resto.reservation.entity.RestaurantTable;

import java.util.List;

public class FilteredTableResponse {
    private List<RestaurantTable> filteredTables;
    private RestaurantTable recommendedTable;

    public FilteredTableResponse(List<RestaurantTable> filteredTables, RestaurantTable recommendedTable) {
        this.filteredTables = filteredTables;
        this.recommendedTable = recommendedTable;
    }

    public List<RestaurantTable> getFilteredTables() {
        return filteredTables;
    }

    public void setFilteredTables(List<RestaurantTable> filteredTables) {
        this.filteredTables = filteredTables;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public RestaurantTable getRecommendedTable() {
        return recommendedTable;
    }

    public void setRecommendedTable(RestaurantTable recommendedTable) {
        this.recommendedTable = recommendedTable;
    }
}
