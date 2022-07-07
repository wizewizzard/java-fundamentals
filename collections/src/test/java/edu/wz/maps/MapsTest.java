package edu.wz.maps;


import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

public class MapsTest {

    /**
     * Being based on maps sets share the same logic with maps. HasMap tolerates both null key and values. But when
     * default comparison does not handle with null key and throws NullPointerException. Creating custom comparator
     * solves it.
     */
    @Test
    public void testNullKeyAndValues(){
        Map<String, String> hashMap = new HashMap<>();
        Map<String, String> treeMap = new TreeMap<>();
        Map<String, String> treeMapCustomComparator = new TreeMap<>(Comparator.comparing(s -> s, (s1, s2) -> {
            if(s1 == null && s2 == null)
                return 0;
            if(s1 == null)
                return -1;
            if(s2 == null)
                return 1;
            return s1.compareTo(s2);
        }));

        hashMap.put("KEY", null);
        hashMap.put(null, "VALUE");

        treeMapCustomComparator.put("KEY", null);
        treeMapCustomComparator.put(null, "VALUE");

        treeMap.put("KEY", null);
        Throwable throwable = catchThrowable(() -> treeMap.put(null, null));


        assertThat(hashMap).containsKey(null);
        assertThat(hashMap).containsValue(null);
        assertThat(treeMap).containsValue(null);
        assertThat(treeMapCustomComparator).containsKey(null);
        assertThat(treeMapCustomComparator).containsValue(null);
        assertThat(throwable.getClass()).isEqualTo(NullPointerException.class);
    }

    @Test
    public void miscHashMapMethods(){
        Map<String, String> hashMap = new HashMap<>();
        hashMap.put("Barbara", "Dark Energy");
        hashMap.put("Larry", "Handgun");
        hashMap.put("Minimi", "Fireball");
        hashMap.put("Lola", "Katana");
        hashMap.put("Akwell", "Sun ray");

        hashMap.computeIfAbsent("Jubee", String::toUpperCase);
        hashMap.computeIfPresent("Minimi", (k, cv) -> cv.concat(", Splash"));
        hashMap.merge("Larry", "Rifle", (s1, s2) -> s1.concat(", ").concat(s2));

        assertThat(hashMap).containsEntry("Jubee", "Jubee".toUpperCase());
        assertThat(hashMap).containsEntry("Minimi", "Fireball, Splash");
        assertThat(hashMap).containsEntry("Larry", "Handgun, Rifle");
    }



}
