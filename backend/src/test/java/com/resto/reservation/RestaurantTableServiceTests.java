package com.resto.reservation;

import com.resto.reservation.config.ZoneConfig;
import com.resto.reservation.entity.RestaurantTable;
import com.resto.reservation.enums.UserPreferences;
import com.resto.reservation.repository.RestaurantTableRepository;
import com.resto.reservation.service.RestaurantTableService;
import com.resto.reservation.service.ZoneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantTableServiceTests {

    @Mock
    private RestaurantTableRepository restaurantTableRepo;
    @Mock
    private ZoneService zoneService;
    @InjectMocks
    private RestaurantTableService restaurantTableService;

    private RestaurantTable table1;
    private RestaurantTable table2;
    private RestaurantTable table3;
    private ZonedDateTime testTime;

    @BeforeEach
    void setup() {
        // terrace zone
        table1 = new RestaurantTable();
        table1.setId(1L);
        table1.setX(1);
        table1.setY(1);
        table1.setCapacity(4);
        table1.setTableNumber(1);

        // terrace zone
        table2 = new RestaurantTable();
        table2.setId(2L);
        table2.setX(5);
        table2.setY(2);
        table2.setCapacity(6);
        table1.setTableNumber(2);

        // quiet zone
        table3 = new RestaurantTable();
        table3.setId(3L);
        table3.setX(5);
        table3.setY(5);
        table3.setCapacity(2);
        table3.setTableNumber(3);

        testTime = ZonedDateTime.now();
    }

    @Test
    void filterTables_allNullReturnsEmpty() {
        List<RestaurantTable> result = restaurantTableService.filterTables(null, null, null);

        assertTrue(result.isEmpty());
        verifyNoInteractions(restaurantTableRepo, zoneService);
    }

    @Test
    void filterTables_groupSizeOnly() {
        when(restaurantTableRepo.getByCapacityGreaterThanEqual(4)).thenReturn(List.of(table1, table2));

        List<RestaurantTable> result = restaurantTableService.filterTables(null, null, 4);

        assertThat(result).containsExactlyInAnyOrder(table1, table2);
        verify(restaurantTableRepo).getByCapacityGreaterThanEqual(4);
        verifyNoInteractions(zoneService);
    }

    @Test
    void filterTables_timeOnly() {
        when(restaurantTableRepo.findAvailableTablesInRange(testTime, testTime.plusHours(2))).thenReturn(List.of(table1, table3));

        List<RestaurantTable> result = restaurantTableService.filterTables(testTime, null, null);

        assertThat(result).containsExactlyInAnyOrder(table1, table3);
        verify(restaurantTableRepo).findAvailableTablesInRange(testTime, testTime.plusHours(2));
        verify(restaurantTableRepo, never()).getByCapacityGreaterThanEqual(any());
        verifyNoInteractions(zoneService);
    }

    @Test
    void filterTables_preferencesOnly() {
        UserPreferences preference = UserPreferences.QUIET;
        ZoneConfig.Bounds bounds = new ZoneConfig.Bounds();
        bounds.setxMax(6);
        bounds.setxMin(4);
        bounds.setyMax(6);
        bounds.setyMin(3);

        when(zoneService.getZoneBounds("QUIET")).thenReturn(bounds);
        when(restaurantTableRepo.getByPositionIn(4, 3, 6, 6)).thenReturn(List.of(table3));

        List<RestaurantTable> result = restaurantTableService.filterTables(null, List.of(preference), null);

        assertThat(result).containsExactly(table3);
    }

    @Test
    void filterTables_multiplePreferences() {
        List<UserPreferences> preferences = List.of(UserPreferences.QUIET, UserPreferences.TERRACE);
        ZoneConfig.Bounds quietBounds = new ZoneConfig.Bounds();
        quietBounds.setxMax(6);
        quietBounds.setxMin(4);
        quietBounds.setyMax(6);
        quietBounds.setyMin(3);

        ZoneConfig.Bounds terraceBounds = new ZoneConfig.Bounds();
        terraceBounds.setxMax(6);
        terraceBounds.setxMin(1);
        terraceBounds.setyMax(2);
        terraceBounds.setyMin(1);

        when(zoneService.getZoneBounds("QUIET")).thenReturn(quietBounds);
        when(zoneService.getZoneBounds("TERRACE")).thenReturn(terraceBounds);

        when(restaurantTableRepo.getByPositionIn(4, 3, 6, 6)).thenReturn(List.of(table3));
        when(restaurantTableRepo.getByPositionIn(1, 1, 6, 2)).thenReturn(List.of(table1, table2));

        List<RestaurantTable> result = restaurantTableService.filterTables(null, preferences, null);

        assertThat(result).containsExactlyInAnyOrder(table1, table2, table3);
    }

    @Test
    void filterTables_groupSizeAndTime() {
        when(restaurantTableRepo.getByCapacityGreaterThanEqual(4)).thenReturn(List.of(table1, table2));
        when(restaurantTableRepo.findAvailableTablesInRange(testTime, testTime.plusHours(2))).thenReturn(List.of(table2, table3));

        List<RestaurantTable> result = restaurantTableService.filterTables(testTime, null, 4);

        assertThat(result).containsExactly(table2);

        RestaurantTable recommended = restaurantTableService.getRecommendedTable(result, null, 4);
        assertThat(recommended).isEqualTo(table2);
    }

    @Test
    void filterTables_timeAndPreferenceNoIntersection() {
        UserPreferences preference = UserPreferences.TERRACE;
        ZoneConfig.Bounds bounds = new ZoneConfig.Bounds();
        bounds.setxMax(6);
        bounds.setxMin(1);
        bounds.setyMax(2);
        bounds.setyMin(1);

        when(restaurantTableRepo.findAvailableTablesInRange(testTime, testTime.plusHours(2))).thenReturn(List.of(table3));
        when(zoneService.getZoneBounds("TERRACE")).thenReturn(bounds);
        when(restaurantTableRepo.getByPositionIn(1, 1, 6, 2)).thenReturn(List.of(table1, table2));

        List<RestaurantTable> result = restaurantTableService.filterTables(testTime, List.of(preference), null);

        assertTrue(result.isEmpty());
    }

    @Test
    void getRecommendedTable_nullWhenFilteredTablesEmpty() {
        RestaurantTable result = restaurantTableService.getRecommendedTable(List.of(), null, 4);
        assertNull(result);
    }

    @Test
    void getRecommendedTable_noPreferences() {
        RestaurantTable result = restaurantTableService.getRecommendedTable(List.of(table1, table2), null, 4);
        assertEquals(result, table1);
    }

    @Test
    void getRecommendedTable_withPreferences() {
        List<UserPreferences> userPreferences = List.of(UserPreferences.QUIET);
        ZoneConfig.Bounds bounds = new ZoneConfig.Bounds();
        bounds.setxMin(4);
        bounds.setxMax(6);
        bounds.setyMin(3);
        bounds.setyMax(6);
        when(zoneService.getZoneBounds("QUIET")).thenReturn(bounds);
        when(restaurantTableRepo.getByPositionIn(4, 3, 6, 6)).thenReturn(List.of(table3));
        when(restaurantTableService.applyUserPreferenceFilter(List.of(table2, table3), userPreferences)).thenReturn(List.of(table3));

        RestaurantTable result = restaurantTableService.getRecommendedTable(List.of(table2, table3), userPreferences, 2);

        assertEquals(result, table3);
    }

    @Test
    void getRecommendedTable_preferencesNoMatch() {
        List<UserPreferences> userPreferences = List.of(UserPreferences.QUIET);
        ZoneConfig.Bounds bounds = new ZoneConfig.Bounds();
        bounds.setxMin(4);
        bounds.setxMax(6);
        bounds.setyMin(3);
        bounds.setyMax(6);
        when(zoneService.getZoneBounds("QUIET")).thenReturn(bounds);
        when(restaurantTableRepo.getByPositionIn(4, 3, 6, 6)).thenReturn(List.of());
        when(restaurantTableService.applyUserPreferenceFilter(List.of(table1, table2), userPreferences)).thenReturn(List.of());

        RestaurantTable result = restaurantTableService.getRecommendedTable(List.of(table1, table2), userPreferences, 4);
        assertEquals(result, table1);
    }

    @Test
    void getRecommendedTable_preferencesMostEfficient() {
        List<UserPreferences> userPreferences = List.of(UserPreferences.TERRACE);
        ZoneConfig.Bounds bounds = new ZoneConfig.Bounds();
        bounds.setxMin(1);
        bounds.setxMax(6);
        bounds.setyMin(1);
        bounds.setyMax(2);
        when(zoneService.getZoneBounds("TERRACE")).thenReturn(bounds);
        when(restaurantTableRepo.getByPositionIn(1, 1, 6, 2)).thenReturn(List.of(table1, table2));
        when(restaurantTableService.applyUserPreferenceFilter(List.of(table1, table2), userPreferences)).thenReturn(List.of(table1, table2));

        RestaurantTable result = restaurantTableService.getRecommendedTable(List.of(table1, table2), userPreferences, 4);

        assertEquals(result, table1);
    }
}
