package com.resto.reservation.controller;

import com.resto.reservation.entity.responseobjects.FilteredTableResponse;
import com.resto.reservation.entity.RestaurantTable;
import com.resto.reservation.enums.UserPreferences;
import com.resto.reservation.repository.RestaurantTableRepository;
import com.resto.reservation.service.RestaurantTableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@RestController
@Tag(name = "Tables", description = "Restaurant table management endpoints")
@RequestMapping("/api/tables")
public class RestaurantTableController {

    private final RestaurantTableRepository tableRepo;
    private final RestaurantTableService restaurantTableService;

    public RestaurantTableController(RestaurantTableRepository tableRepo, RestaurantTableService restaurantTableService) {
        this.tableRepo = tableRepo;
        this.restaurantTableService = restaurantTableService;
    }

    @Operation(summary = "Get a list of available tables during a specified time",
            description = "Returns all tables that have no overlapping reservations at the given timestamp. Time must be provided in ISO 8601 format with timezone offset (e.g 2026-21-03T19:00:00+02:00)",
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully retrieved available tables",
                        content = @Content(
                                mediaType = "application/json",
                                array = @ArraySchema(schema = @Schema(
                                        implementation = RestaurantTable.class
                                ))
                        )
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid or missing time parameter",
                        content = @Content
                )
            }
    )
    @GetMapping("/availableat")
    public ResponseEntity<List<RestaurantTable>> getAvailableTablesDuring(
            @Parameter(
                    description = "Timestamp in ISO 8601 format with timezone",
                    required = true,
                    example = "2026-03-21T19:00:00+02:00",
                    schema = @Schema(type = "string", format = "date-time")
            )@RequestParam("time") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime time) {
        ZonedDateTime utc = time.withZoneSameInstant(ZoneOffset.UTC);
        return ResponseEntity.ok(tableRepo.findAvailableTablesAtTime(utc));
    }

    @Operation(summary = "Get a list of tables matching the specified filters",
            description = "Filters tables by availability time, user seating preferences, group size. If time and group size parameters are provided and matching tables for "
                                + "said parameters exist, a recommended table is also returned (null if not). Recommended table accommodates user preferences if possible and seats the guests "
                                + "as efficiently as possible",
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully retrieved filtered tables with an optional table recommendation",
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(
                                        implementation = FilteredTableResponse.class
                                )
                        )
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid parameter format",
                        content = @Content
                )
            })
    @GetMapping("/filtered")
    public ResponseEntity<FilteredTableResponse> getFilteredTables(
            @Parameter(
                    description = "Filter by availability at this timestamp. ISO 8601 format with timezone",
                    required = false,
                    example = "2026-03-21T19:00:00+02:00",
                    schema = @Schema(type = "string", format = "date-time")
            )@RequestParam(value = "time", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime time,

           @Parameter(
                   description = "Filter by user's seating area preferences.",
                   required = false,
                   example = "[\"QUIET\", \"TERRACE\"]",
                   array = @ArraySchema(schema = @Schema(
                           implementation = UserPreferences.class
                   ))
           )
           @RequestParam(value = "userPreferences", required = false) List<UserPreferences> userPreferences,

           @Parameter(
                   description = "Filter by group size, the minimum seating capacity required for the table",
                   required = false,
                   example = "4",
                   schema = @Schema(type = "integer", minimum = "1")
           )
           @RequestParam(value = "groupSize", required = false) Integer groupSize) {

        List<RestaurantTable> filteredTables = restaurantTableService.filterTables(time, userPreferences, groupSize);
        RestaurantTable recommendedTable = null;

        if (time != null && groupSize != null && !filteredTables.isEmpty()) {
            recommendedTable = restaurantTableService.getRecommendedTable(filteredTables, userPreferences);
        }
        return ResponseEntity.ok(new FilteredTableResponse(filteredTables, recommendedTable));
    }
}
