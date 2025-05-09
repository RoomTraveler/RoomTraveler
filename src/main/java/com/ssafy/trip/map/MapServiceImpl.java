// src/main/java/com/ssafy/trip/model/service/trip/MapServiceImpl.java
package com.ssafy.trip.map;

import com.ssafy.trip.map.MapDTO.ContentType;
import com.ssafy.trip.map.MapDTO.Gugun;
import com.ssafy.trip.map.MapDTO.Plan;
import com.ssafy.trip.map.MapDTO.RegionTripResDto;
import com.ssafy.trip.map.MapDTO.Sido;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MapServiceImpl implements MapService {
    private final MapDAO mapDAO;

    @Override
    public List<RegionTripResDto> getRegionTrip(int sidoCode, int gugunCode) {
        return mapDAO.getRegionTrip(sidoCode, gugunCode);
    }

    @Override
    public List<Sido> getSidoList() {
        return mapDAO.getSidos();
    }

    @Override
    public List<Gugun> getGugunList(int sido) {
        return mapDAO.getGuguns(sido);
    }

    @Override
    public List<ContentType> getContentTypes() {
        return mapDAO.getContentTypes();
    }

    @Override
    public List<RegionTripResDto> getInfoByLocalContent(int sidoCode, int gugunCode, int contentTypeId) {
        return mapDAO.getInfoByLocalContent(sidoCode, gugunCode, contentTypeId);
    }

    @Override
    public void savePlan(String email, List<Integer> attractionIds) {
        Long userId = mapDAO.findUserIdByEmail(email);
        int planId = mapDAO.insertPlan(userId);
        mapDAO.insertPlanAttractions(planId, attractionIds);
    }

    @Override
    public List<Plan> getPlan(Long userId) {
        return mapDAO.getPlan(userId);
    }

    @Override
    public List<RegionTripResDto> findShortestPlan(RegionTripResDto startLocation,
                                                   List<RegionTripResDto> locations) {
        int n = locations.size();
        int[] idxs = new int[n];
        for (int i = 0; i < n; i++) idxs[i] = i;

        double minDist = Double.MAX_VALUE;
        List<RegionTripResDto> best = new ArrayList<>();

        do {
            RegionTripResDto cur = startLocation;
            double sum = 0.0;
            List<RegionTripResDto> path = new ArrayList<>();
            for (int idx : idxs) {
                RegionTripResDto loc = locations.get(idx);
                path.add(loc);
                sum += haversine(cur.getLatitude(), cur.getLongitude(),
                                 loc.getLatitude(), loc.getLongitude());
                cur = loc;
            }
            // 출발지 복귀
            sum += haversine(cur.getLatitude(), cur.getLongitude(),
                             startLocation.getLatitude(), startLocation.getLongitude());

            if (sum < minDist) {
                minDist = sum;
                best = new ArrayList<>(path);
            }
        } while (nextPermutation(idxs));

        return best;
    }

    // 다음 순열
    private static boolean nextPermutation(int[] a) {
        int n = a.length;
        int i = n - 1;
        while (i > 0 && a[i - 1] >= a[i]) i--;
        if (i <= 0) return false;
        int j = n - 1;
        while (a[j] <= a[i - 1]) j--;
        swap(a, i - 1, j);
        j = n - 1;
        while (i < j) {
            swap(a, i, j);
            i++;
            j--;
        }
        return true;
    }

    private static void swap(int[] a, int x, int y) {
        int tmp = a[x];
        a[x] = a[y];
        a[y] = tmp;
    }

    private static double haversine(double lat1, double lon1,
                                    double lat2, double lon2) {
        double R = 6371; // km
        double toRad = Math.PI / 180;
        double dLat = (lat2 - lat1) * toRad;
        double dLon = (lon2 - lon1) * toRad;
        double sLat = Math.sin(dLat / 2);
        double sLon = Math.sin(dLon / 2);
        double h = sLat*sLat +
                   Math.cos(lat1*toRad)*Math.cos(lat2*toRad)*sLon*sLon;
        return 2 * R * Math.asin(Math.sqrt(h));
    }
}
